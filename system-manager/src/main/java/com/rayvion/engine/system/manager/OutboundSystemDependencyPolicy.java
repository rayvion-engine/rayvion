package com.rayvion.engine.system.manager;

import com.rayvion.engine.commons.graph.OutboundEdgePolicy;
import com.rayvion.engine.system.System;

/**
 * Edge policy that determines whether an existing system depends on a newly registered system.
 *
 * <p>In the {@link SystemManager}'s dependency graph, an outbound edge from system&nbsp;A to
 * system&nbsp;B means that B depends on A. This policy answers the question:
 * <em>"Does the candidate system ({@code target}) depend on the new system ({@link #system})?"</em>
 *
 * <p>The check is implemented by reversing the roles: it asks whether {@link #system} would be
 * an inbound dependency of {@code target}, delegating to
 * {@link InboundSystemDependencyPolicy#isSatisfiedBy(System)} with the arguments swapped.
 *
 * @see InboundSystemDependencyPolicy
 * @see SystemManager
 */
public record OutboundSystemDependencyPolicy(
        System system
) implements OutboundEdgePolicy<System> {

    /**
     * Returns {@code true} if {@code target} (an already-registered system) declares a
     * dependency that is satisfied by {@link #system} (the newly registered system).
     *
     * @param target a candidate system that is already registered in the
     *               {@link SystemManager}; must not be {@code null}
     * @return {@code true} if an outbound edge should be created from {@link #system} to
     *         {@code target}
     */
    @Override
    public boolean isSatisfiedBy(System target) {
            return new InboundSystemDependencyPolicy(target).isSatisfiedBy(system);
    }
}
