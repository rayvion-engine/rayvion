package com.rayvion.engine.commons.task.pipeline.execution;

import com.rayvion.engine.commons.task.pipeline.Workflow;

public interface WorkflowExecutionStrategy {
    WorkflowExecution execute(Workflow workflow);
}
