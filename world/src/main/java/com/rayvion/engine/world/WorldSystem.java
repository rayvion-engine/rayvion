package com.rayvion.engine.world;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;

import java.util.Collection;

/**
 * Engine system responsible for managing the lifecycle of {@link World} instances
 * and the membership of entities within those worlds.
 *
 * <p>{@code WorldSystem} acts as the central registry for all active worlds inside
 * the Rayvion engine. It provides operations to add and remove worlds, retrieve a
 * world by its ID, and manage the set of entities that belong to each world.
 * Multiple worlds can be active at the same time, allowing the engine to run
 * isolated simulation environments in parallel (e.g. a game world and a UI world).</p>
 *
 * <p>This system advertises itself through the {@link #TRAIT} coordinate so that
 * other systems can declare a dependency on world management capabilities without
 * coupling to a specific implementation. The default {@link #getDescriptor()}
 * implementation publishes these coordinates automatically.</p>
 *
 * <p>Implementations must be thread-safe when worlds or entities are added and
 * removed concurrently.</p>
 *
 * @see World
 * @see com.rayvion.engine.world.impl.WorldSystemImpl
 */
public interface WorldSystem extends System {

    /**
     * The {@link SystemTraitCoordinate} that identifies the world-management
     * capability provided by this system.
     *
     * <p>Other engine systems that need to interact with world data should declare
     * a dependency on this trait rather than on a concrete implementation class.
     * The coordinate is defined under the {@code com.rayvion.engine} group,
     * artifact {@code world}, at version {@code 0.1.0}.</p>
     */
    SystemTraitCoordinate TRAIT = new SystemTraitCoordinate("com.rayvion.engine", "world", Version.parse("0.1.0"));

    /**
     * Returns the {@link SystemDescriptor} for this system.
     *
     * <p>The default descriptor declares no dependencies on other systems and
     * exposes the {@link #TRAIT} coordinate, allowing other systems to discover
     * and depend on world-management functionality without knowing the concrete
     * implementation. The system coordinate matches the {@link #TRAIT} coordinate
     * ({@code com.rayvion.engine:world:0.1.0}).</p>
     *
     * @return the descriptor describing this system's identity and capabilities
     */
    @Override
    default SystemDescriptor getDescriptor() {
        return new SystemDescriptor(
                new com.rayvion.engine.system.descriptor.SystemCoordinate("com.rayvion.engine", "world", Version.parse("0.1.0")),
                java.util.Set.of(),
                java.util.Set.of(TRAIT)
        );
    }

    /**
     * Registers a world with this system.
     *
     * <p>After this call the world is retrievable via {@link #getWorld(long)} and
     * an empty entity set is created for it so that entities can be associated
     * immediately with {@link #addEntityToWorld(long, long)}. If a world with the
     * same ID was previously registered, its entry is replaced and the existing
     * entity set may be reset.</p>
     *
     * @param world the world to register; must not be {@code null}
     */
    void addWorld(World world);

    /**
     * Removes the world with the given ID from this system.
     *
     * <p>Removing a world also discards the entire entity set that was associated
     * with it. After this call, any entities that were members of the world are
     * no longer tracked by this system (but remain registered with the entity
     * system).</p>
     *
     * @param worldId the unique identifier of the world to remove
     * @return {@code true} if a world with the given ID existed and was removed;
     *         {@code false} if no such world was registered
     */
    boolean removeWorld(long worldId);

    /**
     * Returns the world registered under the given ID.
     *
     * @param worldId the unique identifier of the world to retrieve
     * @return the {@link World} associated with {@code worldId}, or {@code null}
     *         if no world with that ID has been registered
     */
    World getWorld(long worldId);

    /**
     * Associates an entity with a world.
     *
     * <p>The entity identified by {@code entityId} is added to the membership
     * set of the world identified by {@code worldId}. If no world with
     * {@code worldId} is registered this operation is silently ignored; no
     * exception is thrown.</p>
     *
     * @param worldId  the unique identifier of the target world
     * @param entityId the unique identifier of the entity to add to the world
     */
    void addEntityToWorld(long worldId, long entityId);

    /**
     * Removes an entity from a world.
     *
     * <p>The entity identified by {@code entityId} is removed from the membership
     * set of the world identified by {@code worldId}. If either the world does
     * not exist or the entity was not a member of that world, {@code false} is
     * returned and no state is changed.</p>
     *
     * @param worldId  the unique identifier of the world from which the entity
     *                 should be removed
     * @param entityId the unique identifier of the entity to remove
     * @return {@code true} if the entity was a member of the world and was
     *         successfully removed; {@code false} otherwise
     */
    boolean removeEntityFromWorld(long worldId, long entityId);

    /**
     * Returns an unmodifiable view of all entity IDs that belong to a world.
     *
     * <p>The returned collection reflects the current membership at the time of
     * the call. If the world does not exist, an empty collection is returned.
     * Callers must not attempt to modify the returned collection.</p>
     *
     * @param worldId the unique identifier of the world whose entity list is
     *                requested
     * @return an unmodifiable {@link Collection} of entity IDs belonging to the
     *         specified world, or an empty collection if the world is unknown
     */
    Collection<Long> getEntities(long worldId);
}
