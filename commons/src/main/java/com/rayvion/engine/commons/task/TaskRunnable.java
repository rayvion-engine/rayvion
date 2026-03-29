package com.rayvion.engine.commons.task;

import com.rayvion.engine.commons.task.descriptor.TaskDescriptor;

import java.util.Map;
import java.util.concurrent.Future;

@FunctionalInterface
public interface TaskRunnable<TResult> {
    Future<TResult> run(Map<TaskDescriptor<?>, ?> dependenciesResults);
}
