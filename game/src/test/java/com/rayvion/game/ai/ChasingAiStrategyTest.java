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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ChasingAiStrategyTest {
    private TransformSystem transformSystem;
    private PhysicsSystem physicsSystem;
    private CharacteristicSystem characteristicSystem;
    private ChasingAiStrategy strategy;

    private final long ENTITY_ID = 1L;
    private final long TARGET_ID = 2L;
    private final long WORLD_ID = 100L;

    @BeforeEach
    void setUp() {
        transformSystem = mock(TransformSystem.class);
        physicsSystem = mock(PhysicsSystem.class);
        characteristicSystem = mock(CharacteristicSystem.class);
        strategy = new ChasingAiStrategy(TARGET_ID, WORLD_ID, transformSystem, physicsSystem, characteristicSystem);
    }

    @Test
    void testUpdateWithoutTransforms() {
        when(transformSystem.hasTransform(ENTITY_ID)).thenReturn(false);
        strategy.update(ENTITY_ID);
        verify(physicsSystem, never()).getBody(anyLong(), anyLong());
    }

    @Test
    void testUpdateWithTargetReached() {
        when(transformSystem.hasTransform(ENTITY_ID)).thenReturn(true);
        when(transformSystem.hasTransform(TARGET_ID)).thenReturn(true);
        
        Transform entityTransform = mock(Transform.class);
        when(entityTransform.getX()).thenReturn(100.0);
        when(entityTransform.getY()).thenReturn(100.0);
        when(transformSystem.getTransform(ENTITY_ID)).thenReturn(entityTransform);

        Transform targetTransform = mock(Transform.class);
        when(targetTransform.getX()).thenReturn(102.0); // Distanc = 2 < 5
        when(targetTransform.getY()).thenReturn(100.0);
        when(transformSystem.getTransform(TARGET_ID)).thenReturn(targetTransform);

        PhysicsBody body = mock(PhysicsBody.class);
        when(physicsSystem.getBody(WORLD_ID, ENTITY_ID)).thenReturn(body);

        strategy.update(ENTITY_ID);

        verify(body).setVelocity(0, 0);
    }

    @Test
    void testUpdateChasing() {
        when(transformSystem.hasTransform(ENTITY_ID)).thenReturn(true);
        when(transformSystem.hasTransform(TARGET_ID)).thenReturn(true);
        
        Transform entityTransform = mock(Transform.class);
        when(entityTransform.getX()).thenReturn(100.0);
        when(entityTransform.getY()).thenReturn(100.0);
        when(transformSystem.getTransform(ENTITY_ID)).thenReturn(entityTransform);

        Transform targetTransform = mock(Transform.class);
        when(targetTransform.getX()).thenReturn(130.0); // Distance = 50 > 5
        when(targetTransform.getY()).thenReturn(140.0);
        when(transformSystem.getTransform(TARGET_ID)).thenReturn(targetTransform);

        PhysicsBody body = mock(PhysicsBody.class);
        when(physicsSystem.getBody(WORLD_ID, ENTITY_ID)).thenReturn(body);

        when(characteristicSystem.getValue(any(Entity.class), eq("speed"))).thenReturn(10.0);

        strategy.update(ENTITY_ID);

        // dx = 30, dy = 40, distanc = 50
        // vx = (30/50) * 10 = 6
        // vy = (40/50) * 10 = 8
        verify(body).setVelocity(6.0, 8.0);
        verify(body).setRotation(Math.atan2(40, 30));
    }

    @Test
    void testUpdateWithNullBody() {
        when(transformSystem.hasTransform(ENTITY_ID)).thenReturn(true);
        when(transformSystem.hasTransform(TARGET_ID)).thenReturn(true);
        when(transformSystem.getTransform(ENTITY_ID)).thenReturn(mock(Transform.class));
        when(transformSystem.getTransform(TARGET_ID)).thenReturn(mock(Transform.class));
        
        when(physicsSystem.getBody(WORLD_ID, ENTITY_ID)).thenReturn(null);

        // Should not throw
        strategy.update(ENTITY_ID);
    }

    @Test
    void testUpdateIgnoresTargetOutsideDetectionRange() {
        when(transformSystem.hasTransform(ENTITY_ID)).thenReturn(true);
        when(transformSystem.hasTransform(TARGET_ID)).thenReturn(true);

        Transform entityTransform = mock(Transform.class);
        when(entityTransform.getX()).thenReturn(0.0);
        when(entityTransform.getY()).thenReturn(0.0);
        when(transformSystem.getTransform(ENTITY_ID)).thenReturn(entityTransform);

        Transform targetTransform = mock(Transform.class);
        when(targetTransform.getX()).thenReturn(500.0);
        when(targetTransform.getY()).thenReturn(0.0);
        when(transformSystem.getTransform(TARGET_ID)).thenReturn(targetTransform);

        PhysicsBody body = mock(PhysicsBody.class);
        when(physicsSystem.getBody(WORLD_ID, ENTITY_ID)).thenReturn(body);

        strategy.update(ENTITY_ID);

        verify(body).setVelocity(0, 0);
        verify(characteristicSystem, never()).getValue(any(Entity.class), eq("speed"));
    }
}
