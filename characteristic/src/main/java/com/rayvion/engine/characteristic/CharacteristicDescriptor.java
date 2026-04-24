package com.rayvion.engine.characteristic;

import lombok.Value;

/**
 * Descriptor for a characteristic, defining its metadata and type.
 * Characteristics are dynamic attributes that can be attached to entities.
 *
 * @param <T> The type of the characteristic value.
 */
@Value
public class CharacteristicDescriptor<T> {
    /**
     * The unique identifier of the characteristic.
     */
    String id;

    /**
     * The human-readable name of the characteristic.
     */
    String name;

    /**
     * A description of what this characteristic represents.
     */
    String description;

    /**
     * The class type of the characteristic value.
     */
    Class<T> type;

    /**
     * The default value for this characteristic when it's not explicitly set for an entity.
     */
    T defaultValue;
}
