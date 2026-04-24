package com.rayvion.game.combat;

import com.rayvion.engine.bindings.BindingGroup;
import com.rayvion.engine.bindings.BindingEvent;
import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.entity.EntitySystem;
import com.rayvion.engine.equipment.EquipmentSystem;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.graphics.GraphicsSystem;
import com.rayvion.engine.graphics.TextureGraphics;
import com.rayvion.engine.input.KeyEvent;
import com.rayvion.engine.inventory.InventoryItem;
import com.rayvion.engine.inventory.ItemInteractEvent;
import com.rayvion.engine.physics.PhysicsSystem;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import com.rayvion.engine.world.WorldSystem;
import com.rayvion.engine.audio.AudioSystem;
import com.rayvion.engine.bindings.BindingParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HandgunSystemTest {

    private HandgunSystem handgunSystem;
    private long worldId = 1L;
    private long playerId = 100L;

    private EquipmentSystem equipmentSystem;
    private WorldSystem worldSystem;
    private TransformSystem transformSystem;
    private CharacteristicSystem characteristicSystem;
    private GraphicsSystem graphicsSystem;
    private EventManager eventManager;
    private PhysicsSystem physicsSystem;
    private EntitySystem entitySystem;
    private AudioSystem audioSystem;

    @BeforeEach
    void setUp() {
        handgunSystem = new HandgunSystem(worldId, playerId);

        equipmentSystem = mock(EquipmentSystem.class);
        worldSystem = mock(WorldSystem.class);
        transformSystem = mock(TransformSystem.class);
        characteristicSystem = mock(CharacteristicSystem.class);
        graphicsSystem = mock(GraphicsSystem.class);
        eventManager = mock(EventManager.class);
        physicsSystem = mock(PhysicsSystem.class);
        entitySystem = mock(EntitySystem.class);
        audioSystem = mock(AudioSystem.class);

        handgunSystem.onDependencyAdded(equipmentSystem);
        handgunSystem.onDependencyAdded(worldSystem);
        handgunSystem.onDependencyAdded(transformSystem);
        handgunSystem.onDependencyAdded(characteristicSystem);
        handgunSystem.onDependencyAdded(graphicsSystem);
        handgunSystem.onDependencyAdded(eventManager);
        handgunSystem.onDependencyAdded(physicsSystem);
        handgunSystem.onDependencyAdded(entitySystem);
        handgunSystem.onDependencyAdded(audioSystem);
    }

    @Test
    void testGetDescriptor() {
        assertNotNull(handgunSystem.getDescriptor());
        assertEquals("handgun-combat", handgunSystem.getDescriptor().coordinate().id());
    }

    @Test
    void testInit() {
        handgunSystem.init();
        verify(eventManager).subscribe(eq(BindingEvent.class), any());
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testEquipHandgun() {
        handgunSystem.init();
        ArgumentCaptor<Consumer<ItemInteractEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), captor.capture());

        InventoryItem handgun = new InventoryItem("handgun_01", "Pistol", "Type", "handgun", null, null, false);
        Entity entity = new Entity(playerId);
        ItemInteractEvent event = new ItemInteractEvent(entity, handgun);

        when(equipmentSystem.getEquippedItem(playerId)).thenReturn(Optional.empty());

        captor.getValue().accept(event);

        verify(equipmentSystem).equip(playerId, handgun);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testUnequipHandgun() {
        handgunSystem.init();
        ArgumentCaptor<Consumer<ItemInteractEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), captor.capture());

        InventoryItem handgun = new InventoryItem("handgun_01", "Pistol", "Type", "handgun", null, null, false);
        Entity entity = new Entity(playerId);
        ItemInteractEvent event = new ItemInteractEvent(entity, handgun);

        when(equipmentSystem.getEquippedItem(playerId)).thenReturn(Optional.of(handgun));

        captor.getValue().accept(event);

        verify(equipmentSystem).unequip(playerId);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testInteractNonHandgun() {
        handgunSystem.init();
        ArgumentCaptor<Consumer<ItemInteractEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), captor.capture());

        InventoryItem sword = new InventoryItem("sword_01", "Sword", "Type", "melee", null, null, false);
        Entity entity = new Entity(playerId);
        ItemInteractEvent event = new ItemInteractEvent(entity, sword);

        captor.getValue().accept(event);

        verify(equipmentSystem, never()).equip(anyLong(), any());
        verify(equipmentSystem, never()).unequip(anyLong());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAttackSuccess() {
        handgunSystem.init();
        ArgumentCaptor<Consumer<BindingEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(BindingEvent.class), captor.capture());

        InventoryItem handgun = new InventoryItem("handgun_01", "Pistol", "Type", "handgun", null, null, false);
        when(equipmentSystem.getEquippedItem(playerId)).thenReturn(Optional.of(handgun));

        Transform transform = new Transform();
        transform.setX(100);
        transform.setY(100);
        when(transformSystem.getTransform(playerId)).thenReturn(transform);
        when(characteristicSystem.getValue(any(), eq("facing_angle"))).thenReturn(90.0);
        when(characteristicSystem.getValue(any(), eq("strength"))).thenReturn(10.0);

        Entity bulletEntity = new Entity(200L);
        when(entitySystem.createEntity()).thenReturn(bulletEntity);

        BindingEvent event = new BindingEvent(new BindingParameter("Attack", new BindingGroup("")), KeyEvent.Type.KEY_DOWN);
        captor.getValue().accept(event);

        verify(audioSystem).playSound("rifle_shoot");
        verify(characteristicSystem).setValue(any(Entity.class), eq("animation_state"), eq("shoot"));
        verify(worldSystem).addEntityToWorld(worldId, 200L);
        verify(transformSystem).setTransform(eq(200L), any());
        verify(graphicsSystem).setEntityGraphics(eq(200L), any(TextureGraphics.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAttackCooldown() {
        handgunSystem.init();
        ArgumentCaptor<Consumer<BindingEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(BindingEvent.class), captor.capture());

        InventoryItem handgun = new InventoryItem("handgun_01", "Pistol", "Type", "handgun", null, null, false);
        when(equipmentSystem.getEquippedItem(playerId)).thenReturn(Optional.of(handgun));
        when(transformSystem.getTransform(playerId)).thenReturn(new Transform());
        when(entitySystem.createEntity()).thenReturn(new Entity(200L));

        BindingEvent event = new BindingEvent(new BindingParameter("Attack", new BindingGroup("")), KeyEvent.Type.KEY_DOWN);
        
        // First attack
        captor.getValue().accept(event);
        verify(entitySystem, times(1)).createEntity();

        // Second attack immediately (cooldown is 400ms)
        captor.getValue().accept(event);
        verify(entitySystem, times(1)).createEntity(); // Should still be 1
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAttackNoHandgun() {
        handgunSystem.init();
        ArgumentCaptor<Consumer<BindingEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(BindingEvent.class), captor.capture());

        when(equipmentSystem.getEquippedItem(playerId)).thenReturn(Optional.empty());

        BindingEvent event = new BindingEvent(new BindingParameter("Attack", new BindingGroup("")), KeyEvent.Type.KEY_DOWN);
        captor.getValue().accept(event);

        verify(entitySystem, never()).createEntity();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testBulletMovementAndCollision() {
        handgunSystem.init();
        
        // Setup an active bullet via attack
        ArgumentCaptor<Consumer<BindingEvent>> attackCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager, atLeastOnce()).subscribe(eq(BindingEvent.class), attackCaptor.capture());

        InventoryItem handgun = new InventoryItem("handgun_01", "Pistol", "Type", "handgun", null, null, false);
        when(equipmentSystem.getEquippedItem(playerId)).thenReturn(Optional.of(handgun));
        Transform playerTransform = new Transform();
        playerTransform.setX(0);
        playerTransform.setY(0);
        when(transformSystem.getTransform(playerId)).thenReturn(playerTransform);
        when(characteristicSystem.getValue(any(), eq("facing_angle"))).thenReturn(0.0); // Facing Right
        
        Entity bulletEntity = new Entity(200L);
        when(entitySystem.createEntity()).thenReturn(bulletEntity);
        
        attackCaptor.getValue().accept(new BindingEvent(new BindingParameter("Attack", new BindingGroup("")), KeyEvent.Type.KEY_DOWN));

        // Now test tick
        when(worldSystem.getEntities(worldId)).thenReturn(Collections.emptyList());
        when(physicsSystem.isPointBlocked(anyLong(), anyDouble(), anyDouble())).thenReturn(false);
        
        // Mock transform for bullet
        Transform bulletTransform = new Transform();
        when(transformSystem.getTransform(200L)).thenReturn(bulletTransform);

        // First tick - move bullet
        handgunSystem.tick();
        verify(transformSystem, atLeastOnce()).setTransform(eq(200L), any());

        // Test collision with wall
        reset(transformSystem);
        when(transformSystem.getTransform(200L)).thenReturn(bulletTransform);
        when(physicsSystem.isPointBlocked(anyLong(), anyDouble(), anyDouble())).thenReturn(true);
        handgunSystem.tick();
        
        verify(worldSystem, atLeastOnce()).removeEntityFromWorld(anyLong(), anyLong());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testBulletHitEntity() {
        handgunSystem.init();
        ArgumentCaptor<Consumer<BindingEvent>> attackCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager, atLeastOnce()).subscribe(eq(BindingEvent.class), attackCaptor.capture());

        InventoryItem handgun = new InventoryItem("handgun_01", "Pistol", "Type", "handgun", null, null, false);
        when(equipmentSystem.getEquippedItem(playerId)).thenReturn(Optional.of(handgun));
        
        Transform playerTransform = new Transform();
        playerTransform.setX(0);
        playerTransform.setY(0);
        when(transformSystem.getTransform(playerId)).thenReturn(playerTransform);
        when(characteristicSystem.getValue(any(), eq("facing_angle"))).thenReturn(0.0);
        
        long bulletId = 200L;
        when(entitySystem.createEntity()).thenReturn(new Entity(bulletId));
        
        attackCaptor.getValue().accept(new BindingEvent(new BindingParameter("Attack", new BindingGroup("")), KeyEvent.Type.KEY_DOWN));

        // Setup enemy
        // Bullet starts at x=20. First sub-step moves it to x=28.
        // So we place enemy at x=28.
        long enemyId = 300L;
        Transform enemyTransform = new Transform();
        enemyTransform.setX(28); 
        enemyTransform.setY(0);
        when(worldSystem.getEntities(worldId)).thenReturn(List.of(playerId, bulletId, enemyId));
        when(transformSystem.getTransform(enemyId)).thenReturn(enemyTransform);
        
        when(characteristicSystem.getValue(any(Entity.class), eq("health"))).thenReturn(100.0);
        when(characteristicSystem.getValue(any(Entity.class), eq("is_ground_item"))).thenReturn(0.0);

        handgunSystem.tick();

        // Now it should hit
        verify(worldSystem, atLeastOnce()).removeEntityFromWorld(eq(worldId), eq(bulletId));
        verify(characteristicSystem, atLeastOnce()).setValue(argThat(e -> e.id() == enemyId), eq("health"), anyDouble());
    }

    @Test
    void testAnimationCleanup() throws InterruptedException {
        // Trigger animation
        handgunSystem.init();
        ArgumentCaptor<Consumer<BindingEvent>> attackCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager, atLeastOnce()).subscribe(eq(BindingEvent.class), attackCaptor.capture());

        InventoryItem handgun = new InventoryItem("handgun_01", "Pistol", "Type", "handgun", null, null, false);
        when(equipmentSystem.getEquippedItem(playerId)).thenReturn(Optional.of(handgun));
        when(transformSystem.getTransform(playerId)).thenReturn(new Transform());
        when(entitySystem.createEntity()).thenReturn(new Entity(200L));
        
        attackCaptor.getValue().accept(new BindingEvent(new BindingParameter("Attack", new BindingGroup("")), KeyEvent.Type.KEY_DOWN));
        
        when(characteristicSystem.hasCharacteristic(any(), eq("animation_state"))).thenReturn(true);

        // Wait for animation to finish (250ms)
        Thread.sleep(300);
        
        handgunSystem.tick();
        
        verify(characteristicSystem, atLeastOnce()).setValue(argThat(e -> e.id() == playerId), eq("animation_state"), eq("idle"));
    }

    @Test
    void testIsHandgunLogic() {
        handgunSystem.init();
        ArgumentCaptor<Consumer<ItemInteractEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), captor.capture());

        // Test various matches
        InventoryItem h1 = new InventoryItem("pistol_01", "Pistol", "Type", "weapon", null, null, false);
        InventoryItem h2 = new InventoryItem("some_handgun", "Handgun", "Type", "weapon", null, null, false);
        InventoryItem h3 = new InventoryItem("item_01", "Pistol", "Type", "pistol", null, null, false);
        InventoryItem h4 = new InventoryItem("item_02", "Handgun", "Type", "handgun", null, null, false);
        
        Entity entity = new Entity(playerId);
        
        // These should trigger equip/unequip
        captor.getValue().accept(new ItemInteractEvent(entity, h1));
        captor.getValue().accept(new ItemInteractEvent(entity, h2));
        captor.getValue().accept(new ItemInteractEvent(entity, h3));
        captor.getValue().accept(new ItemInteractEvent(entity, h4));
        
        verify(equipmentSystem, times(4)).getEquippedItem(playerId);
    }

    @Test
    void testBulletMaxRange() {
        handgunSystem.init();
        ArgumentCaptor<Consumer<BindingEvent>> attackCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager, atLeastOnce()).subscribe(eq(BindingEvent.class), attackCaptor.capture());

        InventoryItem handgun = new InventoryItem("handgun_01", "Pistol", "Type", "handgun", null, null, false);
        when(equipmentSystem.getEquippedItem(playerId)).thenReturn(Optional.of(handgun));
        when(transformSystem.getTransform(playerId)).thenReturn(new Transform());
        when(entitySystem.createEntity()).thenReturn(new Entity(200L));
        
        attackCaptor.getValue().accept(new BindingEvent(new BindingParameter("Attack", new BindingGroup("")), KeyEvent.Type.KEY_DOWN));

        when(worldSystem.getEntities(worldId)).thenReturn(Collections.emptyList());
        
        // Run tick many times to exceed max range
        for (int i = 0; i < 40; i++) {
            handgunSystem.tick();
        }
        
        verify(worldSystem, atLeastOnce()).removeEntityFromWorld(eq(worldId), eq(200L));
    }
}
