package com.rayvion.engine.bindings.impl;

import com.rayvion.engine.bindings.*;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.input.KeyEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import lombok.extern.slf4j.Slf4j;

/**
 * Standard implementation of the {@link BindingSystem}.
 * <p>
 * This implementation subscribes to {@link KeyEvent}s and translates them into {@link BindingEvent}s.
 * It maintains internal maps for efficient lookup between keys and parameters.
 * It also tracks which keys are currently active for each parameter to ensure that
 * a "KEY_DOWN" event is only sent when the parameter first becomes active (first key pressed),
 * and a "KEY_UP" event is only sent when it becomes inactive (last key released).
 */
@Slf4j
public class BindingSystemImpl implements BindingSystem {
    private final EventManager eventManager;
    private final Map<String, BindingGroup> groups = new ConcurrentHashMap<>();
    private final Map<String, Map<String, BindingParameter>> parameters = new ConcurrentHashMap<>();
    
    private final Map<BindingParameter, Set<Integer>> paramToKeys = new ConcurrentHashMap<>();
    private final Map<Integer, Set<BindingParameter>> keyToParams = new ConcurrentHashMap<>();
    private final Map<BindingParameter, Set<Integer>> activeKeysPerParam = new ConcurrentHashMap<>();

    /**
     * Constructs a new BindingSystemImpl.
     *
     * @param eventManager the event manager used for publishing and subscribing to events
     */
    public BindingSystemImpl(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public void init() {
        // Register the event type so it can be published
        eventManager.registerEventType(BindingEvent.class);
        
        // Subscribe to raw key events
        eventManager.subscribe(KeyEvent.class, this::handleKeyEvent);
    }

    /**
     * Handles incoming key events and translates them into binding events.
     *
     * @param event the raw key event
     */
    private void handleKeyEvent(KeyEvent event) {
        Set<BindingParameter> boundParams = keyToParams.get(event.keycode());
        if (boundParams != null) {
            for (BindingParameter param : boundParams) {
                Set<Integer> activeKeys = activeKeysPerParam.computeIfAbsent(param, k -> new CopyOnWriteArraySet<>());
                
                if (event.type() == KeyEvent.Type.KEY_DOWN) {
                    if (activeKeys.isEmpty()) {
                        log.debug("BindingSystemImpl: Parameter {} became ACTIVE", param.name());
                        eventManager.publish(new BindingEvent(param, KeyEvent.Type.KEY_DOWN));
                    }
                    activeKeys.add(event.keycode());
                } else if (event.type() == KeyEvent.Type.KEY_UP) {
                    activeKeys.remove(event.keycode());
                    if (activeKeys.isEmpty()) {
                        log.debug("BindingSystemImpl: Parameter {} became INACTIVE", param.name());
                        eventManager.publish(new BindingEvent(param, KeyEvent.Type.KEY_UP));
                    }
                }
            }
        }
    }

    @Override
    public BindingGroup createGroup(String name) {
        return groups.computeIfAbsent(name, BindingGroup::new);
    }

    @Override
    public BindingParameter createParameter(BindingGroup group, String name) {
        return parameters.computeIfAbsent(group.name(), k -> new ConcurrentHashMap<>())
                .computeIfAbsent(name, n -> new BindingParameter(n, group));
    }

    @Override
    public void addBinding(BindingParameter parameter, int keycode) {
        log.info("BindingSystemImpl: Adding binding for {} to key {}", parameter.name(), keycode);
        paramToKeys.computeIfAbsent(parameter, k -> new CopyOnWriteArraySet<>()).add(keycode);
        keyToParams.computeIfAbsent(keycode, k -> new CopyOnWriteArraySet<>()).add(parameter);
    }

    @Override
    public void removeBinding(BindingParameter parameter, int keycode) {
        Set<Integer> keys = paramToKeys.get(parameter);
        if (keys != null) {
            keys.remove(keycode);
        }
        
        Set<BindingParameter> params = keyToParams.get(keycode);
        if (params != null) {
            params.remove(parameter);
        }
    }

    @Override
    public Collection<Integer> getBindings(BindingParameter parameter) {
        return Collections.unmodifiableCollection(paramToKeys.getOrDefault(parameter, Collections.emptySet()));
    }

    @Override
    public Optional<BindingGroup> getGroup(String name) {
        return Optional.ofNullable(groups.get(name));
    }

    @Override
    public Optional<BindingParameter> getParameter(BindingGroup group, String name) {
        Map<String, BindingParameter> groupParams = parameters.get(group.name());
        if (groupParams == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(groupParams.get(name));
    }
}
