package com.rayvion.game;

import com.rayvion.engine.bindings.BindingEvent;
import com.rayvion.engine.bindings.BindingParameter;
import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.input.KeyEvent;
import com.rayvion.engine.physics.PhysicsBody;
import com.rayvion.engine.physics.PhysicsSystem;
import com.rayvion.engine.system.Tickable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PlayerMovementSystemTest {

    private static final long WORLD_ID = 1L;
    private static final long PLAYER_ID = 100L;
    private PlayerMovementSystem movementSystem;
    private PhysicsSystem physicsSystem;
    private EventManager eventManager;
    private CharacteristicSystem characteristicSystem;
    private PhysicsBody physicsBody;

    @BeforeEach
    void setUp() {
        movementSystem = new PlayerMovementSystem(WORLD_ID, PLAYER_ID);
        physicsSystem = mock(PhysicsSystem.class);
        eventManager = mock(EventManager.class);
        characteristicSystem = mock(CharacteristicSystem.class);
        physicsBody = mock(PhysicsBody.class);

        when(physicsSystem.getBody(WORLD_ID, PLAYER_ID)).thenReturn(physicsBody);
        when(characteristicSystem.getValue(any(Entity.class), anyString())).thenReturn(100.0);
    }

    @Test
    void testGetDescriptor() {
        var descriptor = movementSystem.getDescriptor();
        assertNotNull(descriptor);
        assertEquals("player-movement", descriptor.coordinate().id());
        assertTrue(descriptor.dependencies().stream().anyMatch(d -> d.traitRequirement().id().equals("physics")));
        assertTrue(descriptor.dependencies().stream().anyMatch(d -> d.traitRequirement().id().equals("event")));
        assertTrue(descriptor.dependencies().stream().anyMatch(d -> d.traitRequirement().id().equals("characteristic")));
        assertTrue(descriptor.provides().contains(Tickable.TRAIT));
    }

    @Test
    void testOnDependencyAdded() {
        movementSystem.onDependencyAdded(physicsSystem);
        movementSystem.onDependencyAdded(eventManager);
        movementSystem.onDependencyAdded(characteristicSystem);
        
        // No public getters, but we verify usage in other tests
        movementSystem.init();
        verify(eventManager).subscribe(eq(BindingEvent.class), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testInit() {
        movementSystem.onDependencyAdded(eventManager);
        movementSystem.init();
        verify(eventManager).subscribe(eq(BindingEvent.class), any(Consumer.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleBindingEvent() {
        movementSystem.onDependencyAdded(eventManager);
        movementSystem.onDependencyAdded(physicsSystem);
        movementSystem.init();

        ArgumentCaptor<Consumer<BindingEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(BindingEvent.class), captor.capture());
        Consumer<BindingEvent> handler = captor.getValue();

        BindingParameter forwardParam = new BindingParameter("Forward", null);
        
        // Key Down
        handler.accept(new BindingEvent(forwardParam, KeyEvent.Type.KEY_DOWN));
        verify(physicsBody).setVelocity(0.0, 100.0); // Default speed is 100

        // Key Up
        handler.accept(new BindingEvent(forwardParam, KeyEvent.Type.KEY_UP));
        verify(physicsBody, atLeastOnce()).setVelocity(0.0, 0.0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testUpdateVelocity_NoBody() {
        movementSystem.onDependencyAdded(eventManager);
        movementSystem.onDependencyAdded(physicsSystem);
        movementSystem.init();

        when(physicsSystem.getBody(WORLD_ID, PLAYER_ID)).thenReturn(null);

        ArgumentCaptor<Consumer<BindingEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(BindingEvent.class), captor.capture());
        
        BindingParameter forwardParam = new BindingParameter("Forward", null);
        assertDoesNotThrow(() -> captor.getValue().accept(new BindingEvent(forwardParam, KeyEvent.Type.KEY_DOWN)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testUpdateVelocity_DiagonalAndRotation() {
        movementSystem.onDependencyAdded(eventManager);
        movementSystem.onDependencyAdded(physicsSystem);
        movementSystem.onDependencyAdded(characteristicSystem);
        movementSystem.init();

        ArgumentCaptor<Consumer<BindingEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(BindingEvent.class), captor.capture());
        Consumer<BindingEvent> handler = captor.getValue();

        when(characteristicSystem.getValue(any(Entity.class), eq("speed"))).thenReturn(200.0);

        // Move Forward and Right
        handler.accept(new BindingEvent(new BindingParameter("Forward", null), KeyEvent.Type.KEY_DOWN));
        handler.accept(new BindingEvent(new BindingParameter("Right", null), KeyEvent.Type.KEY_DOWN));

        verify(physicsBody).setVelocity(200.0, 200.0);
        
        // Math.atan2(200, 200) = PI/4 (45 degrees)
        verify(physicsBody).setRotation(Math.PI / 4.0);
        verify(characteristicSystem).setValue(any(Entity.class), eq("facing_angle"), eq(45.0));
        verify(characteristicSystem, atLeastOnce()).setValue(any(Entity.class), eq("animation_state"), eq("move"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testUpdateVelocity_Directions() {
        movementSystem.onDependencyAdded(eventManager);
        movementSystem.onDependencyAdded(physicsSystem);
        movementSystem.init();

        ArgumentCaptor<Consumer<BindingEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(BindingEvent.class), captor.capture());
        Consumer<BindingEvent> handler = captor.getValue();

        // Backward
        handler.accept(new BindingEvent(new BindingParameter("Backward", null), KeyEvent.Type.KEY_DOWN));
        verify(physicsBody).setVelocity(0.0, -100.0);
        
        // Left (and Backward still down)
        handler.accept(new BindingEvent(new BindingParameter("Left", null), KeyEvent.Type.KEY_DOWN));
        verify(physicsBody).setVelocity(-100.0, -100.0);
        
        // Neutralize Backward
        handler.accept(new BindingEvent(new BindingParameter("Forward", null), KeyEvent.Type.KEY_DOWN));
        verify(physicsBody).setVelocity(-100.0, 0.0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testUpdateVelocity_IdleState() {
        movementSystem.onDependencyAdded(eventManager);
        movementSystem.onDependencyAdded(physicsSystem);
        movementSystem.onDependencyAdded(characteristicSystem);
        movementSystem.init();

        ArgumentCaptor<Consumer<BindingEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(BindingEvent.class), captor.capture());
        
        // Trigger update with no active bindings
        captor.getValue().accept(new BindingEvent(new BindingParameter("Unknown", null), KeyEvent.Type.KEY_UP));

        verify(physicsBody).setVelocity(0.0, 0.0);
        verify(characteristicSystem).setValue(any(Entity.class), eq("animation_state"), eq("idle"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testUpdateVelocity_NegativeAngleDeg() {
        movementSystem.onDependencyAdded(eventManager);
        movementSystem.onDependencyAdded(physicsSystem);
        movementSystem.onDependencyAdded(characteristicSystem);
        movementSystem.init();

        ArgumentCaptor<Consumer<BindingEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(BindingEvent.class), captor.capture());
        Consumer<BindingEvent> handler = captor.getValue();

        // Move Backward (vy = -100, vx = 0)
        // atan2(-100, 0) = -PI/2 (-90 degrees)
        handler.accept(new BindingEvent(new BindingParameter("Backward", null), KeyEvent.Type.KEY_DOWN));

        verify(physicsBody).setRotation(-Math.PI / 2.0);
        // -90 + 360 = 270
        verify(characteristicSystem).setValue(any(Entity.class), eq("facing_angle"), eq(270.0));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testTickReappliesVelocityWhileMovementIsActive() {
        movementSystem.onDependencyAdded(eventManager);
        movementSystem.onDependencyAdded(physicsSystem);
        movementSystem.onDependencyAdded(characteristicSystem);
        movementSystem.init();

        ArgumentCaptor<Consumer<BindingEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(BindingEvent.class), captor.capture());

        captor.getValue().accept(new BindingEvent(new BindingParameter("Forward", null), KeyEvent.Type.KEY_DOWN));
        movementSystem.tick();

        verify(physicsBody, times(2)).setVelocity(0.0, 100.0);
        verify(characteristicSystem, atLeast(2)).setValue(any(Entity.class), eq("animation_state"), eq("move"));
    }

    @Test
    void testTickDoesNothingWithoutActiveMovementBindings() {
        movementSystem.onDependencyAdded(physicsSystem);
        movementSystem.onDependencyAdded(characteristicSystem);

        movementSystem.tick();

        verifyNoInteractions(physicsBody);
        verify(characteristicSystem, never()).setValue(any(Entity.class), anyString(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testTickDoesNotRestartMovementAfterKeyRelease() {
        movementSystem.onDependencyAdded(eventManager);
        movementSystem.onDependencyAdded(physicsSystem);
        movementSystem.onDependencyAdded(characteristicSystem);
        movementSystem.init();

        ArgumentCaptor<Consumer<BindingEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(BindingEvent.class), captor.capture());
        Consumer<BindingEvent> handler = captor.getValue();

        BindingParameter forwardParam = new BindingParameter("Forward", null);
        handler.accept(new BindingEvent(forwardParam, KeyEvent.Type.KEY_DOWN));
        handler.accept(new BindingEvent(forwardParam, KeyEvent.Type.KEY_UP));

        clearInvocations(physicsBody, characteristicSystem);

        movementSystem.tick();

        verifyNoInteractions(physicsBody);
        verify(characteristicSystem, never()).setValue(any(Entity.class), anyString(), any());
    }
}
