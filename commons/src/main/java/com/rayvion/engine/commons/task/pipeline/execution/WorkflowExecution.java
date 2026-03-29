package com.rayvion.engine.commons.task.pipeline.execution;

import com.rayvion.engine.commons.task.pipeline.execution.status.TaskExecutionStatus;

import java.util.Set;

public interface WorkflowExecution {
    Set<TaskExecutionStatus> getTasksStatuses();
}
