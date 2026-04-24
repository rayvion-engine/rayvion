package com.rayvion.engine.inventory;

import java.util.List;

/**
 * Interface representing an entity's inventory.
 * <p>
 * An inventory is a container that holds a list of {@link InventoryItem}s.
 * It provides methods for querying and modifying the contents of the container.
 * Implementations should typically emit events when items are added or removed.
 * </p>
 */
public interface Inventory {
    /**
     * Retrieves all items currently stored in the inventory.
     *
     * @return An unmodifiable list of items in the inventory.
     */
    List<InventoryItem> getItems();

    /**
     * Adds an item to the inventory.
     * <p>
     * Implementations may trigger a {@link ItemAddedEvent} upon success.
     * </p>
     *
     * @param item The item to add.
     */
    void addItem(InventoryItem item);

    /**
     * Removes a specific item instance from the inventory.
     * <p>
     * Implementations may trigger a {@link ItemRemovedEvent} upon success.
     * </p>
     *
     * @param item The item instance to remove.
     * @return {@code true} if the item was found and removed, {@code false} otherwise.
     */
    boolean removeItem(InventoryItem item);

    /**
     * Removes all items from the inventory.
     * <p>
     * Implementations should trigger {@link ItemRemovedEvent}s for each removed item.
     * </p>
     */
    void clear();

    /**
     * Returns the number of items currently in the inventory.
     *
     * @return The number of items.
     */
    int size();

    /**
     * Checks if the inventory contains any items.
     *
     * @return {@code true} if the inventory is empty, {@code false} otherwise.
     */
    boolean isEmpty();
}
