package com.rayvion.game.combat;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.system.Tickable;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * System that manages healing effects over time.
 */
@Slf4j
public class HealingSystem implements Tickable {
    private final SystemDescriptor descriptor;
    private CharacteristicSystem characteristicSystem;
    private final List<HealingEffect> activeEffects = new ArrayList<>();

    public HealingSystem() {
        this.descriptor = new SystemDescriptor(
                new SystemCoordinate("com.rayvion.game", "healing-system", Version.parse("1.0.0")),
                Set.of(),
                Set.of(Tickable.TRAIT)
        );
    }

    @Override
    public SystemDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void onDependencyAdded(com.rayvion.engine.system.System dependency) {
        if (dependency instanceof CharacteristicSystem cs) {
            this.characteristicSystem = cs;
        }
    }

    @Override
    public void init() {
        log.info("HealingSystem initialized");
    }

    /**
     * Adds a healing effect to an entity.
     * @param entity The entity to heal.
     * @param totalHealing Total amount of health to restore.
     * @param durationSeconds Duration over which to restore the health.
     */
    public void addEffect(Entity entity, double totalHealing, double durationSeconds) {
        double tickDelaySeconds = getTickDelay().toMillis() / 1000.0;
        int totalTicks = (int) (durationSeconds / tickDelaySeconds);
        if (totalTicks <= 0) totalTicks = 1;
        double healingPerTick = totalHealing / totalTicks;

        activeEffects.add(new HealingEffect(entity, healingPerTick, totalTicks));
        log.info("Added healing effect to entity {}: {} total healing over {} seconds ({} per tick)",
                entity.id(), totalHealing, durationSeconds, healingPerTick);
    }

    @Override
    public void tick() {
        if (characteristicSystem == null) return;

        Iterator<HealingEffect> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            HealingEffect effect = iterator.next();
            applyEffect(effect);
            effect.remainingTicks--;
            if (effect.remainingTicks <= 0) {
                iterator.remove();
                log.debug("Healing effect completed for entity {}", effect.entity.id());
            }
        }
    }

    private void applyEffect(HealingEffect effect) {
        double currentHealth = characteristicSystem.getValue(effect.entity, "health");
        double maxHealth = characteristicSystem.getValue(effect.entity, "max_health");

        if (currentHealth >= maxHealth) return;

        double newHealth = Math.min(maxHealth, currentHealth + effect.healingPerTick);
        characteristicSystem.setValue(effect.entity, "health", newHealth);
        log.trace("Healing entity {}: {} -> {}", effect.entity.id(), currentHealth, newHealth);
    }

    @Override
    public Duration getTickDelay() {
        return Duration.ofMillis(50); // 20 TPS
    }

    private static class HealingEffect {
        final Entity entity;
        final double healingPerTick;
        int remainingTicks;

        HealingEffect(Entity entity, double healingPerTick, int totalTicks) {
            this.entity = entity;
            this.healingPerTick = healingPerTick;
            this.remainingTicks = totalTicks;
        }
    }
}
