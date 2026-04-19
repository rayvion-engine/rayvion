package com.rayvion.engine.scheduler;

import com.rayvion.engine.commons.task.pipeline.Workflow;

/**
 * Represents a configuration for a scheduled {@link Workflow}.
 */
public record ScheduledWorkflow(Workflow workflow, Schedule schedule) {
    
    public enum Status {
        PENDING,
        RUNNING,
        CANCELLED,
        COMPLETED,
        FAILED
    }
}
