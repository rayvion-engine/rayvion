package com.rayvion.engine.physics.impl;

import com.rayvion.engine.physics.PhysicsBody;
import com.rayvion.engine.physics.PhysicsSystem;
import com.rayvion.engine.system.System;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import com.rayvion.engine.world.WorldSystem;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;

import com.rayvion.engine.system.Tickable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link PhysicsSystem} using the Dyn4j physics engine.
 * <p>
 * This system manages multiple physics worlds and synchronizes body transforms
 * with the {@link TransformSystem}.
 * </p>
 */
@Slf4j
public class PhysicsSystemImpl implements PhysicsSystem, Tickable {
    /**
     * Map of world IDs to their corresponding Dyn4j worlds.
     */
    private final Map<Long, World<Body>> physicsWorlds = new ConcurrentHashMap<>();

    /**
     * Map of world IDs to a map of entity IDs and their physics body wrappers.
     */
    private final Map<Long, Map<Long, Dyn4jBodyWrapper>> worldBodies = new ConcurrentHashMap<>();

    /**
     * Reference to the TransformSystem for synchronizing positions.
     */
    private TransformSystem transformSystem;

    @Override
    public com.rayvion.engine.system.descriptor.SystemDescriptor getDescriptor() {
        return new com.rayvion.engine.system.descriptor.SystemDescriptor(
                new com.rayvion.engine.system.descriptor.SystemCoordinate("com.rayvion.engine", "physics", com.github.zafarkhaja.semver.Version.parse("0.1.0")),
                java.util.Set.of(
                        new com.rayvion.engine.system.dependency.SystemDependency(
                                new com.rayvion.engine.system.trait.SystemTraitRequirement(
                                        "com.rayvion.engine",
                                        "transform",
                                        v -> v.equals(com.github.zafarkhaja.semver.Version.parse("0.1.0"))
                                ),
                                com.rayvion.engine.system.dependency.SystemDependency.RequirementLevel.OPTIONAL
                        )
                ),
                java.util.Set.of(Tickable.TRAIT, PhysicsSystem.TRAIT)
        );
    }

    @Override
    public void init() {
    }

    /**
     * Ticks the physics simulation.
     */
    @Override
    public void tick() {
//        log.debug("PhysicsSystem: Ticking...");
        update(getTickDelay().toMillis() / 1000.0);
    }

    @Override
    public void onDependencyAdded(System dependency) {
        if (dependency instanceof TransformSystem ts) {
            this.transformSystem = ts;
        } if (dependency instanceof WorldSystem ws) {
        }
    }

    /**
     * Gets an existing physics world or creates a new one if it doesn't exist.
     * 
     * @param worldId the ID of the world
     * @return the Dyn4j world
     */
    private World<Body> getOrCreateWorld(long worldId) {
        worldBodies.putIfAbsent(worldId, new ConcurrentHashMap<>());
        return physicsWorlds.computeIfAbsent(worldId, id -> {
            World<Body> world = new World<>();
            world.setGravity(new org.dyn4j.geometry.Vector2(0, 0));
            return world;
        });
    }

    @Override
    public PhysicsBody createBoxBody(long worldId, long entityId, double width, double height, boolean isStatic) {
        World<Body> world = getOrCreateWorld(worldId);
        Body body = new Body();
        body.addFixture(Geometry.createRectangle(width, height));
        body.setMass(isStatic ? MassType.INFINITE : MassType.FIXED_ANGULAR_VELOCITY);
        
        // Sync initial position from TransformSystem if available
        if (transformSystem != null && transformSystem.hasTransform(entityId)) {
            Transform t = transformSystem.getTransform(entityId);
            body.getTransform().setTranslation(t.getX(), t.getY());
            body.getTransform().setRotation(t.getRotationZ());
        }

        synchronized (world) {
            world.addBody(body);
        }
        Dyn4jBodyWrapper wrapper = new Dyn4jBodyWrapper(body, entityId);
        worldBodies.get(worldId).put(entityId, wrapper);
        return wrapper;
    }

    @Override
    public PhysicsBody createStaticBoxBody(long worldId, double x, double y, double width, double height) {
        World<Body> world = getOrCreateWorld(worldId);
        Body body = new Body();
        body.addFixture(Geometry.createRectangle(width, height));
        body.setMass(MassType.INFINITE);
        body.translate(x, y);

        synchronized (world) {
            world.addBody(body);
        }
        // We use -1 as entityId for static world geometry that isn't tied to an entity
        return new Dyn4jBodyWrapper(body, -1);
    }

