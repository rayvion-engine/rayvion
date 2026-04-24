package com.rayvion.engine.scheduler;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.commons.task.pipeline.Workflow;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;

import java.util.Collection;
import java.util.Set;

/**
 * A System responsible for managing and executing {@link ScheduledWorkflow}s.
 * <p>
 * The SchedulerSystem allows workflows to be executed according to various timing strategies
 * (one-time, fixed-rate, fixed-delay). It tracks the status of each managed workflow and
 * provides mechanisms to register and unregister them.
 * </p>
 */
public interface SchedulerSystem extends System {
    /**
     * The coordinate identifying the scheduler trait.
     */
    SystemTraitCoordinate TRAIT = new SystemTraitCoordinate("rayvion", "scheduler", Version.parse("1.0.0"));

    /**
     * {@inheritDoc}
     */
    @Override
    default SystemDescriptor getDescriptor() {
        return new SystemDescriptor(
                new SystemCoordinate("rayvion", "scheduler", Version.parse("1.0.0")),
                Set.of(),
                Set.of(TRAIT)
        );
    }

    /**
     * Registers a scheduled workflow to be overseen by the system.
     * <p>
     * Once registered, the system begins managing the workflow's execution according to its
     * underlying {@link Schedule} configuration.
     * </p>
     *
     * @param workflow the scheduled workflow configuration to register
     */
    void register(ScheduledWorkflow workflow);

    /**
     * Convenience method to create and register a scheduled workflow.
     *
     * @param workflow the workflow pipeline to execute
     * @param schedule the timing configuration
     * @return the created and registered {@link ScheduledWorkflow} instance
     */
    ScheduledWorkflow schedule(Workflow workflow, Schedule schedule);

    /**
     * Unregisters a scheduled workflow and cancels any pending or running execution.
     * <p>
     * If the workflow is currently running, it may be interrupted depending on the implementation.
     * </p>
     *
     * @param workflow the scheduled workflow to remove from management
     */
    void unregister(ScheduledWorkflow workflow);

    /**
     * Retrieves the current execution status of a managed workflow.
     *
     * @param workflow the workflow to query
     * @return the current {@link ScheduledWorkflow.Status}, or {@code null} if the workflow is not managed by this system
     */
    ScheduledWorkflow.Status getStatus(ScheduledWorkflow workflow);

    /**
     * Retrieves all workflows currently managed by the scheduler system.
     *
     * @return an unmodifiable collection of registered scheduled workflows
     */
    Collection<ScheduledWorkflow> getManagedWorkflows();
}
