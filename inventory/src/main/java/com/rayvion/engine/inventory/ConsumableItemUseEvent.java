package com.rayvion.engine.inventory;

import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.event.Event;

/**
 * Event published when an item is used from the inventory.
 * <p>
 * This event is typically triggered when an entity consumes or activates a usable item.
 * </p>
 *
 * @param user The entity using the item.
 * @param item The item being used.
 */
public record ConsumableItemUseEvent(
    Entity user,
    InventoryItem item
) implements Event {
}
