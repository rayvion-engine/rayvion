package com.rayvion.engine.core.system.manager;

import com.rayvion.engine.commons.graph.InboundEdgePolicy;
import com.rayvion.engine.core.system.System;

public record InboundSystemDependencyPolicy(
        System system
) implements InboundEdgePolicy<System> {
    @Override
    public boolean isSatisfiedBy(System target) {
        return system
                .getDescriptor()
                .dependencies()
                .stream()
                .anyMatch(dependency -> dependency.isSatisfiedBy(target.getDescriptor()));
    }
}
