package com.rayvion.game.combat;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.characteristic.CharacteristicChangedEvent;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.Tickable;
import com.rayvion.engine.system.dependency.SystemDependency;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;
import com.rayvion.engine.system.trait.SystemTraitRequirement;
import com.rayvion.engine.audio.AudioSystem;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * System that tracks when entities take damage and provides feedback state for rendering.
 */
@Slf4j
public class DamageFeedbackSystem implements System, Tickable {

    private static final long FLASH_DURATION_MS = 200;
    
    private final Map<Long, Long> damagedEntities = new ConcurrentHashMap<>();
    private EventManager eventManager;
    private AudioSystem audioSystem;

    @Override
    public SystemDescriptor getDescriptor() {
        return new SystemDescriptor(
                new SystemCoordinate("com.rayvion.game", "damage-feedback", Version.parse("0.1.0")),
                Set.of(new SystemDependency(
                        new SystemTraitRequirement("com.rayvion.engine", "event", version -> version.getMajorVersion() == 0),
                        SystemDependency.RequirementLevel.REQUIRED
                ), new SystemDependency(
                        new SystemTraitRequirement("com.rayvion.engine", "audio", version -> true),
                        SystemDependency.RequirementLevel.OPTIONAL
                )),
                Set.of(new SystemTraitCoordinate("com.rayvion.game", "damage-feedback", Version.parse("0.1.0")), Tickable.TRAIT)
        );
    }

    @Override
    public void init() {
    }

    @Override
    public void onDependencyAdded(System dependency) {
        if (dependency instanceof EventManager em) {
            this.eventManager = em;
            this.eventManager.subscribe(CharacteristicChangedEvent.class, this::handleCharacteristicChanged);
        } else if (dependency instanceof AudioSystem as) {
            this.audioSystem = as;
        }
    }

    private void handleCharacteristicChanged(CharacteristicChangedEvent<?> event) {
        if ("health".equals(event.getCharacteristicId())) {
            Object oldValue = event.getOldValue();
            Object newValue = event.getNewValue();
            
            if (oldValue instanceof Double oldHealth && newValue instanceof Double newHealth) {
                if (newHealth < oldHealth) {
                    damagedEntities.put(event.getEntity().id(), java.lang.System.currentTimeMillis());
                    if (audioSystem != null) {
                        audioSystem.playSound("hit");
                    }
                }
            }
        }
    }

    @Override
    public void tick() {
        long now = java.lang.System.currentTimeMillis();
        damagedEntities.entrySet().removeIf(entry -> now - entry.getValue() > FLASH_DURATION_MS);
    }

    /**
     * Checks if an entity is currently in a "damaged" state (should flash).
     * 
     * @param entityId The ID of the entity to check.
     * @return true if the entity was damaged recently.
     */
    public boolean isDamaged(long entityId) {
        return damagedEntities.containsKey(entityId);
    }
}
