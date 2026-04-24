package com.rayvion.engine.inventory;

import com.rayvion.engine.graphics.EntityGraphics;

/**
 * Represents an item that can be stored in an inventory or exist in the world.
 * <p>
 * This record holds the immutable data for an item, including its identity,
 * presentation, and behavior (like auto-pickup).
 * </p>
 *
 * @param id               Unique identifier for the item type.
 * @param name             Display name of the item.
 * @param description      Detailed description of the item.
 * @param type              The category or type of the item (e.g., "weapon", "consumable").
 * @param graphics          The visual representation of the item when on the ground or in inventory.
 * @param equippedGraphics The visual representation of the item when equipped (if applicable).
 * @param autoPickup        Whether the item is picked up automatically when an entity with an inventory is in proximity.
 */
public record InventoryItem(
    String id,
    String name,
    String description,
    String type,
    EntityGraphics graphics,
    EntityGraphics equippedGraphics,
    boolean autoPickup
) {
    /**
     * Constructs an InventoryItem without equipped graphics.
     *
     * @param id          Unique identifier for the item type.
     * @param name        Display name of the item.
     * @param description Detailed description of the item.
     * @param type         The category or type of the item.
     * @param graphics     The visual representation of the item.
     * @param autoPickup   Whether the item is picked up automatically.
     */
    public InventoryItem(String id, String name, String description, String type, EntityGraphics graphics, boolean autoPickup) {
        this(id, name, description, type, graphics, null, autoPickup);
    }
}
