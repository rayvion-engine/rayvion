package com.rayvion.engine.system;

import com.rayvion.engine.system.dependency.SystemDependency;
import com.rayvion.engine.system.descriptor.SystemDescriptor;

/**
 * Core contract for all engine systems.
 *
 * <p>A system is a self-contained unit of game-engine functionality (e.g. graphics, audio,
 * physics). Systems are registered with a
 * {@code SystemManager}, which resolves their inter-system
 * dependencies, calls {@link #init()} in the correct order, and notifies them when dependencies
 * are added or removed at runtime.
 *
 * <p><b>Lifecycle</b>
 * <ol>
 *   <li>The system is registered via
 *       {@code SystemManager#addSystem(System)}.</li>
 *   <li>{@link #onDependencyAdded(System)} is called for every already-registered system that
 *       satisfies a {@link SystemDependency.RequirementLevel#REQUIRED} or
 *       {@link SystemDependency.RequirementLevel#OPTIONAL} dependency declared in the
 *       descriptor.</li>
 *   <li>{@link #init()} is called once all required dependencies are resolved.</li>
 *   <li>{@link #onDependencyAdded(System)} may be called again later for optional dependencies
 *       that become available after initialisation.</li>
 *   <li>When a dependency is deregistered,
 *       {@link #onDependencyRemoved(System)} is invoked.</li>
 * </ol>
 *
 * @see "SystemManager"
 * @see com.rayvion.engine.system.descriptor.SystemDescriptor
 */
public interface System {

    /**
     * Returns the descriptor that identifies this system and declares its traits and dependencies.
     *
     * <p>The returned descriptor is used by the
     * {@code SystemManager} to match dependencies between
     * systems and to build the dependency graph.
     *
     * @return this system's {@link SystemDescriptor}; never {@code null}
     */
    SystemDescriptor getDescriptor();

    /**
     * Called when a dependency is found.
     *
     * <p>Dependencies with {@link SystemDependency.RequirementLevel#REQUIRED} will always be
     * resolved before {@link #init()} is called, while dependencies with
     * {@link SystemDependency.RequirementLevel#OPTIONAL} may be found at any time, including
     * after initialisation.
     *
     * @param dependency the dependency that was found
     */
    default void onDependencyAdded(System dependency) { }

    /**
     * Called when a previously added dependency is no longer available.
     *
     * <p>This typically happens when a system that this system depends on is removed from the
     * {@code SystemManager}. Implementations should release
     * any cached references to the removed dependency.
     *
     * @param dependency the system that was removed
     */
    default void onDependencyRemoved(System dependency) { }

    /**
     * Initialises this system.
     *
     * <p>Called by the {@code SystemManager} after all
     * {@link SystemDependency.RequirementLevel#REQUIRED} dependencies have been resolved.
     * Perform any one-time setup here (e.g. resource loading, event listener registration).
     */
    void init();

    /**
     * Releases all resources held by this system.
     *
     * <p>Called by the {@code SystemManager} when the system
     * is being removed. Override to free native resources, deregister listeners, or perform any
     * other cleanup that is the inverse of {@link #init()}.
     */
    default void dispose() { }
}
