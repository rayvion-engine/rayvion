package com.rayvion.engine.physics.impl;

import com.rayvion.engine.physics.PhysicsBody;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.impl.TransformSystemImpl;
import com.rayvion.engine.world.World;
import com.rayvion.engine.world.impl.WorldSystemImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void testPhysicsUpdateSyncsTransform() {
        long worldId = 1L;
        long entityId = 100L;

        worldSystem.addWorld(() -> worldId);
        worldSystem.addEntityToWorld(worldId, entityId);

        Transform initialTransform = new Transform(0, 10, 5);
        transformSystem.setTransform(entityId, initialTransform);

        // Create a dynamic body that will fall due to gravity
        PhysicsBody body = physicsSystem.createBoxBody(worldId, entityId, 1.0, 1.0, false);
        
        // Ensure initial position is synced from Transform
        assertEquals(10.0, transformSystem.getTransform(entityId).getY(), 0.001);

        // Step physics forward by 1 second
        physicsSystem.update(1.0);

        Transform updatedTransform = transformSystem.getTransform(entityId);
        assertTrue(updatedTransform.getY() != 10.0, "Y position should have changed due to gravity");
        
        // Z should remain unchanged
        assertEquals(5.0, updatedTransform.getZ(), 0.001, "Z coordinate should be ignored and preserved by 2D physics");
    }
}
