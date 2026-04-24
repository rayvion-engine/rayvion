package com.rayvion.engine.inventory.impl;

import com.rayvion.engine.audio.AudioSystem;
import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.entity.EntitySystem;
import com.rayvion.engine.graphics.GraphicsSystem;
import com.rayvion.engine.inventory.Inventory;
import com.rayvion.engine.inventory.InventoryItem;
import com.rayvion.engine.inventory.InventorySystem;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import com.rayvion.engine.world.WorldSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GroundItemSystemImplTest {
    private EntitySystem entitySystem;
    private TransformSystem transformSystem;
    private InventorySystem inventorySystem;
    private WorldSystem worldSystem;
    private GraphicsSystem graphicsSystem;
    private AudioSystem audioSystem;
    private CharacteristicSystem characteristicSystem;
    private GroundItemSystemImpl groundItemSystem;

    @BeforeEach
    void setUp() {
        entitySystem = mock(EntitySystem.class);
        transformSystem = mock(TransformSystem.class);
        inventorySystem = mock(InventorySystem.class);
        worldSystem = mock(WorldSystem.class);
        graphicsSystem = mock(GraphicsSystem.class);
        audioSystem = mock(AudioSystem.class);
        characteristicSystem = mock(CharacteristicSystem.class);

        groundItemSystem = new GroundItemSystemImpl(
            entitySystem,
            transformSystem,
            inventorySystem,
            worldSystem,
            graphicsSystem,
            audioSystem,
            characteristicSystem
        );
        groundItemSystem.init();
    }

    @Test
    void testCoreRegistrationAndRetrieval() {
        Entity entity = new Entity(1L);
        InventoryItem item = new InventoryItem("id", "name", "desc", "type", null, false);

        groundItemSystem.registerGroundItem(entity, item);
        
        Optional<InventoryItem> retrieved = groundItemSystem.getGroundItem(entity);
        assertTrue(retrieved.isPresent());
        assertEquals(item, retrieved.get());

        Collection<Entity> allItems = groundItemSystem.getAllGroundItems();
        assertEquals(1, allItems.size());
        assertTrue(allItems.contains(entity));

        groundItemSystem.unregisterGroundItem(entity);
        assertFalse(groundItemSystem.getGroundItem(entity).isPresent());
        assertTrue(groundItemSystem.getAllGroundItems().isEmpty());
    }

    @Test
    void testDropItem() {
        long worldId = 100L;
        InventoryItem item = new InventoryItem("item_id", "Item Name", "Desc", "type", null, false);
        Entity newEntity = new Entity(50L);
        when(entitySystem.createEntity()).thenReturn(newEntity);

        groundItemSystem.dropItem(worldId, item, 10.0, 20.0);

        verify(entitySystem).createEntity();
        verify(worldSystem).addEntityToWorld(worldId, newEntity.id());
        
        ArgumentCaptor<Transform> transformCaptor = ArgumentCaptor.forClass(Transform.class);
        verify(transformSystem).setTransform(eq(newEntity.id()), transformCaptor.capture());
        assertEquals(10.0, transformCaptor.getValue().getX());
        assertEquals(20.0, transformCaptor.getValue().getY());

        verify(characteristicSystem).setValue(newEntity, "width", 32.0);
        verify(characteristicSystem).setValue(newEntity, "height", 32.0);
        verify(characteristicSystem).setValue(newEntity, "is_ground_item", 1.0);

        assertTrue(groundItemSystem.getGroundItem(newEntity).isPresent());
        assertEquals(item, groundItemSystem.getGroundItem(newEntity).get());
    }

    @Test
    void testDropItemWithGraphics() {
        InventoryItem item = new InventoryItem("id", "name", "desc", "type", new com.rayvion.engine.graphics.TextureGraphics("test"), false);
        Entity entity = new Entity(1L);
        when(entitySystem.createEntity()).thenReturn(entity);

        groundItemSystem.dropItem(1L, item, 0, 0);

        verify(graphicsSystem).setEntityGraphics(entity.id(), item.graphics());
    }

    @Test
    void testTryInteract_NoTransform() {
        Entity interactor = new Entity(1L);
        when(transformSystem.hasTransform(interactor.id())).thenReturn(false);

        groundItemSystem.tryInteract(interactor);

        verify(inventorySystem, never()).getInventory(any());
    }

    @Test
    void testTryInteract_PickupInRange() {
        Entity interactor = new Entity(1L);
        Transform interactorPos = new Transform(0, 0, 0);
        when(transformSystem.hasTransform(interactor.id())).thenReturn(true);
        when(transformSystem.getTransform(interactor.id())).thenReturn(interactorPos);

        Entity itemEntity = new Entity(2L);
        InventoryItem item = new InventoryItem("id", "name", "desc", "type", null, false); // Manuel pickp
        Transform itemPos = new Transform(10, 10, 0); // Distance ~14.14 < 32

        groundItemSystem.registerGroundItem(itemEntity, item);
        when(transformSystem.hasTransform(itemEntity.id())).thenReturn(true);
        when(transformSystem.getTransform(itemEntity.id())).thenReturn(itemPos);

        Inventory inventory = mock(Inventory.class);
        when(inventorySystem.getInventory(interactor)).thenReturn(Optional.of(inventory));

        groundItemSystem.tryInteract(interactor);

        verify(inventory).addItem(item);
        verify(audioSystem).playSound("pickup");
        verify(transformSystem).removeTransform(itemEntity.id());
        verify(graphicsSystem).removeEntityGraphics(itemEntity.id());
        assertFalse(groundItemSystem.getGroundItem(itemEntity).isPresent());
    }

    @Test
    void testTryInteract_OutOfRange() {
        Entity interactor = new Entity(1L);
        Transform interactorPos = new Transform(0, 0, 0);
        when(transformSystem.hasTransform(interactor.id())).thenReturn(true);
        when(transformSystem.getTransform(interactor.id())).thenReturn(interactorPos);

        Entity itemEntity = new Entity(2L);
        InventoryItem item = new InventoryItem("id", "name", "desc", "type", null, false);
        Transform itemPos = new Transform(40, 40, 0); // Distance ~56.5 > 32

        groundItemSystem.registerGroundItem(itemEntity, item);
        when(transformSystem.hasTransform(itemEntity.id())).thenReturn(true);
        when(transformSystem.getTransform(itemEntity.id())).thenReturn(itemPos);

        groundItemSystem.tryInteract(interactor);

        verify(inventorySystem, never()).getInventory(any());
    }

    @Test
    void testTryInteract_AutoPickupSkipped() {
        Entity interactor = new Entity(1L);
        Transform interactorPos = new Transform(0, 0, 0);
        when(transformSystem.hasTransform(interactor.id())).thenReturn(true);
        when(transformSystem.getTransform(interactor.id())).thenReturn(interactorPos);

        Entity itemEntity = new Entity(2L);
        InventoryItem item = new InventoryItem("id", "name", "desc", "type", null, true); // Auto pickup
        Transform itemPos = new Transform(5, 5, 0);

        groundItemSystem.registerGroundItem(itemEntity, item);
        when(transformSystem.hasTransform(itemEntity.id())).thenReturn(true);
        when(transformSystem.getTransform(itemEntity.id())).thenReturn(itemPos);

        groundItemSystem.tryInteract(interactor);

        verify(inventorySystem, never()).getInventory(any());
    }

    @Test
    void testTryInteract_ItemNoTransform() {
        Entity interactor = new Entity(1L);
        Transform interactorPos = new Transform(0, 0, 0);
        when(transformSystem.hasTransform(interactor.id())).thenReturn(true);
        when(transformSystem.getTransform(interactor.id())).thenReturn(interactorPos);

        Entity itemEntity = new Entity(2L);
        InventoryItem item = new InventoryItem("id", "name", "desc", "type", null, false);

        groundItemSystem.registerGroundItem(itemEntity, item);
        when(transformSystem.hasTransform(itemEntity.id())).thenReturn(false);

        groundItemSystem.tryInteract(interactor);

        verify(inventorySystem, never()).getInventory(any());
    }

    @Test
    void testPickup_ItemNotFound() {
        Entity picker = new Entity(1L);
        Entity itemEntity = new Entity(2L);
        // Not registered

        groundItemSystem.tryInteract(picker); // Indirectly tests pickup early return if we could call it directly
        // But pickup is private, so we test via tryInteract or similar
    }

    @Test
    void testPickup_NoInventory() {
        Entity picker = new Entity(1L);
        Transform pickerPos = new Transform(0, 0, 0);
        when(transformSystem.hasTransform(picker.id())).thenReturn(true);
        when(transformSystem.getTransform(picker.id())).thenReturn(pickerPos);

        Entity itemEntity = new Entity(2L);
        InventoryItem item = new InventoryItem("id", "name", "desc", "type", null, false);
        Transform itemPos = new Transform(0, 0, 0);

        groundItemSystem.registerGroundItem(itemEntity, item);
        when(transformSystem.hasTransform(itemEntity.id())).thenReturn(true);
        when(transformSystem.getTransform(itemEntity.id())).thenReturn(itemPos);

        when(inventorySystem.getInventory(picker)).thenReturn(Optional.empty());

        groundItemSystem.tryInteract(picker);

        // Should not be removed from ground if no inventory
        assertTrue(groundItemSystem.getGroundItem(itemEntity).isPresent());
        verify(transformSystem, never()).removeTransform(itemEntity.id());
    }

    @Test
    void testTick_AutoPickup() {
        Entity picker = new Entity(1L);
        Transform pickerPos = new Transform(0, 0, 0);
        when(transformSystem.hasTransform(picker.id())).thenReturn(true);
        when(transformSystem.getTransform(picker.id())).thenReturn(pickerPos);

        when(inventorySystem.getEntitiesWithInventory()).thenReturn(List.of(picker));
        
        Entity itemEntity = new Entity(2L);
        InventoryItem item = new InventoryItem("id", "name", "desc", "type", null, true); // Auto
        Transform itemPos = new Transform(10, 0, 0);

        groundItemSystem.registerGroundItem(itemEntity, item);
        when(transformSystem.hasTransform(itemEntity.id())).thenReturn(true);
        when(transformSystem.getTransform(itemEntity.id())).thenReturn(itemPos);

        Inventory inventory = mock(Inventory.class);
        when(inventorySystem.getInventory(picker)).thenReturn(Optional.of(inventory));

        groundItemSystem.tick();

        verify(inventory).addItem(item);
        assertFalse(groundItemSystem.getGroundItem(itemEntity).isPresent());
    }

    @Test
    void testTick_ManualPickupPrompt() {
        Entity picker = new Entity(1L);
        Transform pickerPos = new Transform(0, 0, 0);
        when(transformSystem.hasTransform(picker.id())).thenReturn(true);
        when(transformSystem.getTransform(picker.id())).thenReturn(pickerPos);

        when(inventorySystem.getEntitiesWithInventory()).thenReturn(List.of(picker));
        
        Entity itemEntity = new Entity(2L);
        InventoryItem item = new InventoryItem("id", "Sword", "desc", "type", null, false); // Manual
        Transform itemPos = new Transform(10, 0, 0);

        groundItemSystem.registerGroundItem(itemEntity, item);
        when(transformSystem.hasTransform(itemEntity.id())).thenReturn(true);
        when(transformSystem.getTransform(itemEntity.id())).thenReturn(itemPos);

        groundItemSystem.tick();

        verify(graphicsSystem).setInteractionPrompt(itemEntity.id(), "[E] Pick up Sword");
    }

    @Test
    void testTick_ClearPrompts() {
        Entity itemEntity = new Entity(2L);
        InventoryItem item = new InventoryItem("id", "Sword", "desc", "type", null, false);
        groundItemSystem.registerGroundItem(itemEntity, item);

        when(inventorySystem.getEntitiesWithInventory()).thenReturn(List.of());

        groundItemSystem.tick();

        verify(graphicsSystem).removeInteractionPrompt(itemEntity.id());
    }

    @Test
    void testTick_SkipPickerNoTransform() {
        Entity picker = new Entity(1L);
        when(transformSystem.hasTransform(picker.id())).thenReturn(false);
        when(inventorySystem.getEntitiesWithInventory()).thenReturn(List.of(picker));

        groundItemSystem.tick();

        verify(transformSystem, never()).getTransform(picker.id());
    }

    @Test
    void testTick_ItemNoTransform() {
        Entity picker = new Entity(1L);
        Transform pickerPos = new Transform(0, 0, 0);
        when(transformSystem.hasTransform(picker.id())).thenReturn(true);
        when(transformSystem.getTransform(picker.id())).thenReturn(pickerPos);
        when(inventorySystem.getEntitiesWithInventory()).thenReturn(List.of(picker));

        Entity itemEntity = new Entity(2L);
        InventoryItem item = new InventoryItem("id", "Sword", "desc", "type", null, false);
        groundItemSystem.registerGroundItem(itemEntity, item);
        when(transformSystem.hasTransform(itemEntity.id())).thenReturn(false);

        groundItemSystem.tick();

        verify(transformSystem, never()).getTransform(itemEntity.id());
    }

    @Test
    void testPickup_NoAudioSystem() {
        // Create a system with null audioSystem
        GroundItemSystemImpl systemNoAudio = new GroundItemSystemImpl(
            entitySystem, transformSystem, inventorySystem, worldSystem, graphicsSystem, null, characteristicSystem
        );
        
        Entity picker = new Entity(1L);
        Transform pickerPos = new Transform(0, 0, 0);
        when(transformSystem.hasTransform(picker.id())).thenReturn(true);
        when(transformSystem.getTransform(picker.id())).thenReturn(pickerPos);

        Entity itemEntity = new Entity(2L);
        InventoryItem item = new InventoryItem("id", "name", "desc", "type", null, false);
        Transform itemPos = new Transform(0, 0, 0);

        systemNoAudio.registerGroundItem(itemEntity, item);
        when(transformSystem.hasTransform(itemEntity.id())).thenReturn(true);
        when(transformSystem.getTransform(itemEntity.id())).thenReturn(itemPos);

        Inventory inventory = mock(Inventory.class);
        when(inventorySystem.getInventory(picker)).thenReturn(Optional.of(inventory));

        systemNoAudio.tryInteract(picker);

        verify(inventory).addItem(item);
        // No crsh, and audioSystem.playSound nevr calld because it's nul
    }

    @Test
    void testGetTickDelay() {
        assertEquals(Duration.ofMillis(100), groundItemSystem.getTickDelay());
    }
}
