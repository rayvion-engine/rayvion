package com.rayvion.engine.inventory;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemDescriptor;

import java.util.Optional;

/**
 * System for managing entity inventories.
 * <p>
 * This system serves as a central registry and factory for {@link Inventory} instances
 * associated with {@link Entity} objects. It allows for the lifecycle management of
 * inventories, including creation, retrieval, and removal.
 * </p>
 */
public interface InventorySystem extends System {
    /**
     * {@inheritDoc}
     * <p>
     * Returns the descriptor for the inventory system.
     * </p>
     *
     * @return The {@link SystemDescriptor} for this system.
     */
    @Override
    default SystemDescriptor getDescriptor() {
        return SystemDescriptor.fromCoordinate("com.rayvion.engine", "inventory", Version.parse("0.1.0"));
    }

    /**
     * Retrieves the inventory for a given entity.
     * <p>
     * An entity may or may not have an inventory associated with it.
     * </p>
     *
     * @param entity The entity whose inventory is to be retrieved.
     * @return An {@link Optional} containing the {@link Inventory}, or empty if the entity has no inventory.
     */
    Optional<Inventory> getInventory(Entity entity);

    /**
     * Creates an inventory for a given entity if it doesn't already have one.
     * <p>
     * If an inventory already exists for the entity, the existing one is returned.
     * Otherwise, a new inventory is initialized and associated with the entity.
     * </p>
     *
     * @param entity The entity for which to create an inventory.
     * @return The created or existing {@link Inventory}.
     */
    Inventory createInventory(Entity entity);

    /**
     * Removes the inventory associated with a given entity.
     * <p>
     * If the entity has no inventory, this method does nothing.
     * </p>
     *
     * @param entity The entity whose inventory should be removed.
     */
    void removeInventory(Entity entity);

    /**
     * Returns all entities that currently have an inventory managed by this system.
     *
     * @return A collection of {@link Entity} objects that possess an inventory.
     */
    java.util.Collection<Entity> getEntitiesWithInventory();
}