    @Override
    public PhysicsBody createCircleBody(long worldId, long entityId, double radius, boolean isStatic) {
        World<Body> world = getOrCreateWorld(worldId);
        Body body = new Body();
        body.addFixture(Geometry.createCircle(radius));
        body.setMass(isStatic ? MassType.INFINITE : MassType.FIXED_ANGULAR_VELOCITY);
        
        // Sync initial position from TransformSystem if available
        if (transformSystem != null && transformSystem.hasTransform(entityId)) {
            Transform t = transformSystem.getTransform(entityId);
            body.getTransform().setTranslation(t.getX(), t.getY());
            body.getTransform().setRotation(t.getRotationZ());
        }

        synchronized (world) {
            world.addBody(body);
        }
        Dyn4jBodyWrapper wrapper = new Dyn4jBodyWrapper(body, entityId);
        worldBodies.get(worldId).put(entityId, wrapper);
        return wrapper;
    }

    @Override
    public PhysicsBody getBody(long worldId, long entityId) {
        Map<Long, Dyn4jBodyWrapper> bodies = worldBodies.get(worldId);
        if (bodies != null) {
            return bodies.get(entityId);
        }
        return null;
    }

    @Override
    public boolean removeBody(long worldId, long entityId) {
        log.info("PhysicsSystem: removeBody called for world {} entity {}", worldId, entityId);
        Map<Long, Dyn4jBodyWrapper> bodies = worldBodies.get(worldId);
        if (bodies != null) {
            Dyn4jBodyWrapper wrapper = bodies.remove(entityId);
            if (wrapper != null) {
                World<Body> world = physicsWorlds.get(worldId);
                if (world != null) {
                    synchronized (world) {
                        world.removeBody(wrapper.getDyn4jBody());
                    }
                    log.info("PhysicsSystem: Successfully removed body for entity {} from world {}", entityId, worldId);
                    return true;
                } else {
                    log.error("PhysicsSystem: World {} not found for removing body of entity {}", worldId, entityId);
                }
            } else {
                log.warn("PhysicsSystem: No body wrapper found for entity {} in world {}", entityId, worldId);
            }
        } else {
            log.warn("PhysicsSystem: No bodies map found for world {}", worldId);
        }
        return false;
    }

    @Override
    public boolean isPointBlocked(long worldId, double x, double y) {
        World<Body> world = physicsWorlds.get(worldId);
        if (world == null) return false;

        Vector2 point = new Vector2(x, y);
        synchronized (world) {
            for (Body body : world.getBodies()) {
                if (body.getMass().isInfinite()) { // Static body
                    for (BodyFixture fixture : body.getFixtures()) {
                        if (fixture.getShape().contains(point, body.getTransform())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void update(double delta) {
        if (transformSystem == null) return;

        for (Map.Entry<Long, World<Body>> entry : physicsWorlds.entrySet()) {
            long worldId = entry.getKey();
            World<Body> world = entry.getValue();
            
            // Step physics simulation
            synchronized (world) {
                world.update(delta);
            }

            // Sync updated transforms to TransformSystem
            Map<Long, Dyn4jBodyWrapper> bodies = worldBodies.get(worldId);
            if (bodies != null) {
//                log.debug("PhysicsSystem: Syncing {} bodies for world {}", bodies.size(), worldId);
                for (Dyn4jBodyWrapper wrapper : bodies.values()) {
                    long entityId = wrapper.getEntityId();
                    Body body = wrapper.getDyn4jBody();
                    
                    Transform t = transformSystem.getTransform(entityId);
                    if (t == null) {
                        t = new Transform();
                        transformSystem.setTransform(entityId, t);
                    }
                    
                    t.setX(body.getTransform().getTranslationX());
                    t.setY(body.getTransform().getTranslationY());
                    t.setRotationZ(body.getTransform().getRotationAngle());
                    log.trace("PhysicsSystem: Entity {} position: {}, {}", entityId, t.getX(), t.getY());
                    // Z coordinate and rotationX/Y are purposefully ignored/preserved
                }
            }
        }
    }
}
