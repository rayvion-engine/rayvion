package com.rayvion.engine.core.system.dependency;

import com.rayvion.engine.core.system.trait.SystemTraitRequirement;

public record SystemDependency(
        SystemTraitRequirement trait,
        RequirementLevel requirementLevel
) {
    /**
     * Enum representing the level of requirement for a specific dependency or trait.
     * It indicates whether the dependency is mandatory or optional.
     */
    public enum RequirementLevel {
        /**
         * Means that the system must have at least one system conforming the dependency for it to function properly.
         */
        REQUIRED,
        OPTIONAL
    }
}
