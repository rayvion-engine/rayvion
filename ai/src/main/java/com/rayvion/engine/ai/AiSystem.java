package com.rayvion.engine.ai;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;

import java.util.Set;

/**
 * System responsible for managing and executing AI strategies for entities.
 * <p>
 * The AI system maintains a mapping between entities and their current {@link AiStrategy}.
 * It is typically responsible for triggering the update logic of these strategies during the game tick.
 * </p>
 */
public interface AiSystem extends System {
    /**
     * {@inheritDoc}
     * <p>
     * Returns a descriptor for the AI system, including its coordinate and traits.
     * </p>
     */
    @Override
    default SystemDescriptor getDescriptor() {
        return new SystemDescriptor(
                new SystemCoordinate("com.rayvion.engine", "ai", Version.parse("0.1.0")),
                Set.of(),
                Set.of(new SystemTraitCoordinate("com.rayvion.engine", "ai", Version.parse("0.1.0")))
        );
    }

    /**
     * Assigns an AI strategy to the specified entity.
     * <p>
     * If the entity already has a strategy, it will be replaced by the new one.
     * </p>
     *
     * @param entityId the unique identifier of the entity
     * @param strategy the {@link AiStrategy} to assign to the entity
     */
    void setStrategy(long entityId, AiStrategy strategy);

    /**
     * Retrieves the current AI strategy assigned to the specified entity.
     *
     * @param entityId the unique identifier of the entity
     * @return the assigned {@link AiStrategy}, or {@code null} if no strategy is assigned to the entity
     */
    AiStrategy getStrategy(long entityId);

    /**
     * Checks whether the specified entity has an assigned AI strategy.
     *
     * @param entityId the unique identifier of the entity
     * @return {@code true} if the entity has a strategy, {@code false} otherwise
     */
    boolean hasStrategy(long entityId);

    /**
     * Removes the AI strategy assigned to the specified entity.
     *
     * @param entityId the unique identifier of the entity
     * @return {@code true} if a strategy was successfully removed, {@code false} if the entity had no strategy
     */
    boolean removeStrategy(long entityId);
}
