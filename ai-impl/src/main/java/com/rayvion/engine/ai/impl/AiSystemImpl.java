package com.rayvion.engine.ai.impl;

import com.rayvion.engine.ai.AiStrategy;
import com.rayvion.engine.ai.AiSystem;
import com.rayvion.engine.system.Tickable;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;
import com.github.zafarkhaja.semver.Version;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of the {@link AiSystem} that integrates with the engine's tick system.
 * <p>
 * This implementation uses a {@link Map} to store the AI strategies for each entity.
 * During each {@link #tick()}, it iterates through all registered strategies and calls their
 * {@link AiStrategy#update(long)} method.
 * </p>
 * <p>
 * Note: The tick iteration is performed over a snapshot of the strategies to prevent
 * {@link java.util.ConcurrentModificationException} if a strategy modifies the system during its update.
 * </p>
 */
public class AiSystemImpl implements AiSystem, Tickable {
    /**
     * Map storing entity IDs and their associated AI strategies.
     */
    private final Map<Long, AiStrategy> strategies = new HashMap<>();

    @Override
    public SystemDescriptor getDescriptor() {
        return new SystemDescriptor(
                new SystemCoordinate("com.rayvion.engine", "ai", Version.parse("0.1.0")),
                Set.of(),
                Set.of(Tickable.TRAIT)
        );
    }

    @Override
    public void init() {
        // No initialization needed for now
    }

    @Override
    public void setStrategy(long entityId, AiStrategy strategy) {
        strategies.put(entityId, strategy);
    }

    @Override
    public AiStrategy getStrategy(long entityId) {
        return strategies.get(entityId);
    }

    @Override
    public boolean hasStrategy(long entityId) {
        return strategies.containsKey(entityId);
    }

    @Override
    public boolean removeStrategy(long entityId) {
        return strategies.remove(entityId) != null;
    }

    /**
     * Executes the AI logic for all entities with assigned strategies.
     * <p>
     * This method iterates over a copy of the current strategy mappings to ensure
     * safe execution even if strategies are added or removed during the tick.
     * </p>
     */
    @Override
    public void tick() {
        // Iterate over a copy of keys to avoid exception if a strategy removes itself/others
        for (Long entityId : new HashMap<>(strategies).keySet()) {
            AiStrategy strategy = strategies.get(entityId);
            if (strategy != null) {
                strategy.update(entityId);
            }
        }
    }
}
