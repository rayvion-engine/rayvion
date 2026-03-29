package com.rayvion.engine.commons.task.pipeline.execution.status;

import com.rayvion.engine.commons.task.descriptor.TaskDescriptor;
import lombok.Getter;

import java.util.Set;

public record BlockedTaskStatus(
        @Getter
        TaskDescriptor<?> descriptor,
        Set<TaskDescriptor<?>> blockedBy
) implements TaskExecutionStatus { }
