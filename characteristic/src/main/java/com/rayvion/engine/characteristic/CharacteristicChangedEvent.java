package com.rayvion.engine.characteristic;

import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.event.Event;
import lombok.Value;

/**
 * Event fired when a characteristic value is changed for an entity.
 * This event allows other systems to react to changes in dynamic entity attributes.
 *
 * @param <T> The type of the characteristic value.
 */
@Value
public class CharacteristicChangedEvent<T> implements Event {
    /**
     * The entity whose characteristic was changed.
     */
    Entity entity;

    /**
     * The unique identifier of the characteristic that was changed.
     */
    String characteristicId;

    /**
     * The value of the characteristic before the change.
     */
    T oldValue;

    /**
     * The new value of the characteristic.
     */
    T newValue;
}
