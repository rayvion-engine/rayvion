package com.rayvion.engine.inventory;

import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.event.Event;

/**
 * Event emitted when an item is removed from an entity's inventory.
 * <p>
 * This event contains the entity whose inventory lost the item and the item data that was removed.
 * </p>
 *
 * @param entity The entity whose inventory lost the item.
 * @param item   The item that was removed.
 */
public record ItemRemovedEvent(Entity entity, InventoryItem item) implements Event {
}
