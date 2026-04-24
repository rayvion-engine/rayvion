package com.rayvion.game.ai;

import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.physics.PhysicsBody;
import com.rayvion.engine.physics.PhysicsSystem;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import com.rayvion.game.combat.EntityAttackEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MeleeAiStrategyTest {
    private TransformSystem transformSystem;
    private PhysicsSystem physicsSystem;
    private CharacteristicSystem characteristicSystem;
    private EventManager eventManager;
    private MeleeAiStrategy strategy;

    private final long ENTITY_ID = 1L;
    private final long TARGET_ID = 2L;
    private final long WORLD_ID = 100L;

    @BeforeEach
    void setUp() {
        transformSystem = mock(TransformSystem.class);
        physicsSystem = mock(PhysicsSystem.class);
        characteristicSystem = mock(CharacteristicSystem.class);
        eventManager = mock(EventManager.class);
        when(physicsSystem.isPointBlocked(anyLong(), anyDouble(), anyDouble())).thenReturn(false);
        when(characteristicSystem.getValue(any(Entity.class), eq("speed"))).thenReturn(10.0);
        strategy = new MeleeAiStrategy(TARGET_ID, WORLD_ID, transformSystem, physicsSystem, characteristicSystem, eventManager);
    }

    @Test
    void testUpdateWithoutTransforms() {
        when(transformSystem.hasTransform(ENTITY_ID)).thenReturn(false);
        strategy.update(ENTITY_ID);
        verify(physicsSystem, never()).getBody(anyLong(), anyLong());
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
        when(targetTransform.getX()).thenReturn(200.0); // Distance = 100 > 50
        when(targetTransform.getY()).thenReturn(100.0);
        when(transformSystem.getTransform(TARGET_ID)).thenReturn(targetTransform);

        PhysicsBody body = mock(PhysicsBody.class);
        when(physicsSystem.getBody(WORLD_ID, ENTITY_ID)).thenReturn(body);

        strategy.update(ENTITY_ID);

        verify(body).setVelocity(anyDouble(), anyDouble());
        verify(characteristicSystem).setValue(any(Entity.class), eq("facing_angle"), eq(0.0));
        verify(eventManager, never()).publish(any());
    }

    @Test
    void testUpdateAttacking() {
        when(transformSystem.hasTransform(ENTITY_ID)).thenReturn(true);
        when(transformSystem.hasTransform(TARGET_ID)).thenReturn(true);
        
        Transform entityTransform = mock(Transform.class);
        when(entityTransform.getX()).thenReturn(100.0);
        when(entityTransform.getY()).thenReturn(100.0);
        when(transformSystem.getTransform(ENTITY_ID)).thenReturn(entityTransform);

        Transform targetTransform = mock(Transform.class);
        when(targetTransform.getX()).thenReturn(130.0); // Distance = 30 < 50
        when(targetTransform.getY()).thenReturn(100.0);
        when(transformSystem.getTransform(TARGET_ID)).thenReturn(targetTransform);

        PhysicsBody body = mock(PhysicsBody.class);
        when(physicsSystem.getBody(WORLD_ID, ENTITY_ID)).thenReturn(body);

        strategy.update(ENTITY_ID);

        verify(body).setVelocity(0, 0);
        verify(characteristicSystem).setValue(any(Entity.class), eq("facing_angle"), eq(0.0));
        
        ArgumentCaptor<EntityAttackEvent> eventCaptor = ArgumentCaptor.forClass(EntityAttackEvent.class);
        verify(eventManager).publish(eventCaptor.capture());
        assertEquals(ENTITY_ID, eventCaptor.getValue().attackerId());
    }

    @Test
    void testFacingAngleCalculation() {
        when(transformSystem.hasTransform(ENTITY_ID)).thenReturn(true);
        when(transformSystem.hasTransform(TARGET_ID)).thenReturn(true);
        
        Transform entityTransform = mock(Transform.class);
        when(entityTransform.getX()).thenReturn(100.0);
        when(entityTransform.getY()).thenReturn(100.0);
        when(transformSystem.getTransform(ENTITY_ID)).thenReturn(entityTransform);

        Transform targetTransform = mock(Transform.class);
        when(targetTransform.getX()).thenReturn(100.0); 
        when(targetTransform.getY()).thenReturn(50.0); // Angle = -90 or 270
        when(transformSystem.getTransform(TARGET_ID)).thenReturn(targetTransform);

        PhysicsBody body = mock(PhysicsBody.class);
        when(physicsSystem.getBody(WORLD_ID, ENTITY_ID)).thenReturn(body);

        strategy.update(ENTITY_ID);

        verify(characteristicSystem).setValue(any(Entity.class), eq("facing_angle"), eq(270.0));
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
    void testUpdateDoesNothingWhenTargetOutsideDetectionRange() {
        when(transformSystem.hasTransform(ENTITY_ID)).thenReturn(true);
        when(transformSystem.hasTransform(TARGET_ID)).thenReturn(true);

        when(transformSystem.getTransform(ENTITY_ID)).thenReturn(new Transform(0, 0, 0));
        when(transformSystem.getTransform(TARGET_ID)).thenReturn(new Transform(500, 0, 0));

        PhysicsBody body = mock(PhysicsBody.class);
        when(physicsSystem.getBody(WORLD_ID, ENTITY_ID)).thenReturn(body);

        strategy.update(ENTITY_ID);

        verify(body).setVelocity(0, 0);
        verify(eventManager, never()).publish(any());
    }
}
