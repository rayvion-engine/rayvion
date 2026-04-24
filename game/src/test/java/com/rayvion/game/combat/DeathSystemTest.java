package com.rayvion.game.combat;

import com.rayvion.engine.ai.AiSystem;
import com.rayvion.engine.audio.AudioSystem;
import com.rayvion.engine.characteristic.CharacteristicChangedEvent;
import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.entity.EntityDeathEvent;
import com.rayvion.engine.entity.EntitySystem;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.graphics.GraphicsSystem;
import com.rayvion.engine.physics.PhysicsSystem;
import com.rayvion.engine.transform.TransformSystem;
import com.rayvion.engine.world.WorldSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DeathSystemTest {

    private DeathSystem deathSystem;
    private EventManager eventManager;
    private EntitySystem entitySystem;
    private WorldSystem worldSystem;
    private PhysicsSystem physicsSystem;
    private CharacteristicSystem characteristicSystem;
    private TransformSystem transformSystem;
    private GraphicsSystem graphicsSystem;
    private AiSystem aiSystem;
    private AudioSystem audioSystem;
    private final long worldId = 0L;

    @BeforeEach
    void setUp() {
        deathSystem = new DeathSystem(worldId);
        eventManager = mock(EventManager.class);
        entitySystem = mock(EntitySystem.class);
        worldSystem = mock(WorldSystem.class);
        physicsSystem = mock(PhysicsSystem.class);
        characteristicSystem = mock(CharacteristicSystem.class);
        transformSystem = mock(TransformSystem.class);
        graphicsSystem = mock(GraphicsSystem.class);
        aiSystem = mock(AiSystem.class);
        audioSystem = mock(AudioSystem.class);

        deathSystem.onDependencyAdded(eventManager);
        deathSystem.onDependencyAdded(entitySystem);
        deathSystem.onDependencyAdded(worldSystem);
        deathSystem.onDependencyAdded(physicsSystem);
        deathSystem.onDependencyAdded(characteristicSystem);
        deathSystem.onDependencyAdded(transformSystem);
        deathSystem.onDependencyAdded(graphicsSystem);
        deathSystem.onDependencyAdded(aiSystem);
        deathSystem.onDependencyAdded(audioSystem);
        
        deathSystem.init();
    }

    @Test
    void testGetDescriptor() {
        assertNotNull(deathSystem.getDescriptor());
        assertEquals("death-system", deathSystem.getDescriptor().coordinate().id());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testEntityDeathCleanup() {
        Entity entity = new Entity(123L);
        CharacteristicChangedEvent<Double> event = new CharacteristicChangedEvent<>(entity, "health", 10.0, 0.0);

        // Capture the handler passed to subscribe
        ArgumentCaptor<java.util.function.Consumer<CharacteristicChangedEvent>> handlerCaptor = 
            ArgumentCaptor.forClass(java.util.function.Consumer.class);
        verify(eventManager).subscribe(eq(CharacteristicChangedEvent.class), handlerCaptor.capture());

        // Trigger the death
        handlerCaptor.getValue().accept(event);

        // Verify notifications
        verify(eventManager).registerEventType(EntityDeathEvent.class);
        verify(eventManager).publish(any(EntityDeathEvent.class));
        
        verify(audioSystem).playSound("death");
        verify(physicsSystem).removeBody(worldId, 123L);
        verify(aiSystem).removeStrategy(123L);
        verify(graphicsSystem).removeEntityGraphics(123L);
        verify(transformSystem).removeTransform(123L);
        verify(worldSystem).removeEntityFromWorld(worldId, 123L);
        verify(entitySystem).removeEntity(123L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNoDeathOnPositiveHealth() {
        Entity entity = new Entity(123L);
        CharacteristicChangedEvent<Double> event = new CharacteristicChangedEvent<>(entity, "health", 10.0, 5.0);

        ArgumentCaptor<java.util.function.Consumer<CharacteristicChangedEvent>> handlerCaptor = 
            ArgumentCaptor.forClass(java.util.function.Consumer.class);
        verify(eventManager).subscribe(eq(CharacteristicChangedEvent.class), handlerCaptor.capture());

        handlerCaptor.getValue().accept(event);

        verify(eventManager, never()).publish(any(EntityDeathEvent.class));
        verify(physicsSystem, never()).removeBody(anyLong(), anyLong());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNoDeathOnOtherCharacteristic() {
        Entity entity = new Entity(123L);
        CharacteristicChangedEvent<Double> event = new CharacteristicChangedEvent<>(entity, "speed", 200.0, 0.0);

        ArgumentCaptor<java.util.function.Consumer<CharacteristicChangedEvent>> handlerCaptor = 
            ArgumentCaptor.forClass(java.util.function.Consumer.class);
        verify(eventManager).subscribe(eq(CharacteristicChangedEvent.class), handlerCaptor.capture());

        handlerCaptor.getValue().accept(event);

        verify(eventManager, never()).publish(any(EntityDeathEvent.class));
        verify(physicsSystem, never()).removeBody(anyLong(), anyLong());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNoDeathOnInvalidValueType() {
        Entity entity = new Entity(123L);
        CharacteristicChangedEvent<String> event = new CharacteristicChangedEvent<>(entity, "health", "old", "new");

        ArgumentCaptor<java.util.function.Consumer<CharacteristicChangedEvent>> handlerCaptor = 
            ArgumentCaptor.forClass(java.util.function.Consumer.class);
        verify(eventManager).subscribe(eq(CharacteristicChangedEvent.class), handlerCaptor.capture());

        handlerCaptor.getValue().accept(event);

        verify(eventManager, never()).publish(any(EntityDeathEvent.class));
        verify(physicsSystem, never()).removeBody(anyLong(), anyLong());
    }

    @Test
    void testInitWithNullEventManager() {
        DeathSystem system = new DeathSystem(worldId);
        // No eventManager added
        system.init(); // Should not crash
    }

    @Test
    void testDependencyAddedUnknownType() {
        deathSystem.onDependencyAdded(mock(com.rayvion.engine.system.System.class));
        // Should not crash or affect other dependencies
    }
}
