package com.rayvion.engine.physics.impl;

import com.rayvion.engine.physics.PhysicsBody;
import com.rayvion.engine.physics.PhysicsSystem;
import com.rayvion.engine.system.System;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import com.rayvion.engine.world.WorldSystem;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.world.World;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PhysicsSystemImpl implements PhysicsSystem {
    private final Map<Long, World<Body>> physicsWorlds = new ConcurrentHashMap<>();
    private final Map<Long, Map<Long, Dyn4jBodyWrapper>> worldBodies = new ConcurrentHashMap<>();

    private TransformSystem transformSystem;
    private WorldSystem worldSystem;

    @Override
    public void init() {
    }

    @Override
    public void onDependencyAdded(System dependency) {
        if (dependency instanceof TransformSystem ts) {
            this.transformSystem = ts;
        } if (dependency instanceof WorldSystem ws) {
            this.worldSystem = ws;
        }
    }

    private World<Body> getOrCreateWorld(long worldId) {
        worldBodies.putIfAbsent(worldId, new ConcurrentHashMap<>());
        return physicsWorlds.computeIfAbsent(worldId, id -> {
            World<Body> world = new World<>();
            world.setGravity(World.EARTH_GRAVITY);
            return world;
        });
    }

    @Override
    public PhysicsBody createBoxBody(long worldId, long entityId, double width, double height, boolean isStatic) {
        World<Body> world = getOrCreateWorld(worldId);
        Body body = new Body();
        body.addFixture(Geometry.createRectangle(width, height));
        body.setMass(isStatic ? MassType.INFINITE : MassType.NORMAL);
        
        // Sync initial position from TransformSystem if available
        if (transformSystem != null && transformSystem.hasTransform(entityId)) {
            Transform t = transformSystem.getTransform(entityId);
            body.translate(t.getX(), t.getY());
            body.rotate(t.getRotationZ());
        }

        world.addBody(body);
        Dyn4jBodyWrapper wrapper = new Dyn4jBodyWrapper(body, entityId);
        worldBodies.get(worldId).put(entityId, wrapper);
        return wrapper;
    }

    @Override
    public PhysicsBody createCircleBody(long worldId, long entityId, double radius, boolean isStatic) {
        World<Body> world = getOrCreateWorld(worldId);
        Body body = new Body();
        body.addFixture(Geometry.createCircle(radius));
        body.setMass(isStatic ? MassType.INFINITE : MassType.NORMAL);
        
        // Sync initial position from TransformSystem if available
        if (transformSystem != null && transformSystem.hasTransform(entityId)) {
            Transform t = transformSystem.getTransform(entityId);
            body.translate(t.getX(), t.getY());
            body.rotate(t.getRotationZ());
        }

        world.addBody(body);
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
        Map<Long, Dyn4jBodyWrapper> bodies = worldBodies.get(worldId);
        if (bodies != null) {
            Dyn4jBodyWrapper wrapper = bodies.remove(entityId);
            if (wrapper != null) {
                World<Body> world = physicsWorlds.get(worldId);
                world.removeBody(wrapper.getDyn4jBody());
                return true;
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
            world.update(delta);

            // Sync updated transforms to TransformSystem
            Map<Long, Dyn4jBodyWrapper> bodies = worldBodies.get(worldId);
            if (bodies != null) {
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
                    // Z coordinate and rotationX/Y are purposefully ignored/preserved
                }
            }
        }
    }
}
