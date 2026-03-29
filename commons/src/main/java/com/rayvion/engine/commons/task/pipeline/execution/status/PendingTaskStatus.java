package com.rayvion.engine.commons.task.pipeline.execution.status;

import com.rayvion.engine.commons.task.descriptor.TaskDescriptor;
import lombok.Getter;

public record PendingTaskStatus(
        @Getter
        TaskDescriptor<?> descriptor
) implements TaskExecutionStatus { }
