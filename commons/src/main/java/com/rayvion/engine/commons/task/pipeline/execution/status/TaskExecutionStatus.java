package com.rayvion.engine.commons.task.pipeline.execution.status;

import com.rayvion.engine.commons.task.descriptor.TaskDescriptor;

public interface TaskExecutionStatus {
    TaskDescriptor<?> getDescriptor();
}