package com.rayvion.engine.scheduler;

import com.rayvion.engine.commons.task.pipeline.Workflow;

/**
 * Represents a configuration for a scheduled {@link Workflow}.
 * <p>
 * This record pairs a {@link Workflow} with its intended {@link Schedule}, allowing the {@link SchedulerSystem}
 * to manage its execution lifecycle.
 * </p>
 *
 * @param workflow the workflow pipeline to be executed
 * @param schedule the timing configuration for the execution
 */
public record ScheduledWorkflow(Workflow workflow, Schedule schedule) {

    /**
     * Represents the current execution status of a {@link ScheduledWorkflow}.
     */
    public enum Status {
        /** The workflow is registered and waiting for its next scheduled execution. */
        PENDING,
        /** The workflow is currently being executed. */
        RUNNING,
        /** The scheduled execution has been manually cancelled. */
        CANCELLED,
        /** The workflow has completed its scheduled run (applicable for {@link Schedule.OneTime}). */
        COMPLETED,
        /** The workflow execution failed due to an error. */
        FAILED
    }
}
