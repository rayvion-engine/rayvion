package com.rayvion.engine.commons.task.pipeline.execution.status;

import com.rayvion.engine.commons.task.descriptor.TaskDescriptor;
import lombok.Getter;

import java.time.Duration;

public record FailedTaskStatus(
        @Getter
        TaskDescriptor<?> descriptor,
        Duration duration,
        Throwable cause
) implements TaskExecutionStatus { }
