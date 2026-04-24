package com.rayvion.engine.world;

/**
 * Represents a discrete simulation space within the Rayvion engine.
 *
 * <p>A {@code World} is the top-level container that groups entities together
 * into an isolated logical environment. Multiple worlds can coexist inside the
 * engine simultaneously, each managed by the {@link WorldSystem}. Entities
 * belong to exactly one world at a time, and world-scoped operations (such as
 * physics updates or rendering) are applied only to the entities registered
 * within that world.</p>
 *
 * <p>Implementations must guarantee that {@link #getId()} returns a value that
 * is unique among all currently active worlds tracked by the {@link WorldSystem}.
 * The ID is used as the primary key for world registration and lookup.</p>
 *
 * @see WorldSystem
 */
public interface World {

    /**
     * Returns the unique identifier of this world.
     *
     * <p>The returned ID is used by the {@link WorldSystem} to register, look up,
     * and remove this world, as well as to associate entities with it via
     * {@link WorldSystem#addEntityToWorld(long, long)}. The value must remain
     * constant for the lifetime of the world instance.</p>
     *
     * @return the unique, non-negative long ID that identifies this world
     */
    long getId();
}
