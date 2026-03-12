package com.rayvion.engine.system.manager;

import com.rayvion.engine.commons.graph.OutboundEdgePolicy;
import com.rayvion.engine.system.System;

public record OutboundSystemDependencyPolicy(
        System system
) implements OutboundEdgePolicy<System> {
    @Override
    public boolean isSatisfiedBy(System target) {
            return new InboundSystemDependencyPolicy(target).isSatisfiedBy(system);
    }
}
