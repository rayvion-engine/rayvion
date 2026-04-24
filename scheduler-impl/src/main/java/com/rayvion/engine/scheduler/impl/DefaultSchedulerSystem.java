package com.rayvion.engine.scheduler.impl;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.commons.task.pipeline.Workflow;
import com.rayvion.engine.commons.task.pipeline.execution.DefaultWorkflowExecution;
import com.rayvion.engine.scheduler.Schedule;
import com.rayvion.engine.scheduler.ScheduledWorkflow;
import com.rayvion.engine.scheduler.SchedulerSystem;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of the {@link SchedulerSystem}.
 * <p>
 * This implementation uses a {@link ScheduledExecutorService} to manage the timing and execution
 * of workflows. It maintains internal maps to track the status and active execution handles
 * of all registered workflows.
 * </p>
 */
public class DefaultSchedulerSystem implements SchedulerSystem {
    private final SystemDescriptor descriptor;
    private final ScheduledExecutorService executorService;
    private final Map<ScheduledWorkflow, ScheduledFuture<?>> activeExecutions = new ConcurrentHashMap<>();
    private final Map<ScheduledWorkflow, ScheduledWorkflow.Status> statuses = new ConcurrentHashMap<>();
    
    private boolean initialized = false;

    /**
     * Creates a new {@code DefaultSchedulerSystem} with a default descriptor.
     */
    public DefaultSchedulerSystem() {
        this(new SystemDescriptor(
                new SystemCoordinate("rayvion", "scheduler", Version.parse("1.0.0")),
                Set.of(),
                Set.of(SchedulerSystem.TRAIT)
        ));
    }

    /**
     * Creates a new {@code DefaultSchedulerSystem} with the specified descriptor.
     *
     * @param descriptor the system descriptor to use
     */
    public DefaultSchedulerSystem(SystemDescriptor descriptor) {
        this.descriptor = descriptor;
        this.executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SystemDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        if (initialized) return;
        initialized = true;
    }

    /**
     * Executes the given scheduled workflow and updates its status.
     *
     * @param scheduledWorkflow the workflow to run
     */
    private void runWorkflow(ScheduledWorkflow scheduledWorkflow) {
        try {
            statuses.put(scheduledWorkflow, ScheduledWorkflow.Status.RUNNING);
            DefaultWorkflowExecution execution = new DefaultWorkflowExecution(scheduledWorkflow.workflow(), executorService);
            execution.start();
            execution.await();
            
            if (scheduledWorkflow.schedule() instanceof Schedule.OneTime) {
                statuses.put(scheduledWorkflow, ScheduledWorkflow.Status.COMPLETED);
                activeExecutions.remove(scheduledWorkflow);
            } else {
                statuses.put(scheduledWorkflow, ScheduledWorkflow.Status.PENDING);
            }
        } catch (Exception e) {
            statuses.put(scheduledWorkflow, ScheduledWorkflow.Status.FAILED);
            unregister(scheduledWorkflow); 
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(ScheduledWorkflow workflow) {
        if (activeExecutions.containsKey(workflow)) {
            return;
        }
        
        statuses.put(workflow, ScheduledWorkflow.Status.PENDING);
        Schedule schedule = workflow.schedule();
        
        ScheduledFuture<?> future = switch (schedule) {
            case Schedule.OneTime(java.time.Duration delay) ->
                    executorService.schedule(() -> runWorkflow(workflow), delay.toMillis(), TimeUnit.MILLISECONDS);
            case Schedule.FixedRate(java.time.Duration initialDelay, java.time.Duration period) ->
                    executorService.scheduleAtFixedRate(() -> runWorkflow(workflow), initialDelay.toMillis(), period.toMillis(), TimeUnit.MILLISECONDS);
            case Schedule.FixedDelay(java.time.Duration initialDelay, java.time.Duration delay) ->
                    executorService.scheduleWithFixedDelay(() -> runWorkflow(workflow), initialDelay.toMillis(), delay.toMillis(), TimeUnit.MILLISECONDS);
        };

        activeExecutions.put(workflow, future);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledWorkflow schedule(Workflow workflow, Schedule schedule) {
        ScheduledWorkflow scheduledWorkflow = new ScheduledWorkflow(workflow, schedule);
        register(scheduledWorkflow);
        return scheduledWorkflow;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregister(ScheduledWorkflow workflow) {
        ScheduledFuture<?> future = activeExecutions.remove(workflow);
        if (future != null) {
            future.cancel(true);
            statuses.put(workflow, ScheduledWorkflow.Status.CANCELLED);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ScheduledWorkflow.Status getStatus(ScheduledWorkflow workflow) {
        return statuses.get(workflow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ScheduledWorkflow> getManagedWorkflows() {
        return Collections.unmodifiableSet(activeExecutions.keySet());
    }
}
