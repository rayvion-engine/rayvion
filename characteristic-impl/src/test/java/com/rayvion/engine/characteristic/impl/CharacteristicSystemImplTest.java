package com.rayvion.engine.characteristic.impl;

import com.rayvion.engine.characteristic.CharacteristicChangedEvent;
import com.rayvion.engine.characteristic.CharacteristicDescriptor;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.event.impl.DefaultEventManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CharacteristicSystemImplTest {
    private EventManager eventManager;
    private CharacteristicSystemImpl characteristicSystem;
    private Entity entity;

    @BeforeEach
    void setUp() {
        eventManager = new DefaultEventManager();
        characteristicSystem = new CharacteristicSystemImpl(eventManager);
        characteristicSystem.init();
        entity = new Entity(1L);
    }

    @Test
    void testRegisterAndGetValue() {
        CharacteristicDescriptor<Double> health = new CharacteristicDescriptor<>(
                "health", "Health", "Current health", Double.class, 100.0
        );
        characteristicSystem.registerCharacteristic(health);

        assertEquals(100.0, characteristicSystem.getValue(entity, "health"));
    }

    @Test
    void testSetValue() {
        CharacteristicDescriptor<Double> health = new CharacteristicDescriptor<>(
                "health", "Health", "Current health", Double.class, 100.0
        );
        characteristicSystem.registerCharacteristic(health);

        characteristicSystem.setValue(entity, "health", 80.0);
        assertEquals(80.0, characteristicSystem.getValue(entity, "health"));
    }

    @Test
    void testGenericTypes() {
        CharacteristicDescriptor<String> faction = new CharacteristicDescriptor<>(
                "faction", "Faction", "Entity faction", String.class, "neutral"
        );
        characteristicSystem.registerCharacteristic(faction);

        assertEquals("neutral", characteristicSystem.getValue(entity, "faction"));
        characteristicSystem.setValue(entity, "faction", "player");
        assertEquals("player", characteristicSystem.getValue(entity, "faction"));
    }

    @Test
    void testTypeChecking() {
        CharacteristicDescriptor<Double> health = new CharacteristicDescriptor<>(
                "health", "Health", "Current health", Double.class, 100.0
        );
        characteristicSystem.registerCharacteristic(health);

        assertThrows(IllegalArgumentException.class, () -> {
            characteristicSystem.setValue(entity, "health", "not a double");
        });
    }

    @Test
    void testEventFiring() {
        CharacteristicDescriptor<Double> health = new CharacteristicDescriptor<>(
                "health", "Health", "Current health", Double.class, 100.0
        );
        characteristicSystem.registerCharacteristic(health);

        AtomicReference<CharacteristicChangedEvent<Double>> capturedEvent = new AtomicReference<>();
        eventManager.subscribe(CharacteristicChangedEvent.class, event -> capturedEvent.set((CharacteristicChangedEvent<Double>) event));

        characteristicSystem.setValue(entity, "health", 50.0);

        assertNotNull(capturedEvent.get());
        assertEquals(entity, capturedEvent.get().getEntity());
        assertEquals("health", capturedEvent.get().getCharacteristicId());
        assertEquals(100.0, capturedEvent.get().getOldValue());
        assertEquals(50.0, capturedEvent.get().getNewValue());
    }

    @Test
    void testRemoveCharacteristic() {
        CharacteristicDescriptor<Double> health = new CharacteristicDescriptor<>(
                "health", "Health", "Current health", Double.class, 100.0
        );
        characteristicSystem.registerCharacteristic(health);

        characteristicSystem.setValue(entity, "health", 50.0);
        assertTrue(characteristicSystem.hasCharacteristic(entity, "health"));

        characteristicSystem.removeCharacteristic(entity, "health");
        assertFalse(characteristicSystem.hasCharacteristic(entity, "health"));
        assertEquals(100.0, characteristicSystem.getValue(entity, "health"));
    }
}
