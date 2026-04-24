package com.rayvion.engine.equipment.impl;

import com.rayvion.engine.equipment.EquippedEvent;
import com.rayvion.engine.equipment.UnequippedEvent;
import com.rayvion.engine.event.impl.DefaultEventManager;
import com.rayvion.engine.graphics.EntityGraphics;
import com.rayvion.engine.graphics.TextureGraphics;
import com.rayvion.engine.graphics.impl.GraphicsSystemImpl;
import com.rayvion.engine.inventory.InventoryItem;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.impl.TransformSystemImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class EquipmentSystemTest {
    private DefaultEventManager eventManager;
    private GraphicsSystemImpl graphicsSystem;
    private TransformSystemImpl transformSystem;
    private EquipmentSystemImpl equipmentSystem;

    @BeforeEach
    void setUp() {
        eventManager = new DefaultEventManager();
        transformSystem = new TransformSystemImpl();
        graphicsSystem = new GraphicsSystemImpl();
        graphicsSystem.onDependencyAdded(transformSystem);
        
        equipmentSystem = new EquipmentSystemImpl();
        equipmentSystem.onDependencyAdded(graphicsSystem);
        equipmentSystem.onDependencyAdded(eventManager);
        equipmentSystem.init();
    }

    @Test
    void testEquipUnequip() {
        long entityId = 1L;
        transformSystem.setTransform(entityId, new Transform(0, 0, 0)); // Required for graphics
        
        InventoryItem sword = new InventoryItem(
            "sword_01", "Iron Sword", "Sharp", "weapon", null, false
        );

        assertFalse(equipmentSystem.hasEquippedItem(entityId));
        
        equipmentSystem.equip(entityId, sword);
        assertTrue(equipmentSystem.hasEquippedItem(entityId));
        assertEquals(sword, equipmentSystem.getEquippedItem(entityId).orElseThrow());

        equipmentSystem.unequip(entityId);
        assertFalse(equipmentSystem.hasEquippedItem(entityId));
    }

    @Test
    void testGraphicsOverride() {
        long entityId = 1L;
        transformSystem.setTransform(entityId, new Transform(0, 0, 0));
        
        EntityGraphics baseGraphics = new TextureGraphics("player_texture");
        graphicsSystem.setEntityGraphics(entityId, baseGraphics);
        
        EntityGraphics swordGraphics = new TextureGraphics("sword_texture");
        InventoryItem sword = new InventoryItem(
            "sword_01", "Iron Sword", "Sharp", "weapon", swordGraphics, false
        );

        // Equip overrides graphics
        equipmentSystem.equip(entityId, sword);
        assertEquals(swordGraphics, graphicsSystem.getEntityGraphics(entityId));

        // Unequip restores graphics
        equipmentSystem.unequip(entityId);
        assertEquals(baseGraphics, graphicsSystem.getEntityGraphics(entityId));
    }

    @Test
    void testGraphicsOverrideNoBase() {
        long entityId = 1L;
        transformSystem.setTransform(entityId, new Transform(0, 0, 0));
        
        EntityGraphics swordGraphics = new TextureGraphics("sword_texture");
        InventoryItem sword = new InventoryItem(
            "sword_01", "Iron Sword", "Sharp", "weapon", swordGraphics, false
        );

        // Equip sets graphics
        equipmentSystem.equip(entityId, sword);
        assertEquals(swordGraphics, graphicsSystem.getEntityGraphics(entityId));

        // Unequip removes graphics
        equipmentSystem.unequip(entityId);
        assertFalse(graphicsSystem.hasEntityGraphics(entityId));
    }

    @Test
    void testEvents() {
        long entityId = 1L;
        transformSystem.setTransform(entityId, new Transform(0, 0, 0));
        
        InventoryItem sword = new InventoryItem(
            "sword_01", "Iron Sword", "Sharp", "weapon", null, false
        );

        AtomicInteger equippedCount = new AtomicInteger(0);
        AtomicInteger unequippedCount = new AtomicInteger(0);

        eventManager.subscribe(EquippedEvent.class, e -> {
            assertEquals(entityId, e.entityId());
            assertEquals(sword, e.item());
            equippedCount.incrementAndGet();
        });

        eventManager.subscribe(UnequippedEvent.class, e -> {
            assertEquals(entityId, e.entityId());
            assertEquals(sword, e.item());
            unequippedCount.incrementAndGet();
        });

        equipmentSystem.equip(entityId, sword);
        assertEquals(1, equippedCount.get());

        equipmentSystem.unequip(entityId);
        assertEquals(1, unequippedCount.get());
    }
}
