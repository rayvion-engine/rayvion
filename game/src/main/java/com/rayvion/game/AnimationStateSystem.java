package com.rayvion.game;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.entity.EntitySystem;
import com.rayvion.engine.graphics.EntityGraphics;
import com.rayvion.engine.graphics.GraphicsSystem;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.Tickable;
import com.rayvion.engine.system.dependency.SystemDependency;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitRequirement;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class AnimationStateSystem implements System, Tickable {

    private final SystemDescriptor descriptor;
    private CharacteristicSystem characteristicSystem;
    private GraphicsSystem graphicsSystem;
    private EntitySystem entitySystem;

    // Track last seen states to only update when they change.
    private static class EntityState {
        final String action;
        final String equipment;

        EntityState(String action, String equipment) {
            this.action = action;
            this.equipment = equipment;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EntityState that)) return false;
            return Objects.equals(action, that.action) && Objects.equals(equipment, that.equipment);
        }

        @Override
        public int hashCode() {
            return Objects.hash(action, equipment);
        }
    }

    private final Map<Long, EntityState> lastStates = new ConcurrentHashMap<>();

    public AnimationStateSystem() {
        this.descriptor = new SystemDescriptor(
                new SystemCoordinate("com.rayvion.game", "animation-state", Version.parse("1.0.0")),
                Set.of(
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "characteristic", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "graphics", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "entity", v -> true), SystemDependency.RequirementLevel.REQUIRED)
                ),
                Set.of(Tickable.TRAIT)
        );
    }

    @Override
    public SystemDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void onDependencyAdded(System dependency) {
        if (dependency instanceof CharacteristicSystem cs) characteristicSystem = cs;
        else if (dependency instanceof GraphicsSystem gs) graphicsSystem = gs;
        else if (dependency instanceof EntitySystem es) entitySystem = es;
    }

    @Override
    public void init() {}

    @Override
    @SuppressWarnings("unchecked")
    public void tick() {
        if (characteristicSystem == null || graphicsSystem == null || entitySystem == null) return;

        for (Entity entity : entitySystem.getEntities()) {
            if (!characteristicSystem.hasCharacteristic(entity, "animation_map")) continue;
            
            long entityId = entity.id();
            Map<String, EntityGraphics> animMap = characteristicSystem.getValue(entity, "animation_map");
            if (animMap == null || animMap.isEmpty()) continue;

            String state = characteristicSystem.getValue(entity, "animation_state");
            String equip = characteristicSystem.getValue(entity, "equipment_state");
            
            if (state == null) state = "idle";
            if (equip == null) equip = "unarmed";
            
            EntityState currentState = new EntityState(state, equip);
            EntityState lastState = lastStates.get(entityId);
            
            String key = state + "_" + equip;
            EntityGraphics resolvedGfx = animMap.get(key);
            if (resolvedGfx == null) resolvedGfx = animMap.get(state);
            
            if (resolvedGfx != null) {
                EntityGraphics currentGfx = graphicsSystem.getEntityGraphics(entityId);

                if (lastState == null || !currentState.equals(lastState) || currentGfx != null && !currentGfx.equals(resolvedGfx) && animMap.containsValue(currentGfx)) {
                    graphicsSystem.setEntityGraphics(entityId, resolvedGfx);
                    lastStates.put(entityId, currentState);
                }
            }
        }
    }
}
