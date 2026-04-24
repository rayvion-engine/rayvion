package com.rayvion.engine.system.manager;

import com.rayvion.engine.commons.graph.InboundEdgePolicy;
import com.rayvion.engine.system.System;

/**
 * Edge policy that determines whether an existing system should be connected as an inbound
 * dependency of a newly registered system.
 *
 * <p>In the {@link SystemManager}'s dependency graph, an edge from system&nbsp;A to system&nbsp;B
 * means that B depends on A (A is an inbound dependency of B). This policy answers the question:
 * <em>"Does the new system ({@link #system}) depend on the candidate system ({@code target})?"</em>
 *
 * <p>The check is performed by inspecting whether any dependency declared in the new system's
 * descriptor is satisfied by the candidate system's descriptor, using
 * {@link com.rayvion.engine.system.dependency.SystemDependency#isSatisfiedBy(com.rayvion.engine.system.descriptor.SystemDescriptor)}.
 *
 * @see OutboundSystemDependencyPolicy
 * @see SystemManager
 */
public record InboundSystemDependencyPolicy(
        System system
) implements InboundEdgePolicy<System> {

    /**
     * Returns {@code true} if {@code target} satisfies at least one dependency declared by
     * {@link #system}.
     *
     * @param target a candidate system that is already registered in the
     *               {@link SystemManager}; must not be {@code null}
     * @return {@code true} if an inbound edge should be created from {@code target} to
     *         {@link #system}
     */
    @Override
    public boolean isSatisfiedBy(System target) {
        return system
                .getDescriptor()
                .dependencies()
                .stream()
                .anyMatch(dependency -> dependency.isSatisfiedBy(target.getDescriptor()));
    }
}
