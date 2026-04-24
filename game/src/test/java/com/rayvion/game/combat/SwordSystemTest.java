package com.rayvion.game.combat;

import com.rayvion.engine.audio.AudioSystem;
import com.rayvion.engine.bindings.BindingEvent;
import com.rayvion.engine.bindings.BindingParameter;
import com.rayvion.engine.bindings.BindingGroup;
import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.entity.EntitySystem;
import com.rayvion.engine.equipment.EquipmentSystem;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.graphics.AnimationGraphics;
import com.rayvion.engine.graphics.GraphicsSystem;
import com.rayvion.engine.input.KeyEvent;
import com.rayvion.engine.inventory.InventoryItem;
import com.rayvion.engine.inventory.ItemInteractEvent;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import com.rayvion.engine.world.WorldSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SwordSystemTest {

    private SwordSystem swordSystem;
    private long worldId = 1L;
    private long playerEntityId = 100L;

    private EquipmentSystem equipmentSystem;
    private WorldSystem worldSystem;
    private TransformSystem transformSystem;
    private CharacteristicSystem characteristicSystem;
    private GraphicsSystem graphicsSystem;
    private EventManager eventManager;
    private AudioSystem audioSystem;
    private EntitySystem entitySystem;

    @BeforeEach
    void setUp() {
        swordSystem = new SwordSystem(worldId, playerEntityId);

        equipmentSystem = mock(EquipmentSystem.class);
        worldSystem = mock(WorldSystem.class);
        transformSystem = mock(TransformSystem.class);
        characteristicSystem = mock(CharacteristicSystem.class);
        graphicsSystem = mock(GraphicsSystem.class);
        eventManager = mock(EventManager.class);
        audioSystem = mock(AudioSystem.class);
        entitySystem = mock(EntitySystem.class);

        // Inject dependencies
        swordSystem.onDependencyAdded(equipmentSystem);
        swordSystem.onDependencyAdded(worldSystem);
        swordSystem.onDependencyAdded(transformSystem);
        swordSystem.onDependencyAdded(characteristicSystem);
        swordSystem.onDependencyAdded(graphicsSystem);
        swordSystem.onDependencyAdded(eventManager);
        swordSystem.onDependencyAdded(audioSystem);
        swordSystem.onDependencyAdded(entitySystem);
    }

    @Test
    void testGetDescriptor() {
        var descriptor = swordSystem.getDescriptor();
        assertNotNull(descriptor);
        assertEquals("sword-combat", descriptor.coordinate().id());
        assertTrue(descriptor.provides().contains(com.rayvion.engine.system.Tickable.TRAIT));
    }

    @Test
    void testInit() {
        swordSystem.init();
        verify(eventManager).registerEventType(EntityAttackEvent.class);
        verify(eventManager).subscribe(eq(BindingEvent.class), any());
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), any());
        verify(eventManager).subscribe(eq(EntityAttackEvent.class), any());
    }

    @Test
    void testGetTickDelay() {
        assertEquals(Duration.ofMillis(50), swordSystem.getTickDelay());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleItemInteract_EquipSword() {
        swordSystem.init();
        ArgumentCaptor<Consumer<ItemInteractEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), captor.capture());

        Entity entity = new Entity(playerEntityId);
        InventoryItem sword = mock(InventoryItem.class);
        when(sword.id()).thenReturn("iron_sword");
        ItemInteractEvent event = new ItemInteractEvent(entity, sword);

        when(equipmentSystem.getEquippedItem(playerEntityId)).thenReturn(Optional.empty());

        captor.getValue().accept(event);

        verify(equipmentSystem).equip(playerEntityId, sword);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleItemInteract_UnequipSword() {
        swordSystem.init();
        ArgumentCaptor<Consumer<ItemInteractEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), captor.capture());

        Entity entity = new Entity(playerEntityId);
        InventoryItem sword = mock(InventoryItem.class);
        when(sword.id()).thenReturn("iron_sword");
        ItemInteractEvent event = new ItemInteractEvent(entity, sword);

        when(equipmentSystem.getEquippedItem(playerEntityId)).thenReturn(Optional.of(sword));

        captor.getValue().accept(event);

        verify(equipmentSystem).unequip(playerEntityId);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleItemInteract_SwitchSword() {
        swordSystem.init();
        ArgumentCaptor<Consumer<ItemInteractEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), captor.capture());

        Entity entity = new Entity(playerEntityId);
        InventoryItem newSword = mock(InventoryItem.class);
        when(newSword.id()).thenReturn("steel_sword");
        
        InventoryItem oldSword = mock(InventoryItem.class);
        when(oldSword.id()).thenReturn("iron_sword");
        
        ItemInteractEvent event = new ItemInteractEvent(entity, newSword);

        when(equipmentSystem.getEquippedItem(playerEntityId)).thenReturn(Optional.of(oldSword));

        captor.getValue().accept(event);

        verify(equipmentSystem).equip(playerEntityId, newSword);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleBindingEvent_OtherEvents() {
        swordSystem.init();
        ArgumentCaptor<Consumer<BindingEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(BindingEvent.class), captor.capture());

        // Wrong type
        captor.getValue().accept(new BindingEvent(new BindingParameter("Attack", null), KeyEvent.Type.KEY_UP));
        verify(audioSystem, never()).playSound(anyString());

        // Wrong name
        captor.getValue().accept(new BindingEvent(new BindingParameter("Jump", null), KeyEvent.Type.KEY_DOWN));
        verify(audioSystem, never()).playSound(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testHandleEntityAttack() {
        swordSystem.init();
        ArgumentCaptor<Consumer<EntityAttackEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(EntityAttackEvent.class), captor.capture());

        long attackerId = 200L;
        EntityAttackEvent event = new EntityAttackEvent(attackerId);

        // Mock sword equipped
        InventoryItem sword = mock(InventoryItem.class);
        when(sword.id()).thenReturn("iron_sword");
        when(equipmentSystem.getEquippedItem(attackerId)).thenReturn(Optional.of(sword));

        captor.getValue().accept(event);

        verify(audioSystem).playSound("sword_swing");
    }

    @Test
    void testPerformAttack_Cooldown() {
        InventoryItem sword = mock(InventoryItem.class);
        when(sword.id()).thenReturn("iron_sword");
        when(equipmentSystem.getEquippedItem(playerEntityId)).thenReturn(Optional.of(sword));

        swordSystem.init();
        ArgumentCaptor<Consumer<EntityAttackEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(EntityAttackEvent.class), captor.capture());

        EntityAttackEvent event = new EntityAttackEvent(playerEntityId);

        // First attack
        captor.getValue().accept(event);
        verify(audioSystem, times(1)).playSound("sword_swing");

        // Immediate second attack (should be blocked by cooldown)
        captor.getValue().accept(event);
        // Verify it was STILL called only 1 time in total
        verify(audioSystem, times(1)).playSound("sword_swing"); 
    }

    @Test
    void testPerformAttack_NoSword() {
        when(equipmentSystem.getEquippedItem(playerEntityId)).thenReturn(Optional.empty());

        EntityAttackEvent event = new EntityAttackEvent(playerEntityId);
        swordSystem.init();
        ArgumentCaptor<Consumer<EntityAttackEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(EntityAttackEvent.class), captor.capture());

        captor.getValue().accept(event);

        verify(audioSystem, never()).playSound(anyString());
    }

    @Test
    void testPerformAttack_SuccessWithDamage() {
        // Setup attacker
        InventoryItem sword = mock(InventoryItem.class);
        when(sword.id()).thenReturn("iron_sword");
        when(sword.name()).thenReturn("Iron Sword");
        when(equipmentSystem.getEquippedItem(playerEntityId)).thenReturn(Optional.of(sword));

        Transform attackerTransform = new Transform();
        attackerTransform.setX(100);
        attackerTransform.setY(100);
        when(transformSystem.getTransform(playerEntityId)).thenReturn(attackerTransform);

        when(characteristicSystem.getValue(new Entity(playerEntityId), "facing_angle")).thenReturn(0.0); // Facing Right
        when(characteristicSystem.getValue(new Entity(playerEntityId), "strength")).thenReturn(20.0);

        // Setup enemy within range and cone
        long enemyId = 500L;
        Transform enemyTransform = new Transform();
        enemyTransform.setX(130); // 30 units away
        enemyTransform.setY(100);
        when(transformSystem.getTransform(enemyId)).thenReturn(enemyTransform);
        when(characteristicSystem.getValue(new Entity(enemyId), "health")).thenReturn(50.0);
        when(characteristicSystem.getValue(new Entity(enemyId), "defense")).thenReturn(5.0);
        when(characteristicSystem.getValue(new Entity(enemyId), "is_ground_item")).thenReturn(0.0);

        // Setup world entities
        when(worldSystem.getEntities(worldId)).thenReturn(List.of(playerEntityId, enemyId));

        // Mock slash entity creation
        Entity slashEntity = new Entity(999L);
        when(entitySystem.createEntity()).thenReturn(slashEntity);

        // Trigger attack
        EntityAttackEvent event = new EntityAttackEvent(playerEntityId);
        swordSystem.init();
        ArgumentCaptor<Consumer<EntityAttackEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(EntityAttackEvent.class), captor.capture());

        captor.getValue().accept(event);

        // Verify side effects
        verify(audioSystem).playSound("sword_swing");
        verify(characteristicSystem).setValue(new Entity(playerEntityId), "animation_state", "attack");
        
        // Verify slash spawned
        verify(entitySystem).createEntity();
        verify(worldSystem).addEntityToWorld(worldId, 999L);
        verify(transformSystem).setTransform(eq(999L), any(Transform.class));
        verify(graphicsSystem).setEntityGraphics(eq(999L), any(AnimationGraphics.class));

        // Verify damage dealt: strength(20) - defense(5) = 15 damage. 50 - 15 = 35.
        verify(characteristicSystem).setValue(new Entity(enemyId), "health", 35.0);
    }

    @Test
    void testPerformAttack_EnemyOutOfRange() {
        InventoryItem sword = mock(InventoryItem.class);
        when(sword.id()).thenReturn("iron_sword");
        when(equipmentSystem.getEquippedItem(playerEntityId)).thenReturn(Optional.of(sword));

        Transform attackerTransform = new Transform();
        attackerTransform.setX(100);
        attackerTransform.setY(100);
        when(transformSystem.getTransform(playerEntityId)).thenReturn(attackerTransform);

        // Enemy far away
        long enemyId = 500L;
        Transform enemyTransform = new Transform();
        enemyTransform.setX(200); 
        enemyTransform.setY(200);
        when(transformSystem.getTransform(enemyId)).thenReturn(enemyTransform);

        when(worldSystem.getEntities(worldId)).thenReturn(List.of(playerEntityId, enemyId));
        when(entitySystem.createEntity()).thenReturn(new Entity(999L));

        EntityAttackEvent event = new EntityAttackEvent(playerEntityId);
        swordSystem.init();
        ArgumentCaptor<Consumer<EntityAttackEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(EntityAttackEvent.class), captor.capture());

        captor.getValue().accept(event);

        // Health should not be updated
        verify(characteristicSystem, never()).setValue(eq(new Entity(enemyId)), eq("health"), anyDouble());
    }

    @Test
    void testPerformAttack_EnemyOutOfCone() {
        InventoryItem sword = mock(InventoryItem.class);
        when(sword.id()).thenReturn("iron_sword");
        when(equipmentSystem.getEquippedItem(playerEntityId)).thenReturn(Optional.of(sword));

        Transform attackerTransform = new Transform();
        attackerTransform.setX(100);
        attackerTransform.setY(100);
        when(transformSystem.getTransform(playerEntityId)).thenReturn(attackerTransform);
        when(characteristicSystem.getValue(new Entity(playerEntityId), "facing_angle")).thenReturn(0.0); // Facing Right

        // Enemy behind attacker
        long enemyId = 500L;
        Transform enemyTransform = new Transform();
        enemyTransform.setX(70); 
        enemyTransform.setY(100);
        when(transformSystem.getTransform(enemyId)).thenReturn(enemyTransform);
        when(characteristicSystem.getValue(new Entity(enemyId), "health")).thenReturn(50.0);
        when(characteristicSystem.getValue(new Entity(enemyId), "is_ground_item")).thenReturn(0.0);

        when(worldSystem.getEntities(worldId)).thenReturn(List.of(playerEntityId, enemyId));
        when(entitySystem.createEntity()).thenReturn(new Entity(999L));

        EntityAttackEvent event = new EntityAttackEvent(playerEntityId);
        swordSystem.init();
        ArgumentCaptor<Consumer<EntityAttackEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(EntityAttackEvent.class), captor.capture());

        captor.getValue().accept(event);

        // Health should not be updated
        verify(characteristicSystem, never()).setValue(eq(new Entity(enemyId)), eq("health"), anyDouble());
    }

    @Test
    void testTick_CleanupAndRevert() throws InterruptedException {
        // Trigger an attack to populate animationEndTimes
        InventoryItem sword = mock(InventoryItem.class);
        when(sword.id()).thenReturn("iron_sword");
        when(equipmentSystem.getEquippedItem(playerEntityId)).thenReturn(Optional.of(sword));
        when(transformSystem.getTransform(playerEntityId)).thenReturn(new Transform());
        when(entitySystem.createEntity()).thenReturn(new Entity(999L));
        when(worldSystem.getEntities(worldId)).thenReturn(List.of(playerEntityId, 999L));
        when(characteristicSystem.hasCharacteristic(new Entity(playerEntityId), "animation_state")).thenReturn(true);

        EntityAttackEvent event = new EntityAttackEvent(playerEntityId);
        swordSystem.init();
        ArgumentCaptor<Consumer<EntityAttackEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(EntityAttackEvent.class), captor.capture());
        captor.getValue().accept(event);

        // Wait for animation and slash to expire
        // Animation: 150ms, Slash: 250ms
        Thread.sleep(300);

        swordSystem.tick();

        // Verify animation reverted
        verify(characteristicSystem).setValue(new Entity(playerEntityId), "animation_state", "idle");

        // Verify slash cleaned up
        verify(worldSystem).removeEntityFromWorld(worldId, 999L);
        verify(transformSystem).removeTransform(999L);
        verify(graphicsSystem).removeEntityGraphics(999L);
        verify(entitySystem).removeEntity(999L);
    }

    @Test
    void testPerformAttack_GroundItemIgnored() {
        InventoryItem sword = mock(InventoryItem.class);
        when(sword.id()).thenReturn("iron_sword");
        when(equipmentSystem.getEquippedItem(playerEntityId)).thenReturn(Optional.of(sword));

        Transform attackerTransform = new Transform();
        attackerTransform.setX(100);
        attackerTransform.setY(100);
        when(transformSystem.getTransform(playerEntityId)).thenReturn(attackerTransform);

        long groundItemId = 500L;
        Transform itemTransform = new Transform();
        itemTransform.setX(110); 
        itemTransform.setY(100);
        when(transformSystem.getTransform(groundItemId)).thenReturn(itemTransform);
        when(characteristicSystem.getValue(new Entity(groundItemId), "is_ground_item")).thenReturn(1.0);

        when(worldSystem.getEntities(worldId)).thenReturn(List.of(playerEntityId, groundItemId));
        when(entitySystem.createEntity()).thenReturn(new Entity(999L));

        EntityAttackEvent event = new EntityAttackEvent(playerEntityId);
        swordSystem.init();
        ArgumentCaptor<Consumer<EntityAttackEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(EntityAttackEvent.class), captor.capture());

        captor.getValue().accept(event);

        verify(characteristicSystem, never()).setValue(eq(new Entity(groundItemId)), eq("health"), anyDouble());
    }

    @Test
    void testPerformAttack_EquippedNotSword() {
        InventoryItem rifle = mock(InventoryItem.class);
        when(rifle.id()).thenReturn("laser_rifle");
        when(equipmentSystem.getEquippedItem(playerEntityId)).thenReturn(Optional.of(rifle));

        EntityAttackEvent event = new EntityAttackEvent(playerEntityId);
        swordSystem.init();
        ArgumentCaptor<Consumer<EntityAttackEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(EntityAttackEvent.class), captor.capture());

        captor.getValue().accept(event);

        verify(audioSystem, never()).playSound(anyString());
    }

    @Test
    void testPerformAttack_EnemyIsDead() {
        InventoryItem sword = mock(InventoryItem.class);
        when(sword.id()).thenReturn("iron_sword");
        when(equipmentSystem.getEquippedItem(playerEntityId)).thenReturn(Optional.of(sword));

        Transform attackerTransform = new Transform();
        attackerTransform.setX(100);
        attackerTransform.setY(100);
        when(transformSystem.getTransform(playerEntityId)).thenReturn(attackerTransform);

        long deadEnemyId = 500L;
        Transform enemyTransform = new Transform();
        enemyTransform.setX(110); 
        enemyTransform.setY(100);
        when(transformSystem.getTransform(deadEnemyId)).thenReturn(enemyTransform);
        when(characteristicSystem.getValue(new Entity(deadEnemyId), "health")).thenReturn(0.0); // DEAD
        when(characteristicSystem.getValue(new Entity(deadEnemyId), "is_ground_item")).thenReturn(0.0);

        when(worldSystem.getEntities(worldId)).thenReturn(List.of(playerEntityId, deadEnemyId));
        when(entitySystem.createEntity()).thenReturn(new Entity(999L));

        EntityAttackEvent event = new EntityAttackEvent(playerEntityId);
        swordSystem.init();
        ArgumentCaptor<Consumer<EntityAttackEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(EntityAttackEvent.class), captor.capture());

        captor.getValue().accept(event);

        verify(characteristicSystem, never()).setValue(eq(new Entity(deadEnemyId)), eq("health"), anyDouble());
    }

    @Test
    void testPerformAttack_MissingTransform() {
        InventoryItem sword = mock(InventoryItem.class);
        when(sword.id()).thenReturn("iron_sword");
        when(equipmentSystem.getEquippedItem(playerEntityId)).thenReturn(Optional.of(sword));

        when(transformSystem.getTransform(playerEntityId)).thenReturn(null);

        EntityAttackEvent event = new EntityAttackEvent(playerEntityId);
        swordSystem.init();
        ArgumentCaptor<Consumer<EntityAttackEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(EntityAttackEvent.class), captor.capture());

        captor.getValue().accept(event);

        verify(audioSystem).playSound("sword_swing");
        verify(worldSystem, never()).getEntities(anyLong());
    }

    @Test
    void testOnDependencyAdded_All() {
        // Reset swordSystem to test fresh dependency injection
        swordSystem = new SwordSystem(worldId, playerEntityId);
        
        swordSystem.onDependencyAdded(equipmentSystem);
        swordSystem.onDependencyAdded(worldSystem);
        swordSystem.onDependencyAdded(transformSystem);
        swordSystem.onDependencyAdded(characteristicSystem);
        swordSystem.onDependencyAdded(graphicsSystem);
        swordSystem.onDependencyAdded(eventManager);
        swordSystem.onDependencyAdded(audioSystem);
        swordSystem.onDependencyAdded(entitySystem);
        
        // Verify via a side effect that depends on all systems
        // performAttack uses almost everything
        when(equipmentSystem.getEquippedItem(playerEntityId)).thenReturn(Optional.of(mock(InventoryItem.class)));
        // ... this is mostly to cover the lines in onDependencyAdded
        assertDoesNotThrow(() -> swordSystem.init());
    }

    @Test
    void testTick_CleanupMissingEntity() throws InterruptedException {
        // Trigger an attack to get a slash into animationEndTimes
        long slashId = 999L;
        when(entitySystem.createEntity()).thenReturn(new Entity(slashId));
        InventoryItem sword = mock(InventoryItem.class);
        when(sword.id()).thenReturn("iron_sword");
        when(equipmentSystem.getEquippedItem(playerEntityId)).thenReturn(Optional.of(sword));
        when(transformSystem.getTransform(playerEntityId)).thenReturn(new Transform());
        
        swordSystem.init();
        ArgumentCaptor<Consumer<EntityAttackEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(EntityAttackEvent.class), captor.capture());
        captor.getValue().accept(new EntityAttackEvent(playerEntityId));

        // Mock entity NOT in world anymore
        when(worldSystem.getEntities(worldId)).thenReturn(Collections.emptyList());
        when(characteristicSystem.hasCharacteristic(any(), eq("animation_state"))).thenReturn(false);

        Thread.sleep(300);
        swordSystem.tick();

        // Should NOT try to remove
        verify(worldSystem, never()).removeEntityFromWorld(eq(worldId), eq(slashId));
    }
}
