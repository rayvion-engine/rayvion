package com.rayvion.engine.commons.task.pipeline.execution.status;

import com.rayvion.engine.commons.task.descriptor.TaskDescriptor;
import lombok.Getter;

import java.util.Set;

public record CancelledTaskStatus(
        @Getter
        TaskDescriptor<?> descriptor,
        Set<TaskDescriptor<?>> cancelledBy
) implements TaskExecutionStatus { }
