package com.rayvion.game.mechanism;

import com.rayvion.engine.audio.AudioSystem;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.entity.EntitySystem;
import com.rayvion.engine.graphics.GraphicsSystem;
import com.rayvion.engine.graphics.TextureGraphics;
import com.rayvion.engine.physics.PhysicsSystem;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import com.rayvion.engine.inventory.InventorySystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MechanismSystemTest {

    private MechanismSystem mechanismSystem;
    private EntitySystem entitySystem;
    private TransformSystem transformSystem;
    private GraphicsSystem graphicsSystem;
    private PhysicsSystem physicsSystem;
    private AudioSystem audioSystem;
    private InventorySystem inventorySystem;
    private final long worldId = 1L;

    @BeforeEach
    void setUp() {
        entitySystem = mock(EntitySystem.class);
        transformSystem = mock(TransformSystem.class);
        graphicsSystem = mock(GraphicsSystem.class);
        physicsSystem = mock(PhysicsSystem.class);
        audioSystem = mock(AudioSystem.class);
        inventorySystem = mock(InventorySystem.class);

        mechanismSystem = new MechanismSystem(
                entitySystem,
                transformSystem,
                graphicsSystem,
                physicsSystem,
                audioSystem,
                inventorySystem,
                worldId
        );
    }

    @Test
    void testDescriptor() {
        var descriptor = mechanismSystem.getDescriptor();
        assertNotNull(descriptor);
        assertEquals("mechanism", descriptor.coordinate().id());
        assertEquals("com.rayvion.game", descriptor.coordinate().namespaceId());
        assertTrue(descriptor.provides().contains(com.rayvion.engine.system.Tickable.TRAIT));
    }

    @Test
    void testInit() {
        // Init is empty, but call it for coverage
        mechanismSystem.init();
    }

    @Test
    void testGetTickDelay() {
        assertEquals(Duration.ofMillis(200), mechanismSystem.getTickDelay());
    }

    @Test
    void testTryInteract_NoTransform() {
        Entity interactor = new Entity(100L);
        when(transformSystem.hasTransform(100L)).thenReturn(false);

        mechanismSystem.tryInteract(interactor);

        verify(transformSystem, never()).getTransform(anyLong());
    }

    @Test
    void testTryInteract_InRange() {
        long leverId = 200L;
        long gateId = 300L;
        mechanismSystem.registerLever(leverId, gateId);

        Entity interactor = new Entity(100L);
        Transform interactorTransform = new Transform();
        interactorTransform.setX(0);
        interactorTransform.setY(0);

        Transform leverTransform = new Transform();
        leverTransform.setX(10);
        leverTransform.setY(10); // Dist ~14.14, range is 48.0

        when(transformSystem.hasTransform(100L)).thenReturn(true);
        when(transformSystem.getTransform(100L)).thenReturn(interactorTransform);
        when(transformSystem.hasTransform(leverId)).thenReturn(true);
        when(transformSystem.getTransform(leverId)).thenReturn(leverTransform);

        mechanismSystem.tryInteract(interactor);

        // Verify levr pulld
        verify(graphicsSystem).setEntityGraphics(eq(leverId), any(TextureGraphics.class));
        verify(audioSystem).playSound("pickup");
        verify(graphicsSystem).setEntityGraphics(eq(gateId), any(TextureGraphics.class));
    }

    @Test
    void testTryInteract_OutOfRange() {
        long leverId = 200L;
        long gateId = 300L;
        mechanismSystem.registerLever(leverId, gateId);

        Entity interactor = new Entity(100L);
        Transform interactorTransform = new Transform();
        interactorTransform.setX(0);
        interactorTransform.setY(0);

        Transform leverTransform = new Transform();
        leverTransform.setX(100);
        leverTransform.setY(100); // Dist > 48.0

        when(transformSystem.hasTransform(100L)).thenReturn(true);
        when(transformSystem.getTransform(100L)).thenReturn(interactorTransform);
        when(transformSystem.hasTransform(leverId)).thenReturn(true);
        when(transformSystem.getTransform(leverId)).thenReturn(leverTransform);

        mechanismSystem.tryInteract(interactor);

        verify(graphicsSystem, never()).setEntityGraphics(anyLong(), any());
    }

    @Test
    void testTryInteract_AlreadyPulled() {
        long leverId = 200L;
        long gateId = 300L;
        mechanismSystem.registerLever(leverId, gateId);

        Entity interactor = new Entity(100L);
        Transform interactorTransform = new Transform();
        interactorTransform.setX(0);
        interactorTransform.setY(0);

        Transform leverTransform = new Transform();
        leverTransform.setX(10);
        leverTransform.setY(10);

        when(transformSystem.hasTransform(100L)).thenReturn(true);
        when(transformSystem.getTransform(100L)).thenReturn(interactorTransform);
        when(transformSystem.hasTransform(leverId)).thenReturn(true);
        when(transformSystem.getTransform(leverId)).thenReturn(leverTransform);

        // First interaction
        mechanismSystem.tryInteract(interactor);
        reset(graphicsSystem, audioSystem);

        // Second interaction
        mechanismSystem.tryInteract(interactor);

        verify(graphicsSystem, never()).setEntityGraphics(anyLong(), any());
    }

    @Test
    void testTryInteract_ClosestLever() {
        long lever1 = 201L;
        long lever2 = 202L;
        mechanismSystem.registerLever(lever1, 301L);
        mechanismSystem.registerLever(lever2, 302L);

        Entity interactor = new Entity(100L);
        Transform interactorTransform = new Transform();
        interactorTransform.setX(0);
        interactorTransform.setY(0);

        Transform transform1 = new Transform();
        transform1.setX(20);
        transform1.setY(20); // dist ~28

        Transform transform2 = new Transform();
        transform2.setX(10);
        transform2.setY(10); // dist ~14 (closest)

        when(transformSystem.hasTransform(100L)).thenReturn(true);
        when(transformSystem.getTransform(100L)).thenReturn(interactorTransform);
        
        when(transformSystem.hasTransform(lever1)).thenReturn(true);
        when(transformSystem.getTransform(lever1)).thenReturn(transform1);
        
        when(transformSystem.hasTransform(lever2)).thenReturn(true);
        when(transformSystem.getTransform(lever2)).thenReturn(transform2);

        mechanismSystem.tryInteract(interactor);

        // Should pull lever 2 (closest)
        verify(graphicsSystem).setEntityGraphics(eq(lever2), any(TextureGraphics.class));
        verify(graphicsSystem, never()).setEntityGraphics(eq(lever1), any(TextureGraphics.class));
    }

    @Test
    void testTryInteract_LeverMissingTransform() {
        long leverId = 200L;
        mechanismSystem.registerLever(leverId, 300L);

        Entity interactor = new Entity(100L);
        when(transformSystem.hasTransform(100L)).thenReturn(true);
        when(transformSystem.getTransform(100L)).thenReturn(new Transform());

        when(transformSystem.hasTransform(leverId)).thenReturn(false);

        mechanismSystem.tryInteract(interactor);

        verify(graphicsSystem, never()).setEntityGraphics(anyLong(), any());
    }

    @Test
    void testPullLever_NoAudio() {
        mechanismSystem = new MechanismSystem(
                entitySystem, transformSystem, graphicsSystem, physicsSystem,
                null, inventorySystem, worldId
        );
        long leverId = 200L;
        mechanismSystem.registerLever(leverId, 300L);

        Entity interactor = new Entity(100L);
        when(transformSystem.hasTransform(100L)).thenReturn(true);
        when(transformSystem.getTransform(100L)).thenReturn(new Transform());
        when(transformSystem.hasTransform(leverId)).thenReturn(true);
        when(transformSystem.getTransform(leverId)).thenReturn(new Transform());

        mechanismSystem.tryInteract(interactor);

        verify(graphicsSystem).setEntityGraphics(eq(leverId), any(TextureGraphics.class));
        // No crash should occur
    }

    @Test
    void testGateOpeningAnimationAndCompletion() {
        long leverId = 200L;
        long gateId = 300L;
        mechanismSystem.registerLever(leverId, gateId);

        // Pull lever to start opening
        when(transformSystem.hasTransform(anyLong())).thenReturn(true);
        when(transformSystem.getTransform(anyLong())).thenReturn(new Transform());
        mechanismSystem.tryInteract(new Entity(100L));

        // After pull, progress is 1, graphics set to gate_opening_1
        verify(graphicsSystem).setEntityGraphics(eq(gateId), argThat(tg -> ((TextureGraphics)tg).textureId().equals("gate_opening_1")));

        // First tick: progress 1 -> 2
        mechanismSystem.tick();
        verify(graphicsSystem).setEntityGraphics(eq(gateId), argThat(tg -> ((TextureGraphics)tg).textureId().equals("gate_opening_2")));

        // Second tick: progress 2 -> 3
        mechanismSystem.tick();
        verify(graphicsSystem).setEntityGraphics(eq(gateId), argThat(tg -> ((TextureGraphics)tg).textureId().equals("gate_open")));
        verify(physicsSystem).removeBody(worldId, gateId);

        // Third tick: progress is 3, nothing more should happen
        reset(graphicsSystem, physicsSystem);
        mechanismSystem.tick();
        verify(graphicsSystem, never()).setEntityGraphics(eq(gateId), any());
        verify(physicsSystem, never()).removeBody(anyLong(), anyLong());
    }

    @Test
    void testInteractionPrompts() {
        long leverId = 200L;
        mechanismSystem.registerLever(leverId, 300L);

        Entity interactor = new Entity(100L);
        Transform interactorTransform = new Transform();
        interactorTransform.setX(0);
        interactorTransform.setY(0);

        Transform leverTransform = new Transform();
        leverTransform.setX(10);
        leverTransform.setY(10);

        when(inventorySystem.getEntitiesWithInventory()).thenReturn(List.of(interactor));
        when(transformSystem.hasTransform(100L)).thenReturn(true);
        when(transformSystem.getTransform(100L)).thenReturn(interactorTransform);
        when(transformSystem.hasTransform(leverId)).thenReturn(true);
        when(transformSystem.getTransform(leverId)).thenReturn(leverTransform);

        // Tick to set prompt
        mechanismSystem.tick();
        verify(graphicsSystem).setInteractionPrompt(leverId, "[E] Pull Lever");

        // Move away
        interactorTransform.setX(100);
        mechanismSystem.tick();
        verify(graphicsSystem).removeInteractionPrompt(leverId);

        // Move back
        interactorTransform.setX(0);
        mechanismSystem.tick();
        verify(graphicsSystem, times(2)).setInteractionPrompt(leverId, "[E] Pull Lever");

        // Pull lever
        mechanismSystem.tryInteract(interactor);
        mechanismSystem.tick();
        // Should remove prompt because lever is pulled
        verify(graphicsSystem, times(2)).removeInteractionPrompt(leverId);
    }

    @Test
    void testInteractionPrompts_MultipleInteractors() {
        long leverId = 200L;
        mechanismSystem.registerLever(leverId, 300L);

        Entity interactor1 = new Entity(101L);
        Entity interactor2 = new Entity(102L);
        
        Transform t1 = new Transform(); t1.setX(100); // Far
        Transform t2 = new Transform(); t2.setX(0);   // Near
        
        when(inventorySystem.getEntitiesWithInventory()).thenReturn(List.of(interactor1, interactor2));
        
        when(transformSystem.hasTransform(101L)).thenReturn(true);
        when(transformSystem.getTransform(101L)).thenReturn(t1);
        when(transformSystem.hasTransform(102L)).thenReturn(true);
        when(transformSystem.getTransform(102L)).thenReturn(t2);
        
        when(transformSystem.hasTransform(leverId)).thenReturn(true);
        when(transformSystem.getTransform(leverId)).thenReturn(new Transform());

        mechanismSystem.tick();
        verify(graphicsSystem).setInteractionPrompt(leverId, "[E] Pull Lever");
    }

    @Test
    void testInteractionPrompts_NoTransform() {
        long leverId = 200L;
        mechanismSystem.registerLever(leverId, 300L);
        Entity interactor = new Entity(100L);
        when(inventorySystem.getEntitiesWithInventory()).thenReturn(List.of(interactor));
        when(transformSystem.hasTransform(100L)).thenReturn(false);

        mechanismSystem.tick();
        verify(graphicsSystem).removeInteractionPrompt(leverId);
    }

    @Test
    void testInteractionPrompts_LeverNoTransform() {
        long leverId = 200L;
        mechanismSystem.registerLever(leverId, 300L);
        Entity interactor = new Entity(100L);
        when(inventorySystem.getEntitiesWithInventory()).thenReturn(List.of(interactor));
        when(transformSystem.hasTransform(100L)).thenReturn(true);
        when(transformSystem.getTransform(100L)).thenReturn(new Transform());
        when(transformSystem.hasTransform(leverId)).thenReturn(false);

        mechanismSystem.tick();
        verify(graphicsSystem).removeInteractionPrompt(leverId);
    }

    @Test
    void testPullLever_NoGate() {
        long leverId = 200L;
        mechanismSystem.registerLever(leverId, -1L); // Lever with no gate

        Entity interactor = new Entity(100L);
        when(transformSystem.hasTransform(100L)).thenReturn(true);
        when(transformSystem.getTransform(100L)).thenReturn(new Transform());
        when(transformSystem.hasTransform(leverId)).thenReturn(true);
        when(transformSystem.getTransform(leverId)).thenReturn(new Transform());

        mechanismSystem.tryInteract(interactor);

        verify(graphicsSystem).setEntityGraphics(eq(leverId), any(TextureGraphics.class));
        verify(graphicsSystem, never()).setEntityGraphics(eq(-1L), any());
    }

    @Test
    void testInteractionPrompts_NoInteractors() {
        long leverId = 200L;
        mechanismSystem.registerLever(leverId, 300L);
        
        when(inventorySystem.getEntitiesWithInventory()).thenReturn(Collections.emptyList());

        mechanismSystem.tick();
        verify(graphicsSystem).removeInteractionPrompt(leverId);
    }
}
