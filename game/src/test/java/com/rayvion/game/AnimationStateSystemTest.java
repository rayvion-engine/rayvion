package com.rayvion.game;
// Tst for animaton state managment

import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.entity.EntitySystem;
import com.rayvion.engine.graphics.EntityGraphics;
import com.rayvion.engine.graphics.GraphicsSystem;
import com.rayvion.engine.graphics.TextureGraphics;
import com.rayvion.engine.system.Tickable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AnimationStateSystemTest {

    private static final Entity ENTITY = new Entity(42L);

    private AnimationStateSystem animationStateSystem;
    private CharacteristicSystem characteristicSystem;
    private GraphicsSystem graphicsSystem;
    private EntitySystem entitySystem;

    @BeforeEach
    void setUp() {
        animationStateSystem = new AnimationStateSystem();
        characteristicSystem = mock(CharacteristicSystem.class);
        graphicsSystem = mock(GraphicsSystem.class);
        entitySystem = mock(EntitySystem.class);
    }

    @Test
    void getDescriptor_exposesExpectedMetadata() {
        var descriptor = animationStateSystem.getDescriptor();

        assertNotNull(descriptor);
        assertEquals("animation-state", descriptor.coordinate().id());
        assertTrue(descriptor.dependencies().stream().anyMatch(d -> d.traitRequirement().id().equals("characteristic")));
        assertTrue(descriptor.dependencies().stream().anyMatch(d -> d.traitRequirement().id().equals("graphics")));
        assertTrue(descriptor.dependencies().stream().anyMatch(d -> d.traitRequirement().id().equals("entity")));
        assertTrue(descriptor.provides().contains(Tickable.TRAIT));
    }

    @Test
    void onDependencyAdded_andInit_acceptKnownDependencies() {
        animationStateSystem.onDependencyAdded(characteristicSystem);
        animationStateSystem.onDependencyAdded(graphicsSystem);
        animationStateSystem.onDependencyAdded(entitySystem);

        assertDoesNotThrow(() -> animationStateSystem.onDependencyAdded(mock(com.rayvion.engine.system.System.class)));
        assertDoesNotThrow(() -> animationStateSystem.init());
    }

    @Test
    void tick_returnsEarlyWhenRequiredDependenciesAreMissing() {
        animationStateSystem.tick();
        verifyNoInteractions(graphicsSystem, entitySystem, characteristicSystem);

        animationStateSystem.onDependencyAdded(characteristicSystem);
        animationStateSystem.tick();
        verifyNoInteractions(graphicsSystem, entitySystem);

        animationStateSystem.onDependencyAdded(graphicsSystem);
        animationStateSystem.tick();
        verifyNoInteractions(entitySystem);
    }

    @Test
    void tick_skipsEntitiesWithoutUsableAnimationMap() {
        animationStateSystem.onDependencyAdded(characteristicSystem);
        animationStateSystem.onDependencyAdded(graphicsSystem);
        animationStateSystem.onDependencyAdded(entitySystem);

        Entity nullMapEntity = new Entity(1L);
        Entity emptyMapEntity = new Entity(2L);
        Entity noCharacteristicEntity = new Entity(3L);

        when(entitySystem.getEntities()).thenReturn(List.of(nullMapEntity, emptyMapEntity, noCharacteristicEntity));
        when(characteristicSystem.hasCharacteristic(nullMapEntity, "animation_map")).thenReturn(true);
        when(characteristicSystem.hasCharacteristic(emptyMapEntity, "animation_map")).thenReturn(true);
        when(characteristicSystem.hasCharacteristic(noCharacteristicEntity, "animation_map")).thenReturn(false);
        when(characteristicSystem.getValue(nullMapEntity, "animation_map")).thenReturn(null);
        when(characteristicSystem.getValue(emptyMapEntity, "animation_map")).thenReturn(Map.of());

        animationStateSystem.tick();

        verifyNoInteractions(graphicsSystem);
        verify(characteristicSystem, never()).getValue(any(Entity.class), eq("animation_state"));
        verify(characteristicSystem, never()).getValue(any(Entity.class), eq("equipment_state"));
    }

    @Test
    void tick_usesDefaultIdleAndUnarmedFallbackAndCachesState() {
        animationStateSystem.onDependencyAdded(characteristicSystem);
        animationStateSystem.onDependencyAdded(graphicsSystem);
        animationStateSystem.onDependencyAdded(entitySystem);

        EntityGraphics idleGraphics = new TextureGraphics("idle");
        when(entitySystem.getEntities()).thenReturn(List.of(ENTITY));
        when(characteristicSystem.hasCharacteristic(ENTITY, "animation_map")).thenReturn(true);
        when(characteristicSystem.getValue(ENTITY, "animation_map")).thenReturn(Map.of("idle", idleGraphics));
        when(characteristicSystem.getValue(ENTITY, "animation_state")).thenReturn(null);
        when(characteristicSystem.getValue(ENTITY, "equipment_state")).thenReturn(null);
        when(graphicsSystem.getEntityGraphics(ENTITY.id())).thenReturn(null, idleGraphics);

        animationStateSystem.tick();
        verify(graphicsSystem).setEntityGraphics(ENTITY.id(), idleGraphics);

        reset(graphicsSystem);
        when(graphicsSystem.getEntityGraphics(ENTITY.id())).thenReturn(idleGraphics);

        animationStateSystem.tick();
        verify(graphicsSystem, never()).setEntityGraphics(ENTITY.id(), idleGraphics);
    }

    @Test
    void tick_updatesGraphicsWhenStateChanges() {
        animationStateSystem.onDependencyAdded(characteristicSystem);
        animationStateSystem.onDependencyAdded(graphicsSystem);
        animationStateSystem.onDependencyAdded(entitySystem);

        EntityGraphics idleSword = new TextureGraphics("idle_sword");
        EntityGraphics moveSword = new TextureGraphics("move_sword");
        Map<String, EntityGraphics> animationMap = Map.of(
                "idle_sword", idleSword,
                "move_sword", moveSword
        );

        when(entitySystem.getEntities()).thenReturn(List.of(ENTITY));
        when(characteristicSystem.hasCharacteristic(ENTITY, "animation_map")).thenReturn(true);
        when(characteristicSystem.getValue(ENTITY, "animation_map")).thenReturn(animationMap);
        when(characteristicSystem.getValue(ENTITY, "animation_state")).thenReturn("idle", "move");
        when(characteristicSystem.getValue(ENTITY, "equipment_state")).thenReturn("sword", "sword");
        when(graphicsSystem.getEntityGraphics(ENTITY.id())).thenReturn(null, idleSword);

        animationStateSystem.tick();
        animationStateSystem.tick();

        verify(graphicsSystem).setEntityGraphics(ENTITY.id(), idleSword);
        verify(graphicsSystem).setEntityGraphics(ENTITY.id(), moveSword);
    }

    @Test
    void tick_replacesMappedGraphicsWhenCurrentGraphicsDriftedWithinAnimationMap() {
        animationStateSystem.onDependencyAdded(characteristicSystem);
        animationStateSystem.onDependencyAdded(graphicsSystem);
        animationStateSystem.onDependencyAdded(entitySystem);

        EntityGraphics idleSword = new TextureGraphics("idle_sword");
        EntityGraphics idleGun = new TextureGraphics("idle_gun");
        Map<String, EntityGraphics> animationMap = Map.of(
                "idle_sword", idleSword,
                "idle_gun", idleGun
        );

        when(entitySystem.getEntities()).thenReturn(List.of(ENTITY));
        when(characteristicSystem.hasCharacteristic(ENTITY, "animation_map")).thenReturn(true);
        when(characteristicSystem.getValue(ENTITY, "animation_map")).thenReturn(animationMap);
        when(characteristicSystem.getValue(ENTITY, "animation_state")).thenReturn("idle", "idle");
        when(characteristicSystem.getValue(ENTITY, "equipment_state")).thenReturn("sword", "sword");
        when(graphicsSystem.getEntityGraphics(ENTITY.id())).thenReturn(null, idleGun);

        animationStateSystem.tick();
        animationStateSystem.tick();

        verify(graphicsSystem, times(2)).setEntityGraphics(ENTITY.id(), idleSword);
    }

    @Test
    void tick_doesNotReplaceUnmappedCurrentGraphicsWhenStateIsUnchanged() {
        animationStateSystem.onDependencyAdded(characteristicSystem);
        animationStateSystem.onDependencyAdded(graphicsSystem);
        animationStateSystem.onDependencyAdded(entitySystem);

        EntityGraphics idleSword = new TextureGraphics("idle_sword");
        EntityGraphics externalGraphics = new TextureGraphics("external");
        Map<String, EntityGraphics> animationMap = Map.of("idle_sword", idleSword);

        when(entitySystem.getEntities()).thenReturn(List.of(ENTITY));
        when(characteristicSystem.hasCharacteristic(ENTITY, "animation_map")).thenReturn(true);
        when(characteristicSystem.getValue(ENTITY, "animation_map")).thenReturn(animationMap);
        when(characteristicSystem.getValue(ENTITY, "animation_state")).thenReturn("idle", "idle");
        when(characteristicSystem.getValue(ENTITY, "equipment_state")).thenReturn("sword", "sword");
        when(graphicsSystem.getEntityGraphics(ENTITY.id())).thenReturn(null, externalGraphics);

        animationStateSystem.tick();
        animationStateSystem.tick();

        verify(graphicsSystem).setEntityGraphics(ENTITY.id(), idleSword);
    }

    @Test
    void tick_skipsWhenNoResolvedGraphicsExists() {
        animationStateSystem.onDependencyAdded(characteristicSystem);
        animationStateSystem.onDependencyAdded(graphicsSystem);
        animationStateSystem.onDependencyAdded(entitySystem);

        when(entitySystem.getEntities()).thenReturn(List.of(ENTITY));
        when(characteristicSystem.hasCharacteristic(ENTITY, "animation_map")).thenReturn(true);
        when(characteristicSystem.getValue(ENTITY, "animation_map")).thenReturn(Map.of("other", new TextureGraphics("other")));
        when(characteristicSystem.getValue(ENTITY, "animation_state")).thenReturn("idle");
        when(characteristicSystem.getValue(ENTITY, "equipment_state")).thenReturn("sword");

        animationStateSystem.tick();

        verify(graphicsSystem, never()).getEntityGraphics(ENTITY.id());
        verify(graphicsSystem, never()).setEntityGraphics(eq(ENTITY.id()), any(EntityGraphics.class));
    }

    @Test
    void entityStateEqualityAndHashCodeBehaveConsistently() throws Exception {
        Object first = newEntityState("idle", "sword");
        Object same = newEntityState("idle", "sword");
        Object different = newEntityState("move", "sword");

        assertEquals(first, first);
        assertEquals(first, same);
        assertNotEquals(first, different);
        assertNotEquals(first, "not a state");
        assertEquals(Objects.hash("idle", "sword"), first.hashCode());
    }

    private static Object newEntityState(String action, String equipment) throws Exception {
        Class<?> clazz = Class.forName("com.rayvion.game.AnimationStateSystem$EntityState");
        Constructor<?> constructor = clazz.getDeclaredConstructor(String.class, String.class);
        constructor.setAccessible(true);
        return constructor.newInstance(action, equipment);
    }
}
