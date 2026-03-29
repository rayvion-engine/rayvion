package com.rayvion.engine.commons.task.descriptor;

import com.rayvion.engine.commons.task.TaskDependency;

import java.util.Set;

public record TaskDescriptor<TResult>(
        TaskIdentity identity,
        Set<TaskDependency> dependencies,
        Set<TaskIdentity> provides
) { }
