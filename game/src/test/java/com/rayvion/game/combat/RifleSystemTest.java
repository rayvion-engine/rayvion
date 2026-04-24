package com.rayvion.game.combat;

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
import com.rayvion.engine.physics.PhysicsBody;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import com.rayvion.engine.world.WorldSystem;
import com.rayvion.engine.audio.AudioSystem;
import com.rayvion.engine.graphics.CameraSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RifleSystemTest {

    private RifleSystem rifleSystem;
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
    private CameraSystem cameraSystem;

    @BeforeEach
    void setUp() {
        rifleSystem = new RifleSystem(worldId, playerId);
        
        equipmentSystem = mock(EquipmentSystem.class);
        worldSystem = mock(WorldSystem.class);
        transformSystem = mock(TransformSystem.class);
        characteristicSystem = mock(CharacteristicSystem.class);
        graphicsSystem = mock(GraphicsSystem.class);
        eventManager = mock(EventManager.class);
        physicsSystem = mock(PhysicsSystem.class);
        entitySystem = mock(EntitySystem.class);
        audioSystem = mock(AudioSystem.class);
        cameraSystem = mock(CameraSystem.class);

        // Inject dependencies
        rifleSystem.onDependencyAdded(equipmentSystem);
        rifleSystem.onDependencyAdded(worldSystem);
        rifleSystem.onDependencyAdded(transformSystem);
        rifleSystem.onDependencyAdded(characteristicSystem);
        rifleSystem.onDependencyAdded(graphicsSystem);
        rifleSystem.onDependencyAdded(eventManager);
        rifleSystem.onDependencyAdded(physicsSystem);
        rifleSystem.onDependencyAdded(entitySystem);
        rifleSystem.onDependencyAdded(audioSystem);
        rifleSystem.onDependencyAdded(cameraSystem);
    }

    @Test
    void testGetDescriptor() {
        var descriptor = rifleSystem.getDescriptor();
        assertNotNull(descriptor);
        assertEquals("rifle-combat", descriptor.coordinate().id());
        assertTrue(descriptor.provides().stream().anyMatch(t -> t.id().equals("tickable")));
    }

    @Test
    void testOnUnknownDependencyAdded() {
        assertDoesNotThrow(() -> rifleSystem.onDependencyAdded(mock(com.rayvion.engine.system.System.class)));
    }

    @Test
    void testInit() {
        rifleSystem.init();
        verify(eventManager).subscribe(eq(BindingEvent.class), any());
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), any());
    }

    @Test
    void testInitWithoutEventManager() {
        RifleSystem standalone = new RifleSystem(worldId, playerId);
        assertDoesNotThrow(standalone::init);
    }

    @Test
    void testHandleItemInteractEquipRifle() {
        rifleSystem.init();
        ArgumentCaptor<Consumer<ItemInteractEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), captor.capture());

        InventoryItem rifle = mock(InventoryItem.class);
        when(rifle.id()).thenReturn("laser_rifle");
        when(rifle.type()).thenReturn("rifle");
        
        Entity player = new Entity(playerId);
        ItemInteractEvent event = new ItemInteractEvent(player, rifle);

        when(equipmentSystem.getEquippedItem(playerId)).thenReturn(Optional.empty());

        captor.getValue().accept(event);

        verify(equipmentSystem).equip(playerId, rifle);
    }

    @Test
    void testHandleItemInteractUnequipRifle() {
        rifleSystem.init();
        ArgumentCaptor<Consumer<ItemInteractEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), captor.capture());

        InventoryItem rifle = mock(InventoryItem.class);
        when(rifle.id()).thenReturn("standard_rifle");
        when(rifle.type()).thenReturn("rifle");
        
        Entity player = new Entity(playerId);
        ItemInteractEvent event = new ItemInteractEvent(player, rifle);

        when(equipmentSystem.getEquippedItem(playerId)).thenReturn(Optional.of(rifle));

        captor.getValue().accept(event);

        verify(equipmentSystem).unequip(playerId);
    }

    @Test
    void testHandleItemInteractIgnoreNonRifle() {
        rifleSystem.init();
        ArgumentCaptor<Consumer<ItemInteractEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), captor.capture());

        InventoryItem sword = mock(InventoryItem.class);
        when(sword.id()).thenReturn("iron_sword");
        when(sword.type()).thenReturn("melee");
        
        Entity player = new Entity(playerId);
        ItemInteractEvent event = new ItemInteractEvent(player, sword);

        captor.getValue().accept(event);

        verify(equipmentSystem, never()).equip(anyLong(), any());
        verify(equipmentSystem, never()).unequip(anyLong());
    }

    @Test
    void testHandleBindingEventAttack() {
        rifleSystem.init();
        ArgumentCaptor<Consumer<BindingEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(BindingEvent.class), captor.capture());

        BindingEvent event = new BindingEvent(new com.rayvion.engine.bindings.BindingParameter("Attack", new com.rayvion.engine.bindings.BindingGroup("combat")), KeyEvent.Type.KEY_DOWN);
        
        // Setup for performAttack to not return early
        InventoryItem rifle = mock(InventoryItem.class);
        when(rifle.id()).thenReturn("rifle");
        when(rifle.type()).thenReturn("rifle");
        when(equipmentSystem.getEquippedItem(playerId)).thenReturn(Optional.of(rifle));
        
        Transform playerTransform = new Transform();
        when(transformSystem.getTransform(playerId)).thenReturn(playerTransform);
        
        when(entitySystem.createEntity()).thenReturn(new Entity(200L), new Entity(201L), new Entity(202L));

        captor.getValue().accept(event);

        verify(audioSystem).playSound("rifle_shoot");
    }

    @Test
    void testHandleBindingEventIgnoreOther() {
        rifleSystem.init();
        ArgumentCaptor<Consumer<BindingEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(BindingEvent.class), captor.capture());

        BindingEvent event = new BindingEvent(new com.rayvion.engine.bindings.BindingParameter("Jump", new com.rayvion.engine.bindings.BindingGroup("movement")), KeyEvent.Type.KEY_DOWN);
        captor.getValue().accept(event);

        verify(audioSystem, never()).playSound(anyString());
    }

    @Test
    void testPerformAttackCooldown() throws InterruptedException {
        rifleSystem.init();
        ArgumentCaptor<Consumer<BindingEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(BindingEvent.class), captor.capture());

        InventoryItem rifle = mock(InventoryItem.class);
        when(rifle.id()).thenReturn("rifle");
        when(rifle.type()).thenReturn("rifle");
        when(equipmentSystem.getEquippedItem(playerId)).thenReturn(Optional.of(rifle));
        
        Transform playerTransform = new Transform();
        when(transformSystem.getTransform(playerId)).thenReturn(playerTransform);
        when(entitySystem.createEntity()).thenReturn(new Entity(200L), new Entity(201L), new Entity(202L));

        BindingEvent event = new BindingEvent(new com.rayvion.engine.bindings.BindingParameter("Attack", new com.rayvion.engine.bindings.BindingGroup("combat")), KeyEvent.Type.KEY_DOWN);
        
        // First attack
        captor.getValue().accept(event);
        verify(audioSystem, times(1)).playSound("rifle_shoot");

        // Immediate second attack (should be blocked by cooldown)
        captor.getValue().accept(event);
        verify(audioSystem, times(1)).playSound("rifle_shoot");
    }

    @Test
    void testPerformAttackNoRifle() {
        rifleSystem.init();
        ArgumentCaptor<Consumer<BindingEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(BindingEvent.class), captor.capture());

        when(equipmentSystem.getEquippedItem(playerId)).thenReturn(Optional.empty());

        BindingEvent event = new BindingEvent(new com.rayvion.engine.bindings.BindingParameter("Attack", new com.rayvion.engine.bindings.BindingGroup("combat")), KeyEvent.Type.KEY_DOWN);
        captor.getValue().accept(event);

        verify(audioSystem, never()).playSound(anyString());
    }

    @Test
    void testPerformAttackWithRecoilAndCameraShake() {
        rifleSystem.init();
        ArgumentCaptor<Consumer<BindingEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(BindingEvent.class), captor.capture());

        InventoryItem rifle = mock(InventoryItem.class);
        when(rifle.id()).thenReturn("rifle");
        when(rifle.type()).thenReturn("rifle");
        when(equipmentSystem.getEquippedItem(playerId)).thenReturn(Optional.of(rifle));
        
        Transform playerTransform = new Transform();
        when(transformSystem.getTransform(playerId)).thenReturn(playerTransform);
        
        PhysicsBody playerBody = mock(PhysicsBody.class);
        when(physicsSystem.getBody(worldId, playerId)).thenReturn(playerBody);
        
        when(entitySystem.createEntity()).thenReturn(new Entity(200L), new Entity(201L), new Entity(202L));

        BindingEvent event = new BindingEvent(new com.rayvion.engine.bindings.BindingParameter("Attack", new com.rayvion.engine.bindings.BindingGroup("combat")), KeyEvent.Type.KEY_DOWN);
        captor.getValue().accept(event);

        verify(cameraSystem).shake(anyDouble(), anyLong());
        verify(playerBody).applyImpulse(anyDouble(), anyDouble());
    }

    @Test
    void testTickAnimationStateReset() throws InterruptedException {
        // Trigger an attack to set animation state and end time
        testHandleBindingEventAttack();
        
        Entity player = new Entity(playerId);
        when(characteristicSystem.hasCharacteristic(player, "animation_state")).thenReturn(true);

        // Tick immediately - should not reset yet
        rifleSystem.tick();
        verify(characteristicSystem, never()).setValue(player, "animation_state", "idle");

        // Wait for animation duration (150ms)
        Thread.sleep(200);
        
        rifleSystem.tick();
        verify(characteristicSystem).setValue(player, "animation_state", "idle");
    }

    @Test
    void testTickVisualEffectsUpdateAndDestruction() throws InterruptedException {
        // Trigger attack to spawn effects (muzzle flash and shell)
        testHandleBindingEventAttack();
        
        long flashId = 201L; // testHandleBindingEventAttack
        long shellId = 202L;
        
        Transform shellTransform = new Transform();
        shellTransform.setX(10);
        shellTransform.setY(10);
        when(transformSystem.getTransform(shellId)).thenReturn(shellTransform);
        
        // Tick - should update shell position
        rifleSystem.tick();
        
        assertTrue(shellTransform.getX() != 10 || shellTransform.getY() != 10);
        verify(transformSystem, atLeastOnce()).setTransform(eq(shellId), any());

        // Wait for effects to expre (flsh 100ms, shel 500ms)
        Thread.sleep(150);
        rifleSystem.tick();
        
        // Flash should be destroyed
        verify(worldSystem).removeEntityFromWorld(worldId, flashId);
        verify(entitySystem).removeEntity(flashId);

        Thread.sleep(400);
        rifleSystem.tick();
        
        // Shell should be destroyed
        verify(worldSystem).removeEntityFromWorld(worldId, shellId);
        verify(entitySystem).removeEntity(shellId);
    }

    @Test
    void testTickBulletMovementAndWallCollision() {
        // Trigger attack to spawn bullet
        testHandleBindingEventAttack();
        
        long bulletId = 200L;
        Transform bulletTransform = new Transform();
        when(transformSystem.getTransform(bulletId)).thenReturn(bulletTransform);
        
        // Mock wall at some distance
        when(physicsSystem.isPointBlocked(eq(worldId), anyDouble(), anyDouble())).thenReturn(false);
        
        // First tick - bullet moves
        rifleSystem.tick();
        verify(transformSystem, atLeastOnce()).setTransform(eq(bulletId), any());
        
        // Mock wall hit
        when(physicsSystem.isPointBlocked(eq(worldId), anyDouble(), anyDouble())).thenReturn(true);
        
        rifleSystem.tick();
        
        // Bullet should be destroyed
        verify(worldSystem).removeEntityFromWorld(worldId, bulletId);
        verify(entitySystem).removeEntity(bulletId);
    }

    @Test
    void testTickBulletEntityCollision() {
        // Trigger attack to spawn bullet
        testHandleBindingEventAttack();
        
        long bulletId = 200L;
        long enemyId = 500L;
        
        when(worldSystem.getEntities(worldId)).thenReturn(List.of(playerId, bulletId, enemyId));
        
        Transform bulletTransform = new Transform();
        when(transformSystem.getTransform(bulletId)).thenReturn(bulletTransform);
        
        Transform enemyTransform = new Transform();
        enemyTransform.setX(25); // Bullet spawns at ~20, moves ~20 per tick (400 speed * 0.05s)
        enemyTransform.setY(0);
        when(transformSystem.getTransform(enemyId)).thenReturn(enemyTransform);
        
        Entity enemy = new Entity(enemyId);
        when(characteristicSystem.getValue(enemy, "health")).thenReturn(100.0);
        when(characteristicSystem.getValue(enemy, "strength")).thenReturn(10.0); // Player strength
        when(characteristicSystem.getValue(new Entity(playerId), "strength")).thenReturn(10.0);
        
        // Mock no wall
        when(physicsSystem.isPointBlocked(anyLong(), anyDouble(), anyDouble())).thenReturn(false);

        rifleSystem.tick();
        
        // Enemy should take damage
        verify(characteristicSystem).setValue(eq(enemy), eq("health"), anyDouble());
        verify(graphicsSystem).setEntityGraphics(eq(enemyId), any(TextureGraphics.class));
        
        // Bullet should be destroyed
        verify(worldSystem).removeEntityFromWorld(worldId, bulletId);
        verify(entitySystem).removeEntity(bulletId);
    }

    @Test
    void testTickBulletMaxRange() {
        // Trigger attack to spawn bullet
        testHandleBindingEventAttack();
        
        long bulletId = 200L;
        Transform bulletTransform = new Transform();
        when(transformSystem.getTransform(bulletId)).thenReturn(bulletTransform);
        
        // Mock no wall or collision
        when(physicsSystem.isPointBlocked(anyLong(), anyDouble(), anyDouble())).thenReturn(false);
        when(worldSystem.getEntities(worldId)).thenReturn(List.of(playerId, bulletId));

        // Tick many times to reach max range (600.0)
        // Speed is 400.0, deltaTime 0.05s -> 20.0 per tick.
        // 600 / 20 = 30 ticks.
        for (int i = 0; i < 35; i++) {
            rifleSystem.tick();
        }
        
        // Bullet should be destroyed after max range
        verify(worldSystem).removeEntityFromWorld(worldId, bulletId);
        verify(entitySystem).removeEntity(bulletId);
    }

    @Test
    void testTickBulletIgnoreGroundItems() {
        // Trigger attack to spawn bullet
        testHandleBindingEventAttack();
        
        long bulletId = 200L;
        long itemId = 600L;
        
        when(worldSystem.getEntities(worldId)).thenReturn(List.of(playerId, bulletId, itemId));
        
        Transform bulletTransform = new Transform();
        when(transformSystem.getTransform(bulletId)).thenReturn(bulletTransform);
        
        Transform itemTransform = new Transform();
        itemTransform.setX(25);
        itemTransform.setY(0);
        when(transformSystem.getTransform(itemId)).thenReturn(itemTransform);
        
        Entity item = new Entity(itemId);
        when(characteristicSystem.getValue(item, "is_ground_item")).thenReturn(1.0);
        
        // Mock no wall
        when(physicsSystem.isPointBlocked(anyLong(), anyDouble(), anyDouble())).thenReturn(false);

        rifleSystem.tick();
        
        // Item should NOT take damage
        verify(characteristicSystem, never()).setValue(eq(item), eq("health"), anyDouble());
        
        // Bullet should NOT be destroyed yet
        verify(worldSystem, never()).removeEntityFromWorld(worldId, bulletId);
    }

    @Test
    void testIsRifleNamingConventions() {
        rifleSystem.init();
        ArgumentCaptor<Consumer<ItemInteractEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(ItemInteractEvent.class), captor.capture());

        InventoryItem rifle1 = mock(InventoryItem.class);
        when(rifle1.id()).thenReturn("RIFFLE");
        when(rifle1.type()).thenReturn("Gun");
        
        InventoryItem rifle2 = mock(InventoryItem.class);
        when(rifle2.id()).thenReturn("Sniper");
        when(rifle2.type()).thenReturn("riffle");

        when(equipmentSystem.getEquippedItem(playerId)).thenReturn(Optional.empty());

        captor.getValue().accept(new ItemInteractEvent(new Entity(playerId), rifle1));
        captor.getValue().accept(new ItemInteractEvent(new Entity(playerId), rifle2));

        verify(equipmentSystem, times(2)).equip(eq(playerId), any());
    }

    @Test
    void testPerformAttackMissingTransform() {
        rifleSystem.init();
        ArgumentCaptor<Consumer<BindingEvent>> captor = ArgumentCaptor.forClass(Consumer.class);
        verify(eventManager).subscribe(eq(BindingEvent.class), captor.capture());

        InventoryItem rifle = mock(InventoryItem.class);
        when(rifle.id()).thenReturn("rifle");
        when(rifle.type()).thenReturn("rifle");
        when(equipmentSystem.getEquippedItem(playerId)).thenReturn(Optional.of(rifle));
        
        when(transformSystem.getTransform(playerId)).thenReturn(null);

        BindingEvent event = new BindingEvent(new com.rayvion.engine.bindings.BindingParameter("Attack", new com.rayvion.engine.bindings.BindingGroup("combat")), KeyEvent.Type.KEY_DOWN);
        captor.getValue().accept(event);

        verify(entitySystem, never()).createEntity();
    }

    @Test
    void testTickMissingTransformOrHealth() {
        // Trigger attack to spawn bullet
        testHandleBindingEventAttack();
        
        long bulletId = 200L;
        long enemyId = 500L;
        
        when(worldSystem.getEntities(worldId)).thenReturn(List.of(playerId, bulletId, enemyId));
        
        // Bullet transform is null
        when(transformSystem.getTransform(bulletId)).thenReturn(null);
        
        // Enemy transform is null
        when(transformSystem.getTransform(enemyId)).thenReturn(null);
        
        rifleSystem.tick();
        
        // No crash, and no damage applied
        verify(characteristicSystem, never()).setValue(eq(new Entity(enemyId)), eq("health"), anyDouble());
    }

    @Test
    void testTickDeadEnemy() {
        // Trigger attack to spawn bullet
        testHandleBindingEventAttack();
        
        long bulletId = 200L;
        long enemyId = 500L;
        
        when(worldSystem.getEntities(worldId)).thenReturn(List.of(playerId, bulletId, enemyId));
        
        Transform bulletTransform = new Transform();
        when(transformSystem.getTransform(bulletId)).thenReturn(bulletTransform);
        
        Transform enemyTransform = new Transform();
        enemyTransform.setX(25);
        enemyTransform.setY(0);
        when(transformSystem.getTransform(enemyId)).thenReturn(enemyTransform);
        
        Entity enemy = new Entity(enemyId);
        when(characteristicSystem.getValue(enemy, "health")).thenReturn(0.0); // Already dead
        
        rifleSystem.tick();
        
        // No damage applied
        verify(characteristicSystem, never()).setValue(eq(enemy), eq("health"), anyDouble());
    }
}
