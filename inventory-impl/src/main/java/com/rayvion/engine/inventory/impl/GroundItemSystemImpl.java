package com.rayvion.engine.inventory.impl;

import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.entity.EntitySystem;
import com.rayvion.engine.graphics.GraphicsSystem;
import com.rayvion.engine.inventory.GroundItemSystem;
import com.rayvion.engine.inventory.Inventory;
import com.rayvion.engine.inventory.InventoryItem;
import com.rayvion.engine.inventory.InventorySystem;
import com.rayvion.engine.system.Tickable;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import com.rayvion.engine.world.WorldSystem;
import com.rayvion.engine.audio.AudioSystem;
import com.rayvion.engine.characteristic.CharacteristicSystem;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the {@link GroundItemSystem} that integrates with various engine systems
 * to manage items as physical entities in the world.
 * <p>
 * This system implements {@link Tickable} to perform periodic proximity checks for
 * auto-pickup and to update interaction prompts on the UI via the {@link GraphicsSystem}.
 * </p>
 */
@Slf4j
public class GroundItemSystemImpl implements GroundItemSystem, Tickable {
    private final EntitySystem entitySystem;
    private final TransformSystem transformSystem;
    private final InventorySystem inventorySystem;
    private final WorldSystem worldSystem;
    private final GraphicsSystem graphicsSystem;
    private final AudioSystem audioSystem;
    private final CharacteristicSystem characteristicSystem;
    
    private final Map<Long, InventoryItem> groundItems = new ConcurrentHashMap<>();
    
    /**
     * The maximum distance within which an entity can interact with or auto-pickup an item.
     */
    private final double pickupRange = 32.0;

    /**
     * Constructs a new GroundItemSystemImpl with all necessary system dependencies.
     *
     * @param entitySystem          System for entity lifecycle management.
     * @param transformSystem       System for managing spatial positions.
     * @param inventorySystem       System for managing entity inventories.
     * @param worldSystem           System for managing world structure.
     * @param graphicsSystem        System for rendering and UI prompts.
     * @param audioSystem           System for sound effects.
     * @param characteristicSystem System for managing entity properties/characteristics.
     */
    public GroundItemSystemImpl(EntitySystem entitySystem,
                                 TransformSystem transformSystem,
                                 InventorySystem inventorySystem,
                                 WorldSystem worldSystem,
                                 GraphicsSystem graphicsSystem,
                                 AudioSystem audioSystem,
                                 CharacteristicSystem characteristicSystem) {
        this.entitySystem = entitySystem;
        this.transformSystem = transformSystem;
        this.inventorySystem = inventorySystem;
        this.worldSystem = worldSystem;
        this.graphicsSystem = graphicsSystem;
        this.audioSystem = audioSystem;
        this.characteristicSystem = characteristicSystem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
    }

