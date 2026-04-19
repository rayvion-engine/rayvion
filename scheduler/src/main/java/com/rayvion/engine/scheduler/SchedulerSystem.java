package com.rayvion.engine.scheduler;

import com.rayvion.engine.commons.task.pipeline.Workflow;
import com.rayvion.engine.system.System;

import java.util.Collection;

/**
 * A System that manages scheduled workflows.
 */
public interface SchedulerSystem extends System {

    /**
     * Registers a scheduled workflow to be overseen by the system.
     * The system begins execution according to the underlying Schedule configuration.
     *
     * @param workflow the scheduled workflow configuration to register
     */
    void register(ScheduledWorkflow workflow);

    /**
     * Convenience method to create and register a scheduled workflow.
     *
     * @param workflow the workflow pipeline to execute
     * @param schedule the timing configuration
     * @return the created ScheduledWorkflow instance
     */
    ScheduledWorkflow schedule(Workflow workflow, Schedule schedule);

    /**
     * Unregisters a scheduled workflow and cancels any pending/running execution.
     *
     * @param workflow the scheduled workflow to remove
     */
    void unregister(ScheduledWorkflow workflow);

    /**
     * Retrieves the current execution status of a managed workflow.
     *
     * @param workflow the workflow to query
     * @return the current status, or null if the workflow is not managed by this system
     */
    ScheduledWorkflow.Status getStatus(ScheduledWorkflow workflow);

    /**
     * Retrieves all workflows currently managed by the scheduler system.
     *
     * @return a collection of registered scheduled workflows
     */
    Collection<ScheduledWorkflow> getManagedWorkflows();
}
