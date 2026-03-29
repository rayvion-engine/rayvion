package com.rayvion.engine.commons.task.pipeline;

import com.rayvion.engine.commons.graph.InboundEdgePolicy;
import com.rayvion.engine.commons.task.Task;

public record InboundTaskDependencyPolicy(
        Task<?> task
) implements InboundEdgePolicy<Task<?>> {
    @Override
    public boolean isSatisfiedBy(Task<?> target) {
        return task
                .descriptor()
                .dependencies()
                .stream()
                .anyMatch(dependency -> dependency.isSatisfiedBy(target.descriptor()));
    }
}
