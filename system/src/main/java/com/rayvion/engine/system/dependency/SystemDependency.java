package com.rayvion.engine.system.dependency;

import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitRequirement;

public record SystemDependency(
        SystemTraitRequirement traitRequirement,
        RequirementLevel requirementLevel
) {
    /**
     * Enum representing the level of requirement for a specific dependency or traitRequirement.
     * It indicates whether the dependency is mandatory or optional.
     */
    public enum RequirementLevel {
        /**
         * Means that the system must have at least one system conforming the dependency for it to function properly.
         */
        REQUIRED,
        OPTIONAL
    }

    public boolean isSatisfiedBy(SystemDescriptor systemDescriptor) {
        return systemDescriptor.provides().stream().anyMatch(traitRequirement::isSatisfiedBy);
    }
}
