package com.rayvion.engine.quest;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;

import java.util.Collection;
import java.util.Set;

/**
 * The Quest System manages the lifecycle and progression of quests within the engine.
 * <p>
 * It provides functionality for registering and unregistering quests, tracking active quests,
 * and updating the progress of individual quest goals.
 * </p>
 */
public interface QuestSystem extends System {
    /**
     * The coordinate defining this system's identity and version.
     */
    SystemTraitCoordinate TRAIT = new SystemTraitCoordinate("com.rayvion.engine", "quest", Version.parse("0.1.0"));

    @Override
    default SystemDescriptor getDescriptor() {
        return new SystemDescriptor(
                new com.rayvion.engine.system.descriptor.SystemCoordinate("com.rayvion.engine", "quest", Version.parse("0.1.0")),
                Set.of(),
                Set.of(TRAIT)
        );
    }

    /**
     * Registers a quest with the system.
     * <p>
     * Once registered, the quest's logic (if any) will be initialized.
     * </p>
     * 
     * @param quest the {@link Quest} to register
     */
    void registerQuest(Quest quest);

    /**
     * Unregisters a quest from the system.
     * 
     * @param questId the ID of the quest to unregister
     */
    void unregisterQuest(String questId);

    /**
     * Retrieves a registered quest by its ID.
     * 
     * @param questId the ID of the quest to retrieve
     * @return the {@link Quest} or null if not found
     */
    Quest getQuest(String questId);

    /**
     * Retrieves all currently active (registered) quests.
     * 
     * @return a collection of all registered {@link Quest}s
     */
    Collection<Quest> getActiveQuests();

    /**
     * Updates the progress of a specific goal in a specific quest.
     * 
     * @param questId The ID of the quest.
     * @param goalId The ID of the goal.
     * @param progress The new progress (0.0 to 1.0).
     */
    void updateProgress(String questId, String goalId, double progress);
}
