package com.rayvion.engine.commons.task.pipeline.execution;

import com.rayvion.engine.commons.task.Task;
import com.rayvion.engine.commons.task.TaskDependency;
import com.rayvion.engine.commons.task.descriptor.TaskDescriptor;
import com.rayvion.engine.commons.task.pipeline.Workflow;
import com.rayvion.engine.commons.task.pipeline.execution.status.*;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultWorkflowExecution implements WorkflowExecution {
    private final Workflow workflow;
    private final ExecutorService executorService;

    private final Map<TaskDescriptor<?>, TaskExecutionStatus> statuses = new ConcurrentHashMap<>();

    private final Map<TaskDescriptor<?>, Object> results = Collections.synchronizedMap(new HashMap<>());
    
    private final CompletableFuture<Void> completionFuture = new CompletableFuture<>();
    
    private final AtomicInteger pendingTasksCount = new AtomicInteger();

    public DefaultWorkflowExecution(Workflow workflow, ExecutorService executorService) {
        this.workflow = workflow;
        this.executorService = executorService;
        
        // Initialize all valid tasks as Pending
        for (Task<?> task : workflow.getGraph().vertexSet()) {
            if (task == Workflow.ROOT_TASK) continue;
            statuses.put(task.descriptor(), new PendingTaskStatus(task.descriptor()));
            pendingTasksCount.incrementAndGet();
        }
    }

    public void start() {
        if (pendingTasksCount.get() == 0) {
            completionFuture.complete(null);
            return;
        }

        // Trigger starting from the root task.
        // Tasks directly connected to ROOT_TASK have no other dependencies (or initial ones).
        List<Task<?>> initialTasks = workflow.getGraph().getOutboundVertices(Workflow.ROOT_TASK);
        for (Task<?> task : initialTasks) {
            evaluateAndSchedule(task);
        }
    }

    @Override
    public Set<TaskExecutionStatus> getTasksStatuses() {
        return new HashSet<>(statuses.values());
    }

    @Override
    public void await() {
        completionFuture.join();
    }

    @Override
    public CompletableFuture<Void> getCompletionFuture() {
        return completionFuture;
    }

    private synchronized void evaluateAndSchedule(Task<?> task) {
        if (task == Workflow.ROOT_TASK) return;

        TaskDescriptor<?> descriptor = task.descriptor();
        TaskExecutionStatus status = statuses.get(descriptor);

        // Only evaluate if pending
        if (!(status instanceof PendingTaskStatus)) {
            return;
        }

        boolean allRequiredMet = true;
        boolean anyPending = false;
        Set<TaskDescriptor<?>> failedRequired = new HashSet<>();

        List<Task<?>> inboundTasks = workflow.getGraph().getInboundVertices(task);

        for (TaskDependency dependency : descriptor.dependencies()) {
            List<Task<?>> satisfiers = inboundTasks.stream()
                    .filter(t -> dependency.isSatisfiedBy(t.descriptor()))
                    .toList();

            if (satisfiers.isEmpty()) {
                if (dependency.requirementLevel() == TaskDependency.RequirementLevel.REQUIRED) {
                    allRequiredMet = false;
                    failedRequired.add(null); 
                }
                continue;
            }

            boolean satisfied = false;
            boolean pendingOrRunning = false;

            for (Task<?> satisfier : satisfiers) {
                if (satisfier == Workflow.ROOT_TASK) {
                    satisfied = true;
                    break;
                }

                TaskExecutionStatus satisfierStatus = statuses.get(satisfier.descriptor());
                if (satisfierStatus instanceof CompletedTaskStatus) {
                    satisfied = true;
                } else if (satisfierStatus instanceof PendingTaskStatus || satisfierStatus instanceof InProgressTaskStatus) {
                    pendingOrRunning = true;
                } else if (satisfierStatus instanceof FailedTaskStatus || satisfierStatus instanceof CancelledTaskStatus || satisfierStatus instanceof BlockedTaskStatus) {
                    failedRequired.add(satisfier.descriptor());
                }
            }

            if (!satisfied) {
                if (dependency.requirementLevel() == TaskDependency.RequirementLevel.REQUIRED) {
                    allRequiredMet = false;
                    if (pendingOrRunning) {
                        anyPending = true;
                    }
                } else {
                    // Optional dependency
                    if (pendingOrRunning) {
                        anyPending = true;
                    }
                }
            }
        }

        if (!allRequiredMet && !failedRequired.isEmpty()) {
            // Cancel because required dependencies failed
            failedRequired.remove(null); // Cleanup in case we added null for completely missing deps
            cancelTask(task, failedRequired);
            return;
        }

        if (anyPending) {
            return;
        }

        if (allRequiredMet) {
            schedule(task);
        }
    }

    private void schedule(Task<?> task) {
        TaskDescriptor<?> descriptor = task.descriptor();
        statuses.put(descriptor, new InProgressTaskStatus(descriptor, Duration.ZERO));

        executorService.submit(() -> {
            Instant start = Instant.now();
            try {
                Map<TaskDescriptor<?>, Object> currentResults = new HashMap<>();
                for (Task<?> inbound : workflow.getGraph().getInboundVertices(task)) {
                    if (inbound != Workflow.ROOT_TASK) {
                        // synchronized read
                        Object res = results.get(inbound.descriptor());
                        if (res != null || results.containsKey(inbound.descriptor())) {
                            currentResults.put(inbound.descriptor(), res);
                        }
                    }
                }

                Future<?> future = task.runnable().run(currentResults);
                Object result = future.get(); // Await task completion

                Duration duration = Duration.between(start, Instant.now());
                results.put(descriptor, result);
                statuses.put(descriptor, new CompletedTaskStatus(descriptor, duration));

                onTaskEnd(task);

            } catch (Throwable t) {
                Duration duration = Duration.between(start, Instant.now());
                statuses.put(descriptor, new FailedTaskStatus(descriptor, duration, t));
                onTaskEnd(task);
            }
        });
    }

    private void cancelTask(Task<?> task, Set<TaskDescriptor<?>> failedDependencies) {
        statuses.put(task.descriptor(), new CancelledTaskStatus(task.descriptor(), failedDependencies));
        onTaskEnd(task);
    }

    private void onTaskEnd(Task<?> task) {
        int remaining = pendingTasksCount.decrementAndGet();
        if (remaining == 0) {
            completionFuture.complete(null);
            return;
        }

        // Trigger downstream
        List<Task<?>> outboundTasks = workflow.getGraph().getOutboundVertices(task);
        for (Task<?> outbound : outboundTasks) {
            evaluateAndSchedule(outbound);
        }
    }
}
