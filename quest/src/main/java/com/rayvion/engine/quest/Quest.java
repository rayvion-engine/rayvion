package com.rayvion.engine.quest;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a quest in the engine.
 * <p>
 * A quest is a collection of goals that must be completed to finish the quest.
 * It also contains {@link QuestLogic} which defines how the quest behaves and updates.
 * </p>
 */
@Getter
@RequiredArgsConstructor
public class Quest {
    /**
     * The unique identifier of the quest.
     */
    private final String id;

    /**
     * The human-readable name of the quest.
     */
    private final String name;

    /**
     * A brief description of what the quest is about.
     */
    private final String description;

    /**
     * The logic associated with this quest.
     */
    @Setter
    private QuestLogic logic;

    /**
     * The goals required to complete this quest, mapped by their IDs.
     */
    private final Map<String, QuestGoal> goals = new HashMap<>();

    /**
     * Retrieves all goals associated with this quest.
     * 
     * @return a collection of {@link QuestGoal}s
     */
    public Collection<QuestGoal> getGoals() {
        return goals.values();
    }

    /**
     * Adds a goal to this quest.
     * 
     * @param goal the {@link QuestGoal} to add
     */
    public void addGoal(QuestGoal goal) {
        goals.put(goal.getId(), goal);
    }

    /**
     * Removes a goal from this quest by its ID.
     * 
     * @param goalId the ID of the goal to remove
     */
    public void removeGoal(String goalId) {
        goals.remove(goalId);
    }

    /**
     * Retrieves a goal from this quest by its ID.
     * 
     * @param goalId the ID of the goal to retrieve
     * @return the {@link QuestGoal} or null if not found
     */
    public QuestGoal getGoal(String goalId) {
        return goals.get(goalId);
    }

    /**
     * Checks if the quest is completed.
     * <p>
     * A quest is completed if it has at least one goal and all its goals are completed.
     * </p>
     * 
     * @return true if completed, false otherwise
     */
    public boolean isCompleted() {
        if (goals.isEmpty()) {
            return false;
        }
        return goals.values().stream().allMatch(QuestGoal::isCompleted);
    }
}
