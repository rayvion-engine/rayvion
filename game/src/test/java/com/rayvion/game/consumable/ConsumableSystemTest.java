package com.rayvion.game.consumable;

import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.inventory.*;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsumableSystemTest {

    private ConsumableSystem consumableSystem;
    private InventorySystem inventorySystem;
    private EventManager eventManager;

    @BeforeEach
    void setUp() {
        consumableSystem = new ConsumableSystem();
        inventorySystem = mock(InventorySystem.class);
        eventManager = mock(EventManager.class);

        consumableSystem.onDependencyAdded(inventorySystem);
        consumableSystem.onDependencyAdded(eventManager);
    }

    @Test
    void testGetDescriptor() {
        SystemDescriptor descriptor = consumableSystem.getDescriptor();
        assertNotNull(descriptor);
        assertEquals("consumable-system", descriptor.coordinate().id());
    }

    @Test
    void testInitSubscribesToEvent() {
        consumableSystem.init();
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), any());
    }

    @Test
    void testInitWithoutEventManager() {
        ConsumableSystem system = new ConsumableSystem();
        // No evnt managr added
        system.init();
        // Shoud not throw excepton
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleItemInteractNonConsumable() {
        consumableSystem.init();
        ArgumentCaptor<Consumer<ItemInteractEvent>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), handlerCaptor.capture());

        Entity entity = new Entity(1L);
        InventoryItem item = new InventoryItem("sword", "Sword", "Melee weapon", "weapon", null, false);
        ItemInteractEvent event = new ItemInteractEvent(entity, item);

        handlerCaptor.getValue().accept(event);

        verify(inventorySystem, never()).getInventory(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleItemInteractNullInventorySystem() {
        ConsumableSystem system = new ConsumableSystem();
        system.onDependencyAdded(eventManager);
        system.init();

        ArgumentCaptor<Consumer<ItemInteractEvent>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), handlerCaptor.capture());

        Entity entity = new Entity(1L);
        InventoryItem item = new InventoryItem("potion", "Potion", "Heals", "consumable", null, false);
        ItemInteractEvent event = new ItemInteractEvent(entity, item);

        handlerCaptor.getValue().accept(event);

        verify(inventorySystem, never()).getInventory(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleItemInteractMissingInventory() {
        consumableSystem.init();
        ArgumentCaptor<Consumer<ItemInteractEvent>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), handlerCaptor.capture());

        Entity entity = new Entity(1L);
        InventoryItem item = new InventoryItem("potion", "Potion", "Heals", "consumable", null, false);
        ItemInteractEvent event = new ItemInteractEvent(entity, item);

        when(inventorySystem.getInventory(entity)).thenReturn(Optional.empty());

        handlerCaptor.getValue().accept(event);

        verify(inventorySystem).getInventory(entity);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleItemInteractFailedRemoval() {
        consumableSystem.init();
        ArgumentCaptor<Consumer<ItemInteractEvent>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), handlerCaptor.capture());

        Entity entity = new Entity(1L);
        InventoryItem item = new InventoryItem("potion", "Potion", "Heals", "consumable", null, false);
        ItemInteractEvent event = new ItemInteractEvent(entity, item);

        Inventory inventory = mock(Inventory.class);
        when(inventorySystem.getInventory(entity)).thenReturn(Optional.of(inventory));
        when(inventory.removeItem(item)).thenReturn(false);

        handlerCaptor.getValue().accept(event);

        verify(inventory).removeItem(item);
        verify(eventManager, never()).publish(any(ConsumableItemUseEvent.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleItemInteractSuccessfulConsumption() {
        consumableSystem.init();
        ArgumentCaptor<Consumer<ItemInteractEvent>> handlerCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), handlerCaptor.capture());

        Entity entity = new Entity(1L);
        InventoryItem item = new InventoryItem("potion", "Potion", "Heals", "consumable", null, false);
        ItemInteractEvent event = new ItemInteractEvent(entity, item);

        Inventory inventory = mock(Inventory.class);
        when(inventorySystem.getInventory(entity)).thenReturn(Optional.of(inventory));
        when(inventory.removeItem(item)).thenReturn(true);

        handlerCaptor.getValue().accept(event);

        verify(inventory).removeItem(item);
        verify(eventManager).publish(any(ConsumableItemUseEvent.class));
        
        ArgumentCaptor<ConsumableItemUseEvent> useEventCaptor = ArgumentCaptor.forClass(ConsumableItemUseEvent.class);
        verify(eventManager).publish(useEventCaptor.capture());
        assertEquals(entity, useEventCaptor.getValue().user());
        assertEquals(item, useEventCaptor.getValue().item());
    }


    @Test
    void testOnDependencyAddedWithOtherSystem() {
        com.rayvion.engine.system.System otherSystem = mock(com.rayvion.engine.system.System.class);
        consumableSystem.onDependencyAdded(otherSystem);
        // Should not crash and not affect existing dependencies
        consumableSystem.init();
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), any());
    }
}
