package com.rayvion.engine.inventory;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;

import java.util.Collection;
import java.util.Optional;

/**
 * System for managing items laying on the ground as entities.
 * <p>
 * This system handles the spawning of items in the game world, registration of entities
 * as interactable ground items, and proximity-based interaction logic (auto-pickup or manual interaction).
 * </p>
 */
public interface GroundItemSystem extends System {
    /**
     * {@inheritDoc}
     * <p>
     * Returns the descriptor for the ground item system, including its traits.
     * </p>
     *
     * @return The {@link SystemDescriptor} for this system.
     */
    @Override
    default SystemDescriptor getDescriptor() {
        return new SystemDescriptor(
                new SystemCoordinate("com.rayvion.engine", "ground-item", Version.parse("0.1.0")),
                java.util.Set.of(),
                java.util.Set.of(com.rayvion.engine.system.Tickable.TRAIT)
        );
    }

    /**
     * Spawns an item on the ground as a new entity.
     * <p>
     * This creates a new entity, adds it to the specified world, sets its position,
     * and assigns visual/interaction characteristics based on the provided {@link InventoryItem}.
     * </p>
     *
     * @param worldId The ID of the world to spawn the item in.
     * @param item    The item data to associate with the ground entity.
     * @param x       The x-coordinate in world space.
     * @param y       The y-coordinate in world space.
     */
    void dropItem(long worldId, InventoryItem item, double x, double y);

    /**
     * Registers an existing entity as a ground item.
     * <p>
     * This allows external systems to designate an entity as a container for item data.
     * </p>
     *
     * @param entity The entity to register.
     * @param item   The item data to associate with the entity.
     */
    void registerGroundItem(Entity entity, InventoryItem item);

    /**
     * Unregisters a ground item, removing its association with item data.
     *
     * @param entity The entity to unregister.
     */
    void unregisterGroundItem(Entity entity);

    /**
     * Retrieves the item data for a ground item entity.
     *
     * @param entity The entity whose item data is to be retrieved.
     * @return An {@link Optional} containing the item data, or empty if the entity is not a ground item.
     */
    Optional<InventoryItem> getGroundItem(Entity entity);

    /**
     * Returns all entities currently registered as ground items.
     *
     * @return A collection of ground item entities.
     */
    Collection<Entity> getAllGroundItems();

    /**
     * Attempts to interact with nearby ground items for a given entity.
     * <p>
     * Typically called when a player presses an interaction key. It finds the
     * closest interactable (non-auto-pickup) ground item within range and performs a pickup.
     * </p>
     *
     * @param interactor The entity attempting the interaction.
     */
    void tryInteract(Entity interactor);
}
