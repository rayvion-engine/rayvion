package com.rayvion.engine.inventory;

import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.event.Event;

/**
 * Event emitted when an item is added to an entity's inventory.
 * <p>
 * This event contains the entity that received the item and the item data itself.
 * </p>
 *
 * @param entity The entity whose inventory received the item.
 * @param item   The item that was added.
 */
public record ItemAddedEvent(Entity entity, InventoryItem item) implements Event {
}
