package com.rayvion.engine.equipment;

import com.rayvion.engine.event.Event;
import com.rayvion.engine.inventory.InventoryItem;

/**
 * Event fired when an entity unequips an item.
 */
public record UnequippedEvent(long entityId, InventoryItem item) implements Event {
}
