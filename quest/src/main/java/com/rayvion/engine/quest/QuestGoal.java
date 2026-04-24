package com.rayvion.engine.quest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a single goal or objective within a {@link Quest}.
 * <p>
 * A goal tracks progress towards completion. A goal is considered completed
 * when its progress reaches 1.0 (100%).
 * </p>
 */
@Getter
@AllArgsConstructor
public class QuestGoal {
    /**
     * The unique identifier of the goal.
     */
    private final String id;

    /**
     * A description of what needs to be done to achieve this goal.
     */
    private final String description;

    /**
     * The current progress of the goal (0.0 to 1.0).
     */
    @Setter
    private double progress;

    /**
     * Checks if this goal is completed.
     * 
     * @return true if progress is 1.0 or greater, false otherwise
     */
    public boolean isCompleted() {
        return progress >= 1.0;
    }
}
