package com.rayvion.engine.characteristic;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;

import java.util.Collection;
import java.util.Optional;

/**
 * System for managing dynamic entity characteristics.
 * <p>
 * This system provides a way to attach typed values to entities that are not
 * part of their core component structure. Characteristics must be registered
 * with a {@link CharacteristicDescriptor} before use.
 * </p>
 */
public interface CharacteristicSystem extends System {
    /**
     * The coordinate of the characteristic system trait.
     */
    SystemTraitCoordinate TRAIT = new SystemTraitCoordinate("com.rayvion.engine", "characteristic", Version.parse("0.1.0"));

    @Override
    default SystemDescriptor getDescriptor() {
        return new SystemDescriptor(
                new com.rayvion.engine.system.descriptor.SystemCoordinate("com.rayvion.engine", "characteristic", Version.parse("0.1.0")),
                java.util.Set.of(),
                java.util.Set.of(TRAIT)
        );
    }

    /**
     * Registers a new characteristic type.
     *
     * @param descriptor The descriptor defining the characteristic's metadata and type.
     * @throws IllegalArgumentException if a characteristic with the same ID is already registered.
     */
    void registerCharacteristic(CharacteristicDescriptor<?> descriptor);

    /**
     * Retrieves all registered characteristic descriptors.
     *
     * @return A collection of all registered characteristic descriptors.
     */
    Collection<CharacteristicDescriptor<?>> getRegisteredCharacteristics();

    /**
     * Gets a registered characteristic descriptor by its unique ID.
     *
     * @param id The unique identifier of the characteristic.
     * @return An {@link Optional} containing the descriptor if found, or empty otherwise.
     */
    Optional<CharacteristicDescriptor<?>> getDescriptor(String id);

    /**
     * Gets the current value of a characteristic for a specific entity.
     * <p>
     * If no value is explicitly set for the entity, the default value defined
     * in the characteristic's descriptor is returned.
     * </p>
     *
     * @param <T>              The expected type of the characteristic value.
     * @param entity           The entity to get the value for.
     * @param characteristicId The ID of the characteristic.
     * @return The current value of the characteristic for the entity.
     * @throws IllegalArgumentException if the characteristic is not registered.
     * @throws ClassCastException       if the value exists but is not of the expected type.
     */
    <T> T getValue(Entity entity, String characteristicId);

    /**
     * Sets the value of a characteristic for a specific entity.
     * <p>
     * If the new value is different from the old value, a {@link CharacteristicChangedEvent}
     * will be published via the system's event manager.
     * </p>
     *
     * @param <T>              The type of the characteristic value.
     * @param entity           The entity to set the value for.
     * @param characteristicId The ID of the characteristic.
     * @param value            The new value to set.
     * @throws IllegalArgumentException if the characteristic is not registered or if the value type is invalid.
     */
    <T> void setValue(Entity entity, String characteristicId, T value);

    /**
     * Checks if an entity has a specific characteristic value explicitly set.
     *
     * @param entity           The entity to check.
     * @param characteristicId The ID of the characteristic.
     * @return {@code true} if the characteristic value is explicitly set for the entity, {@code false} otherwise.
     */
    boolean hasCharacteristic(Entity entity, String characteristicId);

    /**
     * Removes an entity's custom characteristic value, resetting it to its default value.
     * <p>
     * If a value was explicitly set, removing it will trigger a {@link CharacteristicChangedEvent}
     * with the new value being the default value.
     * </p>
     *
     * @param entity           The entity to remove the characteristic from.
     * @param characteristicId The ID of the characteristic to remove.
     */
    void removeCharacteristic(Entity entity, String characteristicId);
}
