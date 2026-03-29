package com.rayvion.engine.commons.task;

import com.rayvion.engine.commons.task.descriptor.TaskDescriptor;

public record Task<TResult>(
        TaskDescriptor<TResult> descriptor,
        TaskRunnable<TResult> runnable
) { }
