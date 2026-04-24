package com.rayvion.engine.equipment.impl;

import com.rayvion.engine.equipment.EquipmentSystem;
import com.rayvion.engine.equipment.EquippedEvent;
import com.rayvion.engine.equipment.UnequippedEvent;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.graphics.EntityGraphics;
import com.rayvion.engine.graphics.GraphicsSystem;
import com.rayvion.engine.inventory.InventoryItem;
import com.rayvion.engine.inventory.ItemInteractEvent;
import com.rayvion.engine.system.System;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class EquipmentSystemImpl implements EquipmentSystem {

    private final Map<Long, InventoryItem> equippedItems = new ConcurrentHashMap<>();
    private final Map<Long, EntityGraphics> originalGraphics = new ConcurrentHashMap<>();
    
    private GraphicsSystem graphicsSystem;
    private EventManager eventManager;

    @Override
    public void init() {
    }

    @Override
    public void onDependencyAdded(System dependency) {
        if (dependency instanceof GraphicsSystem gs) {
            this.graphicsSystem = gs;
        } else if (dependency instanceof EventManager em) {
            this.eventManager = em;
            eventManager.registerEventType(EquippedEvent.class);
            eventManager.registerEventType(UnequippedEvent.class);
        }
    }


    @Override
    public void equip(long entityId, InventoryItem item) {
        // Unequip current item if exists
        if (hasEquippedItem(entityId)) {
            unequip(entityId);
        }

        equippedItems.put(entityId, item);

        // Handle graphics override
        EntityGraphics overrideGraphics = item.equippedGraphics() != null ? item.equippedGraphics() : item.graphics();
        if (overrideGraphics != null && graphicsSystem != null) {
            if (graphicsSystem.hasEntityGraphics(entityId)) {
                originalGraphics.put(entityId, graphicsSystem.getEntityGraphics(entityId));
            }
            graphicsSystem.setEntityGraphics(entityId, overrideGraphics);
        }

        if (eventManager != null) {
            eventManager.publish(new EquippedEvent(entityId, item));
        }
    }

    @Override
    public void unequip(long entityId) {
        InventoryItem item = equippedItems.remove(entityId);
        if (item == null) {
            return;
        }

        // Restore graphics
        if (graphicsSystem != null) {
            EntityGraphics original = originalGraphics.remove(entityId);
            if (original != null) {
                graphicsSystem.setEntityGraphics(entityId, original);
            } else if (item.graphics() != null) {
                // If item had graphics but there was no original, it means it was added by the item
                graphicsSystem.removeEntityGraphics(entityId);
            }
        }

        if (eventManager != null) {
            eventManager.publish(new UnequippedEvent(entityId, item));
        }
    }

    @Override
    public Optional<InventoryItem> getEquippedItem(long entityId) {
        return Optional.ofNullable(equippedItems.get(entityId));
    }

    @Override
    public boolean hasEquippedItem(long entityId) {
        return equippedItems.containsKey(entityId);
    }
}
