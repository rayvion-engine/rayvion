package com.rayvion.engine.commons.task;

import com.rayvion.engine.commons.task.descriptor.TaskDescriptor;
import com.rayvion.engine.commons.task.descriptor.TaskIdentity;

public record TaskDependency(
        TaskIdentity taskRequirement,
        RequirementLevel requirementLevel
) {
    /**
     * Enum representing the level of requirement for a specific dependency or taskRequirement.
     * It indicates whether the dependency is mandatory or optional.
     */
    public enum RequirementLevel {
        /**
         * Means that the system must have at least one system conforming the dependency for it to function properly.
         */
        REQUIRED,
        OPTIONAL
    }

    public boolean isSatisfiedBy(TaskDescriptor<?> taskDescriptor) {
        return taskDescriptor.provides().stream().anyMatch(taskRequirement::equals);
    }
}
