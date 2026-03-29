package com.rayvion.engine.commons.task.pipeline.plan;

import com.rayvion.engine.commons.task.TaskDependency;
import com.rayvion.engine.commons.task.TaskRunnable;

import java.util.Set;

public record ExecutionPlanNode<TResult>(
        Set<TaskDependency> dependencies,
        TaskRunnable<TResult> runnable
) { }