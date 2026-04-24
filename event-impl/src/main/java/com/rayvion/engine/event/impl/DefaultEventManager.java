package com.rayvion.engine.event.impl;

import com.rayvion.engine.event.Event;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.event.EventQueue;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Slf4j
public class DefaultEventManager implements EventManager {

    private final Set<Class<? extends Event>> registeredEvents = ConcurrentHashMap.newKeySet();
    private final Map<Class<?>, CopyOnWriteArrayList<Consumer<Event>>> pushSubscribers = new ConcurrentHashMap<>();
    private final Map<Class<?>, CopyOnWriteArrayList<DefaultEventQueue<Event>>> pullSubscribers = new ConcurrentHashMap<>();
    private final Map<Class<?>, Set<Class<?>>> hierarchyCache = new ConcurrentHashMap<>();

    @Override
    public void init() {
        log.info("Initializing DefaultEventManager");
    }

    @Override
    public <T extends Event> void registerEventType(Class<T> eventType) {
        registeredEvents.add(eventType);
    }

    @Override
    public <T extends Event> void publish(T event) {
        Class<? extends Event> eventClass = event.getClass();
        if (!registeredEvents.contains(eventClass)) {
            throw new IllegalArgumentException("Event type not registered: " + eventClass.getName());
        }

        Set<Class<?>> assignableTypes = getAssignableTypes(eventClass);

        for (Class<?> type : assignableTypes) {
            // Push subscribers
            List<Consumer<Event>> consumers = pushSubscribers.get(type);
            if (consumers != null) {
                for (Consumer<Event> consumer : consumers) {
                    try {
                        consumer.accept(event);
                    } catch (Exception e) {
                        log.error("Exception in event subscriber for event type: {}", eventClass.getName(), e);
                    }
                }
            }

            // Pull subscribers
            List<DefaultEventQueue<Event>> queues = pullSubscribers.get(type);
            if (queues != null) {
                for (DefaultEventQueue<Event> queue : queues) {
                    queue.offer(event);
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> void subscribe(Class<T> eventType, Consumer<T> subscriber) {
        pushSubscribers
                .computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add((Consumer<Event>) subscriber);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Event> EventQueue<T> subscribePull(Class<T> eventType) {
        DefaultEventQueue<T> queue = new DefaultEventQueue<>();
        pullSubscribers
                .computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add((DefaultEventQueue<Event>) queue);
        return queue;
    }

    private Set<Class<?>> getAssignableTypes(Class<?> leafClass) {
        return hierarchyCache.computeIfAbsent(leafClass, clazz -> {
            Set<Class<?>> types = new HashSet<>();
            collectAssignableTypes(clazz, types);
            return types;
        });
    }

    private void collectAssignableTypes(Class<?> clazz, Set<Class<?>> set) {
        if (clazz == null || clazz == Object.class) {
            return;
        }
        set.add(clazz);
        collectAssignableTypes(clazz.getSuperclass(), set);
        for (Class<?> iface : clazz.getInterfaces()) {
            collectAssignableTypes(iface, set);
        }
    }
}
