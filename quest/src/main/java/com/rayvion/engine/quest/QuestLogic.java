package com.rayvion.engine.quest;

/**
 * Interface for custom quest logic.
 * <p>
 * This allows each quest to determine its own logic for updating progress,
 * handling events, and managing lifecycle.
 * </p>
 */
public interface QuestLogic {
    /**
     * Initializes the logic for the given quest.
     * 
     * @param quest The quest instance.
     * @param system The quest system for updating progress.
     */
    void initialize(Quest quest, QuestSystem system);
}
