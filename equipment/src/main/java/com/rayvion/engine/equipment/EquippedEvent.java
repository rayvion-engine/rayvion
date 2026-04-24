package com.rayvion.engine.equipment;

import com.rayvion.engine.event.Event;
import com.rayvion.engine.inventory.InventoryItem;

/**
 * Event fired when an entity equips an item.
 */
public record EquippedEvent(long entityId, InventoryItem item) implements Event {
}
