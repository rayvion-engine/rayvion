package com.rayvion.engine.core.system.manager;

import com.rayvion.engine.commons.graph.OutboundEdgePolicy;
import com.rayvion.engine.core.system.System;

public record OutboundSystemDependencyPolicy(
        System system
) implements OutboundEdgePolicy<System> {
    @Override
    public boolean isSatisfiedBy(System target) {
            return new InboundSystemDependencyPolicy(target).isSatisfiedBy(system);
    }
}
