package com.rayvion.game.combat;

import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.entity.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HealingSystemTest {

    private HealingSystem healingSystem;
    private CharacteristicSystem characteristicSystem;

    @BeforeEach
    void setUp() {
        healingSystem = new HealingSystem();
        characteristicSystem = mock(CharacteristicSystem.class);
    }

    @Test
    void testGetDescriptor() {
        var descriptor = healingSystem.getDescriptor();
        assertNotNull(descriptor);
        assertEquals("healing-system", descriptor.coordinate().id());
        assertTrue(descriptor.provides().contains(com.rayvion.engine.system.Tickable.TRAIT));
    }

    @Test
    void testInit() {
        assertDoesNotThrow(() -> healingSystem.init());
    }

    @Test
    void testGetTickDelay() {
        assertEquals(Duration.ofMillis(50), healingSystem.getTickDelay());
    }

    @Test
    void testOnDependencyAdded() {
        healingSystem.onDependencyAdded(characteristicSystem);
        // We verify its usage in healing tests
        
        // Test unknown dependency
        com.rayvion.engine.system.System unknown = mock(com.rayvion.engine.system.System.class);
        assertDoesNotThrow(() -> healingSystem.onDependencyAdded(unknown));
    }

    @Test
    void testTickEarlyExitNoSystem() {
        // No characteristic system added
        Entity entity = new Entity(1L);
        healingSystem.addEffect(entity, 100.0, 1.0);
        
        // Should return early and not call getValue
        healingSystem.tick();
        
        verify(characteristicSystem, never()).getValue(any(), anyString());
    }

    @Test
    void testHealingApplication() {
        healingSystem.onDependencyAdded(characteristicSystem);
        Entity entity = new Entity(1L);
        
        // 100 healing over 100ms (2 ticks at 50ms per tick)
        healingSystem.addEffect(entity, 100.0, 0.1);
        
        // Mock health: 50/200
        when(characteristicSystem.getValue(entity, "health")).thenReturn(50.0);
        when(characteristicSystem.getValue(entity, "max_health")).thenReturn(200.0);
        
        // Tick 1
        healingSystem.tick();
        verify(characteristicSystem).setValue(entity, "health", 100.0); // 50 + (100 / 2)
        
        // Tick 2
        when(characteristicSystem.getValue(entity, "health")).thenReturn(100.0);
        healingSystem.tick();
        verify(characteristicSystem).setValue(entity, "health", 150.0); // 100 + 50
    }

    @Test
    void testShortDurationEffect() {
        healingSystem.onDependencyAdded(characteristicSystem);
        Entity entity = new Entity(1L);
        
        // 10 healing over 1ms (should result in 1 tick)
        healingSystem.addEffect(entity, 10.0, 0.001);
        
        when(characteristicSystem.getValue(entity, "health")).thenReturn(50.0);
        when(characteristicSystem.getValue(entity, "max_health")).thenReturn(100.0);
        
        healingSystem.tick();
        verify(characteristicSystem).getValue(entity, "health");
        verify(characteristicSystem).getValue(entity, "max_health");
        verify(characteristicSystem).setValue(entity, "health", 60.0);
        
        // Next tick should do nothing as effect is removed
        healingSystem.tick();
        verifyNoMoreInteractions(characteristicSystem);
    }

    @Test
    void testMaxHealthCap() {
        healingSystem.onDependencyAdded(characteristicSystem);
        Entity entity = new Entity(1L);
        
        // 100 healing
        healingSystem.addEffect(entity, 100.0, 0.05); // 1 tick
        
        // Mock health: 90/100
        when(characteristicSystem.getValue(entity, "health")).thenReturn(90.0);
        when(characteristicSystem.getValue(entity, "max_health")).thenReturn(100.0);
        
        healingSystem.tick();
        verify(characteristicSystem).setValue(entity, "health", 100.0); // Capped at 100, not 190
    }

    @Test
    void testAlreadyFullHealth() {
        healingSystem.onDependencyAdded(characteristicSystem);
        Entity entity = new Entity(1L);
        
        healingSystem.addEffect(entity, 100.0, 0.05);
        
        // Mock health: 100/100
        when(characteristicSystem.getValue(entity, "health")).thenReturn(100.0);
        when(characteristicSystem.getValue(entity, "max_health")).thenReturn(100.0);
        
        healingSystem.tick();
        verify(characteristicSystem, never()).setValue(any(), anyString(), anyDouble());
    }

    @Test
    void testEffectCompletionLogging() {
        healingSystem.onDependencyAdded(characteristicSystem);
        Entity entity = new Entity(1L);
        
        healingSystem.addEffect(entity, 10.0, 0.05); // 1 tick
        
        when(characteristicSystem.getValue(entity, "health")).thenReturn(50.0);
        when(characteristicSystem.getValue(entity, "max_health")).thenReturn(100.0);
        
        healingSystem.tick(); // Completis effect
        
        // Verify it was removed (second tick does nothing)
        healingSystem.tick();
        verify(characteristicSystem, times(1)).setValue(any(), anyString(), anyDouble());
    }
}
