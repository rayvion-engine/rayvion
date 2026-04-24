package com.rayvion.engine.characteristic.impl;

import com.rayvion.engine.characteristic.CharacteristicChangedEvent;
import com.rayvion.engine.characteristic.CharacteristicDescriptor;
import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.event.EventManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the {@link CharacteristicSystem}.
 * <p>
 * This implementation uses {@link ConcurrentHashMap} to store characteristic descriptors
 * and entity values, ensuring thread-safe access to characteristic data.
 * </p>
 */
public class CharacteristicSystemImpl implements CharacteristicSystem {
    /**
     * The event manager used to publish characteristic change events.
     */
    private final EventManager eventManager;

    /**
     * A map of registered characteristic descriptors, keyed by their ID.
     */
    private final Map<String, CharacteristicDescriptor<?>> descriptors = new ConcurrentHashMap<>();

    /**
     * A map of entity characteristic values, keyed by entity ID.
     * Each entity has a nested map of characteristic values keyed by characteristic ID.
     */
    private final Map<Long, Map<String, Object>> entityValues = new ConcurrentHashMap<>();

    /**
     * Constructs a new CharacteristicSystemImpl.
     *
     * @param eventManager The event manager to use for publishing events.
     */
    public CharacteristicSystemImpl(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public void init() {
        eventManager.registerEventType(CharacteristicChangedEvent.class);
    }

    @Override
    public void registerCharacteristic(CharacteristicDescriptor<?> descriptor) {
        descriptors.put(descriptor.getId(), descriptor);
    }

    @Override
    public Collection<CharacteristicDescriptor<?>> getRegisteredCharacteristics() {
        return Collections.unmodifiableCollection(descriptors.values());
    }

    @Override
    public Optional<CharacteristicDescriptor<?>> getDescriptor(String id) {
        return Optional.ofNullable(descriptors.get(id));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getValue(Entity entity, String characteristicId) {
        Map<String, Object> values = entityValues.get(entity.id());
        if (values != null && values.containsKey(characteristicId)) {
            return (T) values.get(characteristicId);
        }

        CharacteristicDescriptor<?> descriptor = descriptors.get(characteristicId);
        if (descriptor == null) {
            throw new IllegalArgumentException("Characteristic not registered: " + characteristicId);
        }
        return (T) descriptor.getDefaultValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void setValue(Entity entity, String characteristicId, T value) {
        CharacteristicDescriptor<T> descriptor = (CharacteristicDescriptor<T>) descriptors.get(characteristicId);
        if (descriptor == null) {
            throw new IllegalArgumentException("Characteristic not registered: " + characteristicId);
        }

        if (value != null && !descriptor.getType().isInstance(value)) {
            throw new IllegalArgumentException("Invalid value type for characteristic " + characteristicId +
                ". Expected " + descriptor.getType().getName() + " but got " + value.getClass().getName());
        }

        T oldValue = getValue(entity, characteristicId);

        entityValues.computeIfAbsent(entity.id(), id -> new ConcurrentHashMap<>())
                .put(characteristicId, value);

        if (!Objects.equals(oldValue, value)) {
            eventManager.publish(new CharacteristicChangedEvent<>(entity, characteristicId, oldValue, value));
        }
    }

    @Override
    public boolean hasCharacteristic(Entity entity, String characteristicId) {
        Map<String, Object> values = entityValues.get(entity.id());
        return values != null && values.containsKey(characteristicId);
    }

    @Override
    public void removeCharacteristic(Entity entity, String characteristicId) {
        Map<String, Object> values = entityValues.get(entity.id());
        if (values != null) {
            Object oldValue = values.remove(characteristicId);
            if (oldValue != null) {
                CharacteristicDescriptor<?> descriptor = descriptors.get(characteristicId);
                eventManager.publish(new CharacteristicChangedEvent<>(entity, characteristicId, oldValue, descriptor.getDefaultValue()));
            }
        }
    }
}
