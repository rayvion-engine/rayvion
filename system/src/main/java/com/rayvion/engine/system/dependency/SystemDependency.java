package com.rayvion.engine.system.dependency;

import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitRequirement;

/**
 * Represents a single dependency that a system declares on a trait provided by another system.
 *
 * <p>A {@code SystemDependency} pairs a {@link SystemTraitRequirement} (describing <em>which</em>
 * trait is needed and at what version) with a {@link RequirementLevel} (describing <em>how</em>
 * critical the dependency is). The
 * {@code SystemManager} uses this information when wiring
 * systems together:
 * <ul>
 *   <li>{@link RequirementLevel#REQUIRED} — all required dependencies must be present before
 *       {@link com.rayvion.engine.system.System#init()} is called.</li>
 *   <li>{@link RequirementLevel#OPTIONAL} — optional dependencies are wired up whenever a
 *       matching system is registered, even after initialisation.</li>
 * </ul>
 *
 * @param traitRequirement the trait that must be provided by a candidate dependency system
 * @param requirementLevel whether this dependency is mandatory or optional
 * @see SystemTraitRequirement
 * @see com.rayvion.engine.system.descriptor.SystemDescriptor
 */
public record SystemDependency(
        SystemTraitRequirement traitRequirement,
        RequirementLevel requirementLevel
) {
    /**
     * Indicates whether a dependency must be satisfied before a system can initialise.
     */
    public enum RequirementLevel {
        /**
         * The system must have at least one registered system that satisfies this dependency
         * before {@link com.rayvion.engine.system.System#init()} is called.
         */
        REQUIRED,

        /**
         * The dependency is desirable but not mandatory. The system can initialise without it,
         * and will be notified via
         * {@link com.rayvion.engine.system.System#onDependencyAdded(com.rayvion.engine.system.System)}
         * whenever a matching system is registered.
         */
        OPTIONAL
    }

    /**
     * Returns {@code true} if the given system descriptor satisfies this dependency.
     *
     * <p>Satisfaction is determined by checking whether any of the traits declared in
     * {@code systemDescriptor.provides()} passes the {@link #traitRequirement()}.
     *
     * @param systemDescriptor the descriptor of the candidate dependency system; must not be
     *                         {@code null}
     * @return {@code true} if at least one provided trait satisfies the requirement
     */
    public boolean isSatisfiedBy(SystemDescriptor systemDescriptor) {
        return systemDescriptor.provides().stream().anyMatch(traitRequirement::isSatisfiedBy);
    }
}
