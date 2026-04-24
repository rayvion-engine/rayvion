package com.rayvion.engine.quest.impl;

import com.rayvion.engine.quest.Quest;
import com.rayvion.engine.quest.QuestGoal;
import com.rayvion.engine.quest.QuestSystem;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of the {@link QuestSystem} interface.
 * <p>
 * This implementation uses a {@link ConcurrentHashMap} to store and manage quests,
 * ensuring thread-safety for quest registration and progress updates.
 * </p>
 */
public class QuestSystemImpl implements QuestSystem {
    /**
     * Map of registered quests, keyed by their unique identifiers.
     */
    private final Map<String, Quest> quests = new ConcurrentHashMap<>();

    @Override
    public void init() {
    }

    @Override
    public void registerQuest(Quest quest) {
        quests.put(quest.getId(), quest);
        if (quest.getLogic() != null) {
            quest.getLogic().initialize(quest, this);
        }
    }

    @Override
    public void unregisterQuest(String questId) {
        quests.remove(questId);
    }

    @Override
    public Quest getQuest(String questId) {
        return quests.get(questId);
    }

    @Override
    public Collection<Quest> getActiveQuests() {
        return Collections.unmodifiableCollection(quests.values());
    }

    @Override
    public void updateProgress(String questId, String goalId, double progress) {
        Quest quest = quests.get(questId);
        if (quest != null) {
            QuestGoal goal = quest.getGoal(goalId);
            if (goal != null) {
                goal.setProgress(progress);
            }
        }
    }
}
