package com.rayvion.engine.commons.task.pipeline;

import com.rayvion.engine.commons.graph.OutboundEdgePolicy;
import com.rayvion.engine.commons.task.Task;

public record OutboundTaskDependencyPolicy(
        Task<?> task
) implements OutboundEdgePolicy<Task<?>> {
    @Override
    public boolean isSatisfiedBy(Task<?> target) {
            return new InboundTaskDependencyPolicy(target).isSatisfiedBy(task);
    }
}
