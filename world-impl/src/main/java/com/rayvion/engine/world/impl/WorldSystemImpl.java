package com.rayvion.engine.world.impl;

import com.rayvion.engine.world.World;
import com.rayvion.engine.world.WorldSystem;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default, thread-safe implementation of {@link WorldSystem}.
 *
 * <p>{@code WorldSystemImpl} maintains two concurrent maps:
 * <ul>
 *   <li><b>worlds</b> – maps a world ID to its {@link World} instance.</li>
 *   <li><b>worldEntities</b> – maps a world ID to the concurrent set of entity
 *       IDs that are currently members of that world.</li>
 * </ul>
 * Both maps are backed by {@link ConcurrentHashMap}, making all read and write
 * operations safe for concurrent use without additional synchronisation.</p>
 *
 * <p>This class is registered as the concrete provider of the
 * {@link WorldSystem#TRAIT} capability. It is intended to be instantiated once
 * per engine context and registered with the system manager before any other
 * system that depends on world management is initialised.</p>
 *
 * @see WorldSystem
 * @see World
 */
public class WorldSystemImpl implements WorldSystem {

    /**
     * Stores the registered {@link World} instances keyed by their ID.
     * Access is thread-safe via {@link ConcurrentHashMap}.
     */
    private final Map<Long, World> worlds = new ConcurrentHashMap<>();

    /**
     * Stores the set of entity IDs belonging to each world, keyed by world ID.
     * Each value set is a concurrent key-set derived from a {@link ConcurrentHashMap},
     * allowing safe concurrent additions and removals of entity memberships.
     */
    private final Map<Long, Set<Long>> worldEntities = new ConcurrentHashMap<>();

    /**
     * Initialises this system.
     *
     * <p>No initialisation work is required for this implementation; both internal
     * maps are ready for use immediately after construction. This method is
     * provided to satisfy the {@link com.rayvion.engine.system.System} contract.</p>
     */
    @Override
    public void init() {
    }

    /**
     * {@inheritDoc}
     *
     * <p>Stores the world in the internal registry and, if no entity set already
     * exists for the world's ID, creates an empty concurrent set for it.
     * If a world with the same ID was previously registered the old entry is
     * silently replaced, but the existing entity set is preserved via
     * {@link Map#putIfAbsent}.</p>
     */
    @Override
    public void addWorld(World world) {
        worlds.put(world.getId(), world);
        worldEntities.putIfAbsent(world.getId(), ConcurrentHashMap.newKeySet());
    }

    /**
     * {@inheritDoc}
     *
     * <p>The entity set for the world is removed first to ensure that no orphaned
     * entity data remains if another thread attempts to add an entity between the
     * two map operations.</p>
     */
    @Override
    public boolean removeWorld(long worldId) {
        worldEntities.remove(worldId);
        return worlds.remove(worldId) != null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates directly to the internal worlds map. Returns {@code null} if
     * no world with the given ID has been registered.</p>
     */
    @Override
    public World getWorld(long worldId) {
        return worlds.get(worldId);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Looks up the entity set for the specified world and adds {@code entityId}
     * to it. If {@code worldId} does not correspond to a registered world, the
     * entity set will be {@code null} and the operation is silently skipped —
     * no exception is thrown and no state is modified.</p>
     */
    @Override
    public void addEntityToWorld(long worldId, long entityId) {
        Set<Long> entities = worldEntities.get(worldId);
        if (entities != null) {
            entities.add(entityId);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Looks up the entity set for the specified world and attempts to remove
     * {@code entityId} from it. If the world does not exist {@code false} is
     * returned immediately. The removal delegates to
     * {@link Set#remove(Object)} on the concurrent set, which returns
     * {@code true} only when the element was present.</p>
     */
    @Override
    public boolean removeEntityFromWorld(long worldId, long entityId) {
        Set<Long> entities = worldEntities.get(worldId);
        if (entities != null) {
            return entities.remove(entityId);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Wraps the internal concurrent entity set in an unmodifiable view via
     * {@link Collections#unmodifiableSet}. If no world with the given ID is
     * registered, {@link Collections#emptySet()} is returned instead of
     * {@code null}, allowing callers to iterate the result safely without a
     * {@code null} check.</p>
     */
    @Override
    public Collection<Long> getEntities(long worldId) {
        Set<Long> entities = worldEntities.get(worldId);
        if (entities == null) return Collections.emptySet();
        return Collections.unmodifiableSet(entities);
    }
}
