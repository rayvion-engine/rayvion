package com.rayvion.engine.commons.task.pipeline.execution;

import com.rayvion.engine.commons.task.pipeline.Workflow;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultWorkflowExecutionStrategy implements WorkflowExecutionStrategy {
    private final ExecutorService executorService;

    public DefaultWorkflowExecutionStrategy() {
        this.executorService = Executors.newCachedThreadPool();
    }

    public DefaultWorkflowExecutionStrategy(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public WorkflowExecution execute(Workflow workflow) {
        DefaultWorkflowExecution execution = new DefaultWorkflowExecution(workflow, executorService);
        execution.start();
        return execution;
    }
}
