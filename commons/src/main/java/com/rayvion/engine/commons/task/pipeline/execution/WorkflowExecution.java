package com.rayvion.engine.commons.task.pipeline.execution;

import com.rayvion.engine.commons.task.pipeline.execution.status.TaskExecutionStatus;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface WorkflowExecution {
    Set<TaskExecutionStatus> getTasksStatuses();

    /**
     * Blocks until the workflow has reached a terminal state (either complete or failed/cancelled).
     */
    void await();

    /**
     * Returns a future that completes when the workflow has reached a terminal state.
     */
    CompletableFuture<Void> getCompletionFuture();
}
