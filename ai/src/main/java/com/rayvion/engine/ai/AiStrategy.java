package com.rayvion.engine.ai;

/**
 * Strategy interface for defining AI behaviors for entities.
 * <p>
 * Implementations of this interface define how an entity should behave during each game tick.
 * Strategies can be swapped dynamically at runtime to change entity behavior.
 * </p>
 */
public interface AiStrategy {
    /**
     * Updates the AI logic for the given entity.
     * <p>
     * This method is called periodically by the {@link AiSystem} to allow the entity
     * to perform its AI-driven actions (e.g., movement, combat decisions, state transitions).
     * </p>
     *
     * @param entityId the unique identifier of the entity to update
     */
    void update(long entityId);
}
