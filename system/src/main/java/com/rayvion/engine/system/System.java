package com.rayvion.engine.system;

import com.rayvion.engine.system.dependency.SystemDependency;
import com.rayvion.engine.system.descriptor.SystemDescriptor;

public interface System {
    SystemDescriptor getDescriptor();

    /**
     * Called when a dependency is found.
     * Dependencies with {@link SystemDependency.Type#REQUIRED} will always be found before {@code init()} is called, while dependencies with {@link SystemDependency.Type#OPTIONAL} may be found at any time.
     *
     * @param dependency The dependency that was found.
     */
    default void onDependencyAdded(System dependency) { }

    default void onDependencyRemoved(System dependency) { }

    void init();
}
