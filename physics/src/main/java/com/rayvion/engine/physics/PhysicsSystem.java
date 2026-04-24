package com.rayvion.engine.physics;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;
import java.util.Set;

/**
 * The Physics System manages the physics simulation within the engine.
 * <p>
 * It provides functionality for creating and managing {@link PhysicsBody} instances in multiple worlds,
 * handling collision detection, and updating the simulation state over time.
 * </p>
 */
public interface PhysicsSystem extends System {
    /**
     * The coordinate defining this system's identity and version.
     */
    SystemTraitCoordinate TRAIT = new SystemTraitCoordinate("com.rayvion.engine", "physics", Version.parse("0.1.0"));

    @Override
    default SystemDescriptor getDescriptor() {
        return new SystemDescriptor(
                new com.rayvion.engine.system.descriptor.SystemCoordinate("com.rayvion.engine", "physics", Version.parse("0.1.0")),
                Set.of(),
                Set.of(TRAIT)
        );
    }

    /**
     * Creates a dynamic or static rectangular physics body associated with an entity.
     * 
     * @param worldId the ID of the world to create the body in
     * @param entityId the ID of the entity associated with this body
     * @param width the width of the box
     * @param height the height of the box
     * @param isStatic true if the body should be static, false for dynamic
     * @return the created {@link PhysicsBody}
     */
    PhysicsBody createBoxBody(long worldId, long entityId, double width, double height, boolean isStatic);

    /**
     * Creates a static rectangular physics body at a specific position, not tied to a specific entity.
     * Useful for world geometry.
     * 
     * @param worldId the ID of the world to create the body in
     * @param x the x-coordinate of the box center
     * @param y the y-coordinate of the box center
     * @param width the width of the box
     * @param height the height of the box
     * @return the created {@link PhysicsBody}
     */
    PhysicsBody createStaticBoxBody(long worldId, double x, double y, double width, double height);

    /**
     * Creates a dynamic or static circular physics body associated with an entity.
     * 
     * @param worldId the ID of the world to create the body in
     * @param entityId the ID of the entity associated with this body
     * @param radius the radius of the circle
     * @param isStatic true if the body should be static, false for dynamic
     * @return the created {@link PhysicsBody}
     */
    PhysicsBody createCircleBody(long worldId, long entityId, double radius, boolean isStatic);

    /**
     * Retrieves the physics body associated with an entity in a given world.
     * 
     * @param worldId the ID of the world
     * @param entityId the ID of the entity
     * @return the {@link PhysicsBody} or null if not found
     */
    PhysicsBody getBody(long worldId, long entityId);
    
    /**
     * Removes a physics body from the given world.
     * 
     * @param worldId the ID of the world
     * @param entityId the ID of the entity whose body should be removed
     * @return true if the body was successfully removed, false otherwise
     */
    boolean removeBody(long worldId, long entityId);
    
    /**
     * Checks if a point is blocked by a static physics body in the given world.
     * 
     * @param worldId the ID of the world
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return true if the point is blocked, false otherwise
     */
    boolean isPointBlocked(long worldId, double x, double y);

    /**
     * Updates the physics simulation for all worlds.
     * 
     * @param delta the time elapsed since the last update in seconds
     */
    void update(double delta);
}
