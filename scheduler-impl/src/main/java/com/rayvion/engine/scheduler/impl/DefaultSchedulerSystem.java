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

public class DefaultSchedulerSystem implements SchedulerSystem {
    private final SystemDescriptor descriptor;
    private final ScheduledExecutorService executorService;
    private final Map<ScheduledWorkflow, ScheduledFuture<?>> activeExecutions = new ConcurrentHashMap<>();
    private final Map<ScheduledWorkflow, ScheduledWorkflow.Status> statuses = new ConcurrentHashMap<>();
    
    private boolean initialized = false;

    public DefaultSchedulerSystem() {
        this(new SystemDescriptor(
                new SystemCoordinate("rayvion", "scheduler", Version.parse("1.0.0")),
                Set.of(),
                Set.of(SchedulerSystem.TRAIT)
        ));
    }

    public DefaultSchedulerSystem(SystemDescriptor descriptor) {
        this.descriptor = descriptor;
        this.executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public SystemDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void init() {
        if (initialized) return;
        initialized = true;
    }

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

    @Override
    public ScheduledWorkflow schedule(Workflow workflow, Schedule schedule) {
        ScheduledWorkflow scheduledWorkflow = new ScheduledWorkflow(workflow, schedule);
        register(scheduledWorkflow);
        return scheduledWorkflow;
    }

    @Override
    public void unregister(ScheduledWorkflow workflow) {
        ScheduledFuture<?> future = activeExecutions.remove(workflow);
        if (future != null) {
            future.cancel(true);
            statuses.put(workflow, ScheduledWorkflow.Status.CANCELLED);
        }
    }

    @Override
    public ScheduledWorkflow.Status getStatus(ScheduledWorkflow workflow) {
        return statuses.get(workflow);
    }

    @Override
    public Collection<ScheduledWorkflow> getManagedWorkflows() {
        return Collections.unmodifiableSet(activeExecutions.keySet());
    }
}
