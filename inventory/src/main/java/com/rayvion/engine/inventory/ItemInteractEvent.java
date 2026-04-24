package com.rayvion.engine.inventory;

import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.event.Event;

/**
 * Event published when an entity interacts with an item in its inventory.
 * <p>
 * This is a generic interaction event that can be used for actions like
 * inspecting, moving, or otherwise manipulating an item without necessarily
 * consuming it.
 * </p>
 *
 * @param entity The entity interacting with the item.
 * @param item   The item being interacted with.
 */
public record ItemInteractEvent(
    Entity entity,
    InventoryItem item
) implements Event {
}
