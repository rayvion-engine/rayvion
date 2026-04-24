package com.rayvion.engine.physics.impl;

import com.rayvion.engine.physics.PhysicsBody;
import com.rayvion.engine.physics.PhysicsSystem;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.impl.TransformSystemImpl;
import com.rayvion.engine.world.impl.WorldSystemImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class PhysicsSystemImplTest {
    private TransformSystemImpl transformSystem;
    private WorldSystemImpl worldSystem;
    private PhysicsSystemImpl physicsSystem;

    @BeforeEach
    void setUp() {
        transformSystem = new TransformSystemImpl();
        worldSystem = new WorldSystemImpl();
        physicsSystem = new PhysicsSystemImpl();

        physicsSystem.onDependencyAdded(transformSystem);
        physicsSystem.onDependencyAdded(worldSystem);
    }

    @Test
    void testGetDescriptor() {
        SystemDescriptor descriptor = physicsSystem.getDescriptor();
        assertNotNull(descriptor);
        assertEquals("physics", descriptor.coordinate().id());
        assertTrue(descriptor.provides().contains(PhysicsSystem.TRAIT));
    }

    @Test
    void testInit() {
        assertDoesNotThrow(() -> physicsSystem.init());
    }

    @Test
    void testTick() {
        // PhysicsSystemImpl.tick() calls update() with getTickDelay()
        // We ned to make sure it dosn't crash
        physicsSystem.onDependencyAdded(transformSystem);
        assertDoesNotThrow(() -> physicsSystem.tick());
    }

    @Test
    void testOnDependencyAdded() {
        PhysicsSystemImpl ps = new PhysicsSystemImpl();
        ps.onDependencyAdded(transformSystem);
        ps.onDependencyAdded(worldSystem);
        ps.onDependencyAdded(new System() {
            @Override
            public SystemDescriptor getDescriptor() {
                return null;
            }
            @Override
            public void init() {}
        });
        // Check if transformSystem is set (indirectly via createBoxBody)
        long entityId = 1L;
        transformSystem.setTransform(entityId, new Transform(10, 20, 0));
        PhysicsBody body = ps.createBoxBody(1L, entityId, 1.0, 1.0, false);
        // Dyn4jBodyWrapper doesn't expose position directly, but we can check if it's used in update
        ps.update(0.1);
        assertEquals(10, transformSystem.getTransform(entityId).getX());
    }

    @Test
    void testCreateBoxBody() {
        long worldId = 1L;
        long entityId = 100L;

        // Dynamic body
        PhysicsBody body = physicsSystem.createBoxBody(worldId, entityId, 1.0, 1.0, false);
        assertNotNull(body);
        assertFalse(body.isStatic());
        assertEquals(entityId, ((Dyn4jBodyWrapper)body).getEntityId());

        // Static body
        PhysicsBody staticBody = physicsSystem.createBoxBody(worldId, 101L, 1.0, 1.0, true);
        assertTrue(staticBody.isStatic());
    }

    @Test
    void testCreateBoxBodyWithInitialTransform() {
        long worldId = 1L;
        long entityId = 100L;
        Transform initial = new Transform(10, 20, 0);
        initial.setRotationZ(1.5);
        transformSystem.setTransform(entityId, initial);

        PhysicsBody body = physicsSystem.createBoxBody(worldId, entityId, 1.0, 1.0, false);
        
        // Update physics and check if it started at (10, 20)
        physicsSystem.update(0.1);
        Transform t = transformSystem.getTransform(entityId);
        assertEquals(10, t.getX(), 0.001, "Expected X to be 10 but was " + t.getX());
        assertEquals(20, t.getY(), 0.001, "Expected Y to be 20 but was " + t.getY());
        assertEquals(1.5, t.getRotationZ(), 0.001, "Expected rotationZ to be 1.5 but was " + t.getRotationZ());
    }

    @Test
    void testCreateStaticBoxBody() {
        PhysicsBody body = physicsSystem.createStaticBoxBody(1L, 50, 60, 10, 10);
        assertTrue(body.isStatic());
        assertEquals(-1, ((Dyn4jBodyWrapper)body).getEntityId());
        
        assertTrue(physicsSystem.isPointBlocked(1L, 50, 60));
        assertFalse(physicsSystem.isPointBlocked(1L, 100, 100));
    }

    @Test
    void testCreateCircleBody() {
        long worldId = 1L;
        long entityId = 100L;
        transformSystem.setTransform(entityId, new Transform(5, 5, 0));

        PhysicsBody body = physicsSystem.createCircleBody(worldId, entityId, 2.0, false);
        assertNotNull(body);
        assertFalse(body.isStatic());

        physicsSystem.update(0);
        assertEquals(5, transformSystem.getTransform(entityId).getX(), 0.001);
    }

    @Test
    void testGetBody() {
        long worldId = 1L;
        long entityId = 100L;
        physicsSystem.createBoxBody(worldId, entityId, 1, 1, false);

        assertNotNull(physicsSystem.getBody(worldId, entityId));
        assertNull(physicsSystem.getBody(worldId, 999L));
        assertNull(physicsSystem.getBody(999L, entityId));
    }

    @Test
    void testRemoveBody() {
        long worldId = 1L;
        long entityId = 100L;
        physicsSystem.createBoxBody(worldId, entityId, 1, 1, false);

        assertTrue(physicsSystem.removeBody(worldId, entityId));
        assertNull(physicsSystem.getBody(worldId, entityId));

        // Test non-existent entity
        assertFalse(physicsSystem.removeBody(worldId, 999L));
        
        // Test non-existent world map
        assertFalse(physicsSystem.removeBody(999L, entityId));
    }

    @Test
    void testIsPointBlocked() {
        long worldId = 1L;
        physicsSystem.createStaticBoxBody(worldId, 0, 0, 10, 10);
        physicsSystem.createBoxBody(worldId, 200L, 1, 1, false); // Dynamic body

        assertTrue(physicsSystem.isPointBlocked(worldId, 0, 0));
        assertFalse(physicsSystem.isPointBlocked(worldId, 20, 20)); 
        assertFalse(physicsSystem.isPointBlocked(999L, 0, 0)); // Non-existent world
    }

    @Test
    void testIsPointBlockedEdgeCases() {
        long worldId = 10L;
        // Point in world with no bodies
        assertFalse(physicsSystem.isPointBlocked(worldId, 0, 0));

        // Point in world with only dynamic bodies
        physicsSystem.createBoxBody(worldId, 1L, 1, 1, false);
        assertFalse(physicsSystem.isPointBlocked(worldId, 0, 0));

        // Point in world with static body but outside fixture
        physicsSystem.createStaticBoxBody(worldId, 10, 10, 1, 1);
        assertFalse(physicsSystem.isPointBlocked(worldId, 0, 0));
    }

    @Test
    void testUpdateWithoutTransformSystem() {
        PhysicsSystemImpl ps = new PhysicsSystemImpl();
        // Shoud retun early
        assertDoesNotThrow(() -> ps.update(0.1));
    }

    @Test
    void testUpdateSyncsNewTransform() {
        long worldId = 1L;
        long entityId = 100L;
        physicsSystem.createBoxBody(worldId, entityId, 1, 1, false);
        
        physicsSystem.update(0.1);
        assertNotNull(transformSystem.getTransform(entityId));
    }

    @Test
    void testPhysicsUpdateSyncsTransform() {
        long worldId = 1L;
        long entityId = 100L;

        Transform initialTransform = new Transform(0, 10, 5);
        transformSystem.setTransform(entityId, initialTransform);

        PhysicsBody body = physicsSystem.createBoxBody(worldId, entityId, 1.0, 1.0, false);
        body.setVelocity(0, -10);
        
        assertEquals(10.0, transformSystem.getTransform(entityId).getY(), 0.001);

        physicsSystem.update(1.0);

        Transform updatedTransform = transformSystem.getTransform(entityId);
        assertNotEquals(10.0, updatedTransform.getY(), "Y position should have changed");
        assertEquals(5.0, updatedTransform.getZ(), 0.001, "Z coordinate should be preserved");
    }
}



