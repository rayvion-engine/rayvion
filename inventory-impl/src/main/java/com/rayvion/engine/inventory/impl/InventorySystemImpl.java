package com.rayvion.engine.inventory.impl;

import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.inventory.Inventory;
import com.rayvion.engine.inventory.InventorySystem;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Standard implementation of the {@link InventorySystem} interface.
 * <p>
 * This implementation uses a {@link ConcurrentHashMap} to store and manage
 * {@link Inventory} instances mapped by entity IDs. It also handles the
 * registration of inventory-related event types with the {@link EventManager}.
 * </p>
 */
public class InventorySystemImpl implements InventorySystem {
    private final EventManager eventManager;
    private final Map<Long, Inventory> inventories = new ConcurrentHashMap<>();

    /**
     * Constructs a new InventorySystemImpl.
     *
     * @param eventManager The event manager used for registering event types and publishing events.
     */
    public InventorySystemImpl(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    /**
     * Initializes the system by registering inventory event types.
     */
    @Override
    public void init() {
        eventManager.registerEventType(com.rayvion.engine.inventory.ItemAddedEvent.class);
        eventManager.registerEventType(com.rayvion.engine.inventory.ItemRemovedEvent.class);
        eventManager.registerEventType(com.rayvion.engine.inventory.ItemInteractEvent.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Inventory> getInventory(Entity entity) {
        return Optional.ofNullable(inventories.get(entity.id()));
    }

    /**
     * {@inheritDoc}
     * <p>
     * If no inventory exists for the entity, a new {@link InventoryImpl} is created.
     * </p>
     */
    @Override
    public Inventory createInventory(Entity entity) {
        return inventories.computeIfAbsent(entity.id(), id -> new InventoryImpl(entity, eventManager));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeInventory(Entity entity) {
        inventories.remove(entity.id());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public java.util.Collection<Entity> getEntitiesWithInventory() {
        java.util.List<Entity> entities = new java.util.ArrayList<>();
        for (Long id : inventories.keySet()) {
            entities.add(new Entity(id));
        }
        return entities;
    }
}