    /**
     * {@inheritDoc}
     * <p>
     * Initializes the ground item entity with standard dimensions and marks it as a ground item.
     * </p>
     */
    @Override
    public void dropItem(long worldId, InventoryItem item, double x, double y) {
        Entity entity = entitySystem.createEntity();
        worldSystem.addEntityToWorld(worldId, entity.id());

        Transform t = new Transform();
        t.setX(x);
        t.setY(y);
        transformSystem.setTransform(entity.id(), t);

        if (item.graphics() != null) {
            graphicsSystem.setEntityGraphics(entity.id(), item.graphics());
        }
        
        characteristicSystem.setValue(entity, "width", 32.0);
        characteristicSystem.setValue(entity, "height", 32.0);
        characteristicSystem.setValue(entity, "is_ground_item", 1.0);

        registerGroundItem(entity, item);
        log.info("Dropped item {} at ({}, {}) as entity {}", item.name(), x, y, entity.id());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerGroundItem(Entity entity, InventoryItem item) {
        groundItems.put(entity.id(), item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterGroundItem(Entity entity) {
        groundItems.remove(entity.id());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<InventoryItem> getGroundItem(Entity entity) {
        return Optional.ofNullable(groundItems.get(entity.id()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Entity> getAllGroundItems() {
        Collection<Entity> entities = new ArrayList<>();
        for (Long id : groundItems.keySet()) {
            entities.add(new Entity(id));
        }
        return entities;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Searches for the closest non-auto-pickup item within {@link #pickupRange} and
     * performs a pickup for the interactor.
     * </p>
     */
    @Override
    public void tryInteract(Entity interactor) {
        if (!transformSystem.hasTransform(interactor.id())) return;
        Transform interactorTransform = transformSystem.getTransform(interactor.id());

        Entity closestItem = null;
        double minDistance = Double.MAX_VALUE;

        for (Map.Entry<Long, InventoryItem> entry : groundItems.entrySet()) {
            InventoryItem item = entry.getValue();
            if (item.autoPickup()) continue; // Skip auto-pickup items for manual interaction

            long itemEntityId = entry.getKey();
            if (!transformSystem.hasTransform(itemEntityId)) continue;

            Transform itemTransform = transformSystem.getTransform(itemEntityId);
            double dist = calculateDistance(interactorTransform, itemTransform);

            if (dist < pickupRange && dist < minDistance) {
                minDistance = dist;
                closestItem = new Entity(itemEntityId);
            }
        }

        if (closestItem != null) {
            pickup(interactor, closestItem);
        }
    }

    /**
     * Performs the pickup of an item entity by a picker entity.
     * <p>
     * Transfers the item data to the picker's inventory, plays a sound effect,
     * and removes the ground item entity's components from the world.
     * </p>
     *
     * @param picker     The entity picking up the item.
     * @param itemEntity The entity representing the item on the ground.
     */
    private void pickup(Entity picker, Entity itemEntity) {
        InventoryItem item = groundItems.get(itemEntity.id());
        if (item == null) return;

        Optional<Inventory> inventoryOpt = inventorySystem.getInventory(picker);
        if (inventoryOpt.isPresent()) {
            inventoryOpt.get().addItem(item);
            
            if (audioSystem != null) {
                audioSystem.playSound("pickup");
            }
            // Remove from ground
            unregisterGroundItem(itemEntity);
            transformSystem.removeTransform(itemEntity.id());
            graphicsSystem.removeEntityGraphics(itemEntity.id());
            // worldSystem.removeEntityFromWorld(...) - Assuming a way to remove or just let it be
            
            log.info("Entity {} picked up item {}", picker.id(), item.name());
        }
    }

    /**
     * Periodic logic to handle auto-pickup and interaction prompt visibility.
     * <p>
     * Iterates through all entities with inventories and their proximity to ground items.
     * Items with {@code autoPickup=true} are collected immediately. For other items,
     * the closest one to any picker will display an interaction prompt.
     * </p>
     */
    @Override
    public void tick() {
        Collection<Entity> inventoryEntities = inventorySystem.getEntitiesWithInventory();
        
        // Track which items should have a prompt this tick
        java.util.Set<Long> itemsWithPrompts = new java.util.HashSet<>();

        // 1. Handle auto-pickup and find closest items for prompts
        for (Entity inventoryEntity : inventoryEntities) {
            if (!transformSystem.hasTransform(inventoryEntity.id())) continue;
            Transform pickerTransform = transformSystem.getTransform(inventoryEntity.id());

            Entity closestManualItem = null;
            double minDistance = Double.MAX_VALUE;

            for (Map.Entry<Long, InventoryItem> entry : groundItems.entrySet()) {
                long itemEntityId = entry.getKey();
                InventoryItem item = entry.getValue();
                if (!transformSystem.hasTransform(itemEntityId)) continue;
                Transform itemTransform = transformSystem.getTransform(itemEntityId);

                double dist = calculateDistance(itemTransform, pickerTransform);
                if (dist < pickupRange) {
                    if (item.autoPickup()) {
                        // Auto-pickup immediately
                        pickup(inventoryEntity, new Entity(itemEntityId));
                    } else {
                        // Potential manual pickup
                        if (dist < minDistance) {
                            minDistance = dist;
                            closestManualItem = new Entity(itemEntityId);
                        }
                    }
                }
            }

            if (closestManualItem != null) {
                itemsWithPrompts.add(closestManualItem.id());
                String message = "[E] Pick up " + groundItems.get(closestManualItem.id()).name();
                graphicsSystem.setInteractionPrompt(closestManualItem.id(), message);
            }
        }

        // 2. Clear prompts for items that are no longer the closest for any player
        for (Long itemId : groundItems.keySet()) {
            if (!itemsWithPrompts.contains(itemId)) {
                graphicsSystem.removeInteractionPrompt(itemId);
            }
        }
    }

    /**
     * Calculates the Euclidean distance between two transforms.
     *
     * @param t1 The first transform.
     * @param t2 The second transform.
     * @return The distance between the two transforms.
     */
    private double calculateDistance(Transform t1, Transform t2) {
        double dx = t1.getX() - t2.getX();
        double dy = t1.getY() - t2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns a delay of 100ms, performing proximity checks 10 times per second.
     * </p>
     */
    @Override
    public Duration getTickDelay() {
        return Duration.ofMillis(100); // Check auto-pickup 10 times per second
    }
}
