package com.rayvion.game.combat;

import com.rayvion.engine.characteristic.CharacteristicChangedEvent;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.audio.AudioSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DamageFeedbackSystemTest {

    private DamageFeedbackSystem damageFeedbackSystem;
    private EventManager eventManager;
    private AudioSystem audioSystem;

    @BeforeEach
    void setUp() {
        damageFeedbackSystem = new DamageFeedbackSystem();
        eventManager = mock(EventManager.class);
        audioSystem = mock(AudioSystem.class);
    }

    @Test
    void testGetDescriptor() {
        var descriptor = damageFeedbackSystem.getDescriptor();
        assertNotNull(descriptor);
        assertEquals("damage-feedback", descriptor.coordinate().id());
        assertTrue(descriptor.provides().stream().anyMatch(t -> t.id().equals("damage-feedback")));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testOnDependencyAdded() {
        damageFeedbackSystem.onDependencyAdded(eventManager);
        verify(eventManager).subscribe(eq(CharacteristicChangedEvent.class), any(Consumer.class));

        damageFeedbackSystem.onDependencyAdded(audioSystem);
        // audioSystem is stored internally, we'll verify its usage in other tests
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleDamageFeedback() {
        damageFeedbackSystem.onDependencyAdded(eventManager);
        damageFeedbackSystem.onDependencyAdded(audioSystem);

        ArgumentCaptor<Consumer<CharacteristicChangedEvent>> handlerCaptor = 
            ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(CharacteristicChangedEvent.class), handlerCaptor.capture());

        Entity entity = new Entity(123L);
        CharacteristicChangedEvent<Double> event = new CharacteristicChangedEvent<>(entity, "health", 10.0, 5.0);

        // Triggr damge
        handlerCaptor.getValue().accept(event);

        assertTrue(damageFeedbackSystem.isDamaged(123L));
        verify(audioSystem).playSound("hit");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleDamageFeedbackWithoutAudio() {
        damageFeedbackSystem.onDependencyAdded(eventManager);
        // audioSystem is NOT added

        ArgumentCaptor<Consumer<CharacteristicChangedEvent>> handlerCaptor = 
            ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(CharacteristicChangedEvent.class), handlerCaptor.capture());

        Entity entity = new Entity(123L);
        CharacteristicChangedEvent<Double> event = new CharacteristicChangedEvent<>(entity, "health", 10.0, 5.0);

        // Trigger damag
        handlerCaptor.getValue().accept(event);

        assertTrue(damageFeedbackSystem.isDamaged(123L));
        // No crash, and audioSystem.playSound was never called (since it's null)
    }

    @Test
    void testOnUnknownDependencyAdded() {
        com.rayvion.engine.system.System unknownSystem = mock(com.rayvion.engine.system.System.class);
        assertDoesNotThrow(() -> damageFeedbackSystem.onDependencyAdded(unknownSystem));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNoFeedbackOnHealthIncrease() {
        damageFeedbackSystem.onDependencyAdded(eventManager);
        damageFeedbackSystem.onDependencyAdded(audioSystem);

        ArgumentCaptor<Consumer<CharacteristicChangedEvent>> handlerCaptor = 
            ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(CharacteristicChangedEvent.class), handlerCaptor.capture());

        Entity entity = new Entity(123L);
        CharacteristicChangedEvent<Double> event = new CharacteristicChangedEvent<>(entity, "health", 5.0, 10.0);

        // Triggr healng
        handlerCaptor.getValue().accept(event);

        assertFalse(damageFeedbackSystem.isDamaged(123L));
        verify(audioSystem, never()).playSound(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNoFeedbackOnOtherCharacteristic() {
        damageFeedbackSystem.onDependencyAdded(eventManager);
        damageFeedbackSystem.onDependencyAdded(audioSystem);

        ArgumentCaptor<Consumer<CharacteristicChangedEvent>> handlerCaptor = 
            ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(CharacteristicChangedEvent.class), handlerCaptor.capture());

        Entity entity = new Entity(123L);
        CharacteristicChangedEvent<Double> event = new CharacteristicChangedEvent<>(entity, "mana", 10.0, 5.0);

        // Trigger mana decrease
        handlerCaptor.getValue().accept(event);

        assertFalse(damageFeedbackSystem.isDamaged(123L));
        verify(audioSystem, never()).playSound(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testNoFeedbackOnNonDoubleValues() {
        damageFeedbackSystem.onDependencyAdded(eventManager);

        ArgumentCaptor<Consumer<CharacteristicChangedEvent>> handlerCaptor = 
            ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(CharacteristicChangedEvent.class), handlerCaptor.capture());

        Entity entity = new Entity(123L);
        // Using Integer instead of Double
        CharacteristicChangedEvent<Integer> event = new CharacteristicChangedEvent<>(entity, "health", 10, 5);

        handlerCaptor.getValue().accept((CharacteristicChangedEvent)event);

        assertFalse(damageFeedbackSystem.isDamaged(123L));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testTickCleanup() throws InterruptedException {
        damageFeedbackSystem.onDependencyAdded(eventManager);
        
        ArgumentCaptor<Consumer<CharacteristicChangedEvent>> handlerCaptor = 
            ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(CharacteristicChangedEvent.class), handlerCaptor.capture());

        Entity entity = new Entity(123L);
        CharacteristicChangedEvent<Double> event = new CharacteristicChangedEvent<>(entity, "health", 10.0, 5.0);

        handlerCaptor.getValue().accept(event);
        assertTrue(damageFeedbackSystem.isDamaged(123L));

        // Wait for flash duration to expire (200ms)
        Thread.sleep(250);
        damageFeedbackSystem.tick();

        assertFalse(damageFeedbackSystem.isDamaged(123L));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testTickKeepsRecentDamage() {
        damageFeedbackSystem.onDependencyAdded(eventManager);
        
        ArgumentCaptor<Consumer<CharacteristicChangedEvent>> handlerCaptor = 
            ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(CharacteristicChangedEvent.class), handlerCaptor.capture());

        Entity entity = new Entity(123L);
        CharacteristicChangedEvent<Double> event = new CharacteristicChangedEvent<>(entity, "health", 10.0, 5.0);

        handlerCaptor.getValue().accept(event);
        assertTrue(damageFeedbackSystem.isDamaged(123L));

        damageFeedbackSystem.tick();
        assertTrue(damageFeedbackSystem.isDamaged(123L));
    }
    
    @Test
    void testInit() {
        // Should not throw anything
        assertDoesNotThrow(() -> damageFeedbackSystem.init());
    }
}
