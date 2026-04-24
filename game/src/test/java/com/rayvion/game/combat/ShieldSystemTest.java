package com.rayvion.game.combat;

import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.equipment.EquipmentSystem;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.inventory.InventoryItem;
import com.rayvion.engine.inventory.ItemInteractEvent;
import com.rayvion.engine.system.System;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShieldSystemTest {

    private ShieldSystem shieldSystem;
    private EquipmentSystem equipmentSystem;
    private EventManager eventManager;

    @BeforeEach
    void setUp() {
        shieldSystem = new ShieldSystem();
        equipmentSystem = mock(EquipmentSystem.class);
        eventManager = mock(EventManager.class);
    }

    @Test
    void testGetDescriptor() {
        var descriptor = shieldSystem.getDescriptor();
        assertNotNull(descriptor);
        assertEquals("shield-system", descriptor.coordinate().id());
        assertTrue(descriptor.dependencies().stream().anyMatch(d -> d.traitRequirement().id().equals("equipment")));
        assertTrue(descriptor.dependencies().stream().anyMatch(d -> d.traitRequirement().id().equals("event")));
    }

    @Test
    void testOnDependencyAdded() {
        shieldSystem.onDependencyAdded(equipmentSystem);
        shieldSystem.onDependencyAdded(eventManager);
        
        // No direct way to check private fields, but we wil verify behavior in handleItemInteract
        shieldSystem.init();
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), any());
    }

    @Test
    void testOnUnknownDependencyAdded() {
        System unknownSystem = mock(System.class);
        assertDoesNotThrow(() -> shieldSystem.onDependencyAdded(unknownSystem));
    }

    @Test
    void testInit() {
        shieldSystem.onDependencyAdded(eventManager);
        shieldSystem.init();
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), any());
    }

    @Test
    void testInitWithoutEventManager() {
        // Should not throw
        assertDoesNotThrow(() -> shieldSystem.init());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleItemInteractNotAShield() {
        shieldSystem.onDependencyAdded(eventManager);
        shieldSystem.onDependencyAdded(equipmentSystem);
        shieldSystem.init();

        ArgumentCaptor<Consumer<ItemInteractEvent>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), handlerCaptor.capture());

        Entity entity = new Entity(1L);
        InventoryItem item = new InventoryItem("sword", "Sword", "A sword", "weapon", null, false);
        ItemInteractEvent event = new ItemInteractEvent(entity, item);

        handlerCaptor.getValue().accept(event);

        verifyNoInteractions(equipmentSystem);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleItemInteractEquipShield() {
        shieldSystem.onDependencyAdded(eventManager);
        shieldSystem.onDependencyAdded(equipmentSystem);
        shieldSystem.init();

        ArgumentCaptor<Consumer<ItemInteractEvent>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), handlerCaptor.capture());

        Entity entity = new Entity(1L);
        InventoryItem shield = new InventoryItem("iron_shield", "Shield", "A shield", "shield", null, false);
        ItemInteractEvent event = new ItemInteractEvent(entity, shield);

        when(equipmentSystem.getEquippedItem(1L)).thenReturn(Optional.empty());

        handlerCaptor.getValue().accept(event);

        verify(equipmentSystem).equip(1L, shield);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleItemInteractUnequipShield() {
        shieldSystem.onDependencyAdded(eventManager);
        shieldSystem.onDependencyAdded(equipmentSystem);
        shieldSystem.init();

        ArgumentCaptor<Consumer<ItemInteractEvent>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), handlerCaptor.capture());

        Entity entity = new Entity(1L);
        InventoryItem shield = new InventoryItem("iron_shield", "Shield", "A shield", "shield", null, false);
        ItemInteractEvent event = new ItemInteractEvent(entity, shield);

        when(equipmentSystem.getEquippedItem(1L)).thenReturn(Optional.of(shield));

        handlerCaptor.getValue().accept(event);

        verify(equipmentSystem).unequip(1L);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleItemInteractEquipDifferentShield() {
        shieldSystem.onDependencyAdded(eventManager);
        shieldSystem.onDependencyAdded(equipmentSystem);
        shieldSystem.init();

        ArgumentCaptor<Consumer<ItemInteractEvent>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), handlerCaptor.capture());

        Entity entity = new Entity(1L);
        InventoryItem oldShield = new InventoryItem("old_shield", "Old Shield", "An old shield", "shield", null, false);
        InventoryItem newShield = new InventoryItem("new_shield", "New Shield", "A new shield", "shield", null, false);
        ItemInteractEvent event = new ItemInteractEvent(entity, newShield);

        when(equipmentSystem.getEquippedItem(1L)).thenReturn(Optional.of(oldShield));

        handlerCaptor.getValue().accept(event);

        verify(equipmentSystem).equip(1L, newShield);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testIsShieldById() {
        shieldSystem.onDependencyAdded(eventManager);
        shieldSystem.onDependencyAdded(equipmentSystem);
        shieldSystem.init();

        ArgumentCaptor<Consumer<ItemInteractEvent>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), handlerCaptor.capture());

        Entity entity = new Entity(1L);
        // type is not "shield", but ID contains "shield"
        InventoryItem shieldById = new InventoryItem("basic_shield_item", "Basic Shield", "A basic shield", "item", null, false);
        ItemInteractEvent event = new ItemInteractEvent(entity, shieldById);

        when(equipmentSystem.getEquippedItem(1L)).thenReturn(Optional.empty());

        handlerCaptor.getValue().accept(event);

        verify(equipmentSystem).equip(1L, shieldById);
    }
}
