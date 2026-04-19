package com.rayvion.engine.commons.task.pipeline;

import com.rayvion.engine.commons.identity.namespace.StringHierarchyNamespaceFactory;
import com.rayvion.engine.commons.task.Task;
import com.rayvion.engine.commons.task.TaskDependency;
import com.rayvion.engine.commons.task.TaskRunnable;
import com.rayvion.engine.commons.task.descriptor.TaskDescriptor;
import com.rayvion.engine.commons.task.descriptor.TaskIdentity;
import com.rayvion.engine.commons.task.pipeline.execution.DefaultWorkflowExecutionStrategy;
import com.rayvion.engine.commons.task.pipeline.execution.WorkflowExecution;
import com.rayvion.engine.commons.task.pipeline.execution.status.*;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkflowExecutionTest {

    private TaskIdentity makeIdentity(String name) {
        return new TaskIdentity(StringHierarchyNamespaceFactory.parse(name), name);
    }

    private Task<String> makeTask(String name, Set<TaskDependency> deps, Set<TaskIdentity> provides, TaskRunnable<String> runnable) {
        TaskDescriptor<String> descriptor = new TaskDescriptor<>(makeIdentity(name), deps, provides);
        return new Task<>(descriptor, runnable);
    }

    @Test
    public void testSuccessfulPipelineExecution() {
        Workflow workflow = new Workflow();

        TaskIdentity provideA = makeIdentity("A");
        TaskIdentity provideB = makeIdentity("B");

        Task<String> taskA = makeTask("TaskA", Set.of(), Set.of(provideA), deps -> CompletableFuture.completedFuture("ResultA"));
        
        Task<String> taskB = makeTask("TaskB", 
                Set.of(new TaskDependency(provideA, TaskDependency.RequirementLevel.REQUIRED)), 
                Set.of(provideB), 
                deps -> CompletableFuture.completedFuture(deps.get(taskA.descriptor()) + "-ResultB")
        );

        workflow.addTask(taskA);
        workflow.addTask(taskB);

        DefaultWorkflowExecutionStrategy strategy = new DefaultWorkflowExecutionStrategy();
        WorkflowExecution execution = strategy.execute(workflow);

        execution.await();

        Set<TaskExecutionStatus> statuses = execution.getTasksStatuses();
        
        TaskExecutionStatus statusA = statuses.stream().filter(s -> s.getDescriptor().equals(taskA.descriptor())).findFirst().get();
        TaskExecutionStatus statusB = statuses.stream().filter(s -> s.getDescriptor().equals(taskB.descriptor())).findFirst().get();

        assertTrue(statusA instanceof CompletedTaskStatus);
        assertTrue(statusB instanceof CompletedTaskStatus);
    }
    
    @Test
    public void testFailedPipelineExecutionCancelsDependentTasks() {
        Workflow workflow = new Workflow();

        TaskIdentity provideA = makeIdentity("A");
        TaskIdentity provideC = makeIdentity("C");

        Task<String> taskA = makeTask("TaskA", Set.of(), Set.of(provideA), deps -> CompletableFuture.failedFuture(new RuntimeException("Failing Task A")));
        
        Task<String> taskC = makeTask("TaskC", 
                Set.of(new TaskDependency(provideA, TaskDependency.RequirementLevel.REQUIRED)), 
                Set.of(provideC), 
                deps -> CompletableFuture.completedFuture("Should Not Run")
        );

        workflow.addTask(taskA);
        workflow.addTask(taskC);

        DefaultWorkflowExecutionStrategy strategy = new DefaultWorkflowExecutionStrategy();
        WorkflowExecution execution = strategy.execute(workflow);

        execution.await();

        Set<TaskExecutionStatus> statuses = execution.getTasksStatuses();
        
        TaskExecutionStatus statusA = statuses.stream().filter(s -> s.getDescriptor().equals(taskA.descriptor())).findFirst().get();
        TaskExecutionStatus statusC = statuses.stream().filter(s -> s.getDescriptor().equals(taskC.descriptor())).findFirst().get();

        assertTrue(statusA instanceof FailedTaskStatus);
        assertTrue(statusC instanceof CancelledTaskStatus);
    }
}
