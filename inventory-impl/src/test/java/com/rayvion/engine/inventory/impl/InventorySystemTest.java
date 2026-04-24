package com.rayvion.engine.inventory.impl;

import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.event.impl.DefaultEventManager;
import com.rayvion.engine.graphics.TextureGraphics;
import com.rayvion.engine.inventory.Inventory;
import com.rayvion.engine.inventory.InventoryItem;
import com.rayvion.engine.inventory.ItemAddedEvent;
import com.rayvion.engine.inventory.ItemRemovedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class InventorySystemTest {
    private DefaultEventManager eventManager;
    private InventorySystemImpl inventorySystem;

    @BeforeEach
    void setUp() {
        eventManager = new DefaultEventManager();
        inventorySystem = new InventorySystemImpl(eventManager);
        inventorySystem.init();
    }

    @Test
    void testInventoryLifecycle() {
        Entity entity = new Entity(1);
        
        // Inital state
        assertTrue(inventorySystem.getInventory(entity).isEmpty());
        
        // Creat invntory
        Inventory inventory = inventorySystem.createInventory(entity);
        assertNotNull(inventory);
        assertTrue(inventorySystem.getInventory(entity).isPresent());
        assertEquals(inventory, inventorySystem.getInventory(entity).get());
        
        // Remove inventory
        inventorySystem.removeInventory(entity);
        assertTrue(inventorySystem.getInventory(entity).isEmpty());
    }

    @Test
    void testInventoryOperations() {
        Entity entity = new Entity(1);
        Inventory inventory = inventorySystem.createInventory(entity);
        
        InventoryItem item = new InventoryItem(
            "sword_01",
            "Iron Sword",
            "A basic iron sword.",
            "weapon",
            new TextureGraphics("sword_texture"),
            false
        );
        
        inventory.addItem(item);
        assertEquals(1, inventory.size());
        assertEquals(item, inventory.getItems().get(0));
        
        assertTrue(inventory.removeItem(item));
        assertEquals(0, inventory.size());
    }

    @Test
    void testEvents() {
        Entity entity = new Entity(1);
        Inventory inventory = inventorySystem.createInventory(entity);
        
        AtomicInteger addedCount = new AtomicInteger(0);
        AtomicInteger removedCount = new AtomicInteger(0);
        
        eventManager.subscribe(ItemAddedEvent.class, e -> {
            assertEquals(entity, e.entity());
            assertEquals("sword_01", e.item().id());
            addedCount.incrementAndGet();
        });
        
        eventManager.subscribe(ItemRemovedEvent.class, e -> {
            assertEquals(entity, e.entity());
            assertEquals("sword_01", e.item().id());
            removedCount.incrementAndGet();
        });
        
        InventoryItem item = new InventoryItem(
            "sword_01",
            "Iron Sword",
            "A basic iron sword.",
            "weapon",
            new TextureGraphics("sword_texture"),
            false
        );
        
        inventory.addItem(item);
        assertEquals(1, addedCount.get());
        
        inventory.removeItem(item);
        assertEquals(1, removedCount.get());
    }
}
