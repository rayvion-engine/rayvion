package com.rayvion.game.ai;

import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.physics.PhysicsBody;
import com.rayvion.engine.physics.PhysicsSystem;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PathfindingAiStrategyTest {
    private TransformSystem transformSystem;
    private PhysicsSystem physicsSystem;
    private CharacteristicSystem characteristicSystem;
    private PathfindingAiStrategy strategy;

    private final long ENTITY_ID = 1L;
    private final long TARGET_ID = 2L;
    private final long WORLD_ID = 100L;
    private final double TILE_SIZE = 32.0;
    private final int GRID_WIDTH = 10;
    private final int GRID_HEIGHT = 10;

    @BeforeEach
    void setUp() {
        transformSystem = mock(TransformSystem.class);
        physicsSystem = mock(PhysicsSystem.class);
        characteristicSystem = mock(CharacteristicSystem.class);
        when(characteristicSystem.getValue(any(Entity.class), anyString())).thenReturn(10.0);
        strategy = new PathfindingAiStrategy(TARGET_ID, WORLD_ID, transformSystem, physicsSystem, characteristicSystem, TILE_SIZE, GRID_WIDTH, GRID_HEIGHT);
    }

    @Test
    void testUpdateWithoutTransforms() {
        when(transformSystem.hasTransform(ENTITY_ID)).thenReturn(false);
        strategy.update(ENTITY_ID);
        verify(physicsSystem, never()).getBody(anyLong(), anyLong());
    }

    @Test
    void testUpdateSameTile() {
        when(transformSystem.hasTransform(ENTITY_ID)).thenReturn(true);
        when(transformSystem.hasTransform(TARGET_ID)).thenReturn(true);
        
        Transform entityTransform = mock(Transform.class);
        when(entityTransform.getX()).thenReturn(48.0); // Tile (1, 1)
        when(entityTransform.getY()).thenReturn(48.0);
        when(transformSystem.getTransform(ENTITY_ID)).thenReturn(entityTransform);

        Transform targetTransform = mock(Transform.class);
        when(targetTransform.getX()).thenReturn(50.0); // Tile (1, 1)
        when(targetTransform.getY()).thenReturn(50.0);
        when(transformSystem.getTransform(TARGET_ID)).thenReturn(targetTransform);

        PhysicsBody body = mock(PhysicsBody.class);
        when(physicsSystem.getBody(WORLD_ID, ENTITY_ID)).thenReturn(body);

        strategy.update(ENTITY_ID);

        // Path should be just the target tile. Waypoint reached logic should trigger.
        verify(body).setVelocity(0, 0);
    }

    @Test
    void testRecalculatePathFound() {
        when(transformSystem.hasTransform(ENTITY_ID)).thenReturn(true);
        when(transformSystem.hasTransform(TARGET_ID)).thenReturn(true);
        
        when(transformSystem.getTransform(ENTITY_ID)).thenReturn(new Transform(32, 32, 0)); // (1, 1)
        when(transformSystem.getTransform(TARGET_ID)).thenReturn(new Transform(96, 32, 0)); // (3, 1)

        PhysicsBody body = mock(PhysicsBody.class);
        when(physicsSystem.getBody(WORLD_ID, ENTITY_ID)).thenReturn(body);
        when(characteristicSystem.getValue(any(Entity.class), eq("speed"))).thenReturn(10.0);

        // Grid (2, 1) is NOT blocked
        when(physicsSystem.isPointBlocked(eq(WORLD_ID), anyDouble(), anyDouble())).thenReturn(false);

        strategy.update(ENTITY_ID);

        // Should move towards (2, 1) which is center 80, 48
        // start (32, 32)
        // dx = 80 - 32 = 48
        // dy = 48 - 32 = 16
        // distance = sqrt(48^2 + 16^2)
        verify(body).setVelocity(anyDouble(), anyDouble());
    }

    @Test
    void testNoPathFound() {
        when(transformSystem.hasTransform(ENTITY_ID)).thenReturn(true);
        when(transformSystem.hasTransform(TARGET_ID)).thenReturn(true);
        
        when(transformSystem.getTransform(ENTITY_ID)).thenReturn(new Transform(32, 32, 0)); // (1, 1)
        when(transformSystem.getTransform(TARGET_ID)).thenReturn(new Transform(96, 32, 0)); // (3, 1)

        PhysicsBody body = mock(PhysicsBody.class);
        when(physicsSystem.getBody(WORLD_ID, ENTITY_ID)).thenReturn(body);

        // Surround the enemy with walls
        when(physicsSystem.isPointBlocked(eq(WORLD_ID), anyDouble(), anyDouble())).thenReturn(true);

        strategy.update(ENTITY_ID);

        verify(body).setVelocity(0, 0);
    }

    @Test
    void testTargetBlocked() {
        when(transformSystem.hasTransform(ENTITY_ID)).thenReturn(true);
        when(transformSystem.hasTransform(TARGET_ID)).thenReturn(true);
        
        when(transformSystem.getTransform(ENTITY_ID)).thenReturn(new Transform(32, 32, 0));
        when(transformSystem.getTransform(TARGET_ID)).thenReturn(new Transform(96, 32, 0));

        PhysicsBody body = mock(PhysicsBody.class);
        when(physicsSystem.getBody(WORLD_ID, ENTITY_ID)).thenReturn(body);

        // Mark target tile as blocked
        when(physicsSystem.isPointBlocked(WORLD_ID, 96.0 + 16.0, 32.0 + 16.0)).thenReturn(true);

        strategy.update(ENTITY_ID);

        verify(body).setVelocity(0, 0);
    }

    @Test
    void testClamping() {
        when(transformSystem.hasTransform(ENTITY_ID)).thenReturn(true);
        when(transformSystem.hasTransform(TARGET_ID)).thenReturn(true);
        
        // Way out of bounds
        when(transformSystem.getTransform(ENTITY_ID)).thenReturn(new Transform(-100, -100, 0));
        when(transformSystem.getTransform(TARGET_ID)).thenReturn(new Transform(1000, 1000, 0));

        PhysicsBody body = mock(PhysicsBody.class);
        when(physicsSystem.getBody(WORLD_ID, ENTITY_ID)).thenReturn(body);

        assertDoesNotThrow(() -> strategy.update(ENTITY_ID));
    }

    @Test
    void testUpdateRecalculateAfterInterval() throws InterruptedException {
        when(transformSystem.hasTransform(ENTITY_ID)).thenReturn(true);
        when(transformSystem.hasTransform(TARGET_ID)).thenReturn(true);
        when(transformSystem.getTransform(ENTITY_ID)).thenReturn(new Transform(32, 32, 0));
        when(transformSystem.getTransform(TARGET_ID)).thenReturn(new Transform(64, 64, 0));
        
        strategy.update(ENTITY_ID); // First update, recalculates
        
        // Update again immediately, should NOT recalculate (isBlocked won't be called again for all nodes)
        reset(physicsSystem);
        strategy.update(ENTITY_ID);
        verify(physicsSystem, never()).isPointBlocked(anyLong(), anyDouble(), anyDouble());
        
        // Wait for interval
        Thread.sleep(501);
        strategy.update(ENTITY_ID);
        // Should recalculate now
        verify(physicsSystem, atLeastOnce()).isPointBlocked(anyLong(), anyDouble(), anyDouble());
    }

    @Test
    void testUpdateStopsWhenTargetOutsideDetectionRange() {
        when(transformSystem.hasTransform(ENTITY_ID)).thenReturn(true);
        when(transformSystem.hasTransform(TARGET_ID)).thenReturn(true);
        when(transformSystem.getTransform(ENTITY_ID)).thenReturn(new Transform(0, 0, 0));
        when(transformSystem.getTransform(TARGET_ID)).thenReturn(new Transform(500, 0, 0));

        PhysicsBody body = mock(PhysicsBody.class);
        when(physicsSystem.getBody(WORLD_ID, ENTITY_ID)).thenReturn(body);

        strategy.update(ENTITY_ID);

        verify(body).setVelocity(0, 0);
        verify(physicsSystem, never()).isPointBlocked(anyLong(), anyDouble(), anyDouble());
    }
}
