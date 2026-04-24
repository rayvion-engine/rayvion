package com.rayvion.engine.inventory.impl;

import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.inventory.Inventory;
import com.rayvion.engine.inventory.InventoryItem;
import com.rayvion.engine.inventory.ItemAddedEvent;
import com.rayvion.engine.inventory.ItemRemovedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Standard implementation of the {@link Inventory} interface.
 * <p>
 * This implementation manages an internal list of {@link InventoryItem}s and
 * publishes {@link ItemAddedEvent} and {@link ItemRemovedEvent} whenever the
 * collection is modified.
 * </p>
 */
public class InventoryImpl implements Inventory {
    private final Entity owner;
    private final EventManager eventManager;
    private final List<InventoryItem> items = new ArrayList<>();

    /**
     * Constructs a new InventoryImpl for a specific entity owner.
     *
     * @param owner        The entity that owns this inventory.
     * @param eventManager The event manager used for publishing inventory events.
     */
    public InventoryImpl(Entity owner, EventManager eventManager) {
        this.owner = owner;
        this.eventManager = eventManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<InventoryItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Publishes an {@link ItemAddedEvent} after the item is added.
     * </p>
     */
    @Override
    public void addItem(InventoryItem item) {
        items.add(item);
        eventManager.publish(new ItemAddedEvent(owner, item));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Publishes an {@link ItemRemovedEvent} if the item was successfully removed.
     * </p>
     */
    @Override
    public boolean removeItem(InventoryItem item) {
        boolean removed = items.remove(item);
        if (removed) {
            eventManager.publish(new ItemRemovedEvent(owner, item));
        }
        return removed;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Publishes an {@link ItemRemovedEvent} for every item that was cleared from the inventory.
     * </p>
     */
    @Override
    public void clear() {
        List<InventoryItem> itemsToClear = new ArrayList<>(items);
        items.clear();
        for (InventoryItem item : itemsToClear) {
            eventManager.publish(new ItemRemovedEvent(owner, item));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return items.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }
}
