package com.rayvion.engine.system.descriptor;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.dependency.SystemDependency;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;

import java.util.Set;

/**
 * Describes a system's identity, the traits it provides, and the dependencies it requires.
 *
 * <p>A {@code SystemDescriptor} is the contract that the
 * {@code SystemManager} inspects to build the dependency
 * graph. It answers three questions about a system:
 * <ul>
 *   <li><b>Who am I?</b> — encoded in the {@link #coordinate()}</li>
 *   <li><b>What do I need?</b> — encoded in the {@link #dependencies()} set</li>
 *   <li><b>What do I offer?</b> — encoded in the {@link #provides()} set</li>
 * </ul>
 *
 * <p>A minimal descriptor (no dependencies, no provided traits) can be created with the
 * convenience factory:
 * <pre>{@code
 * SystemDescriptor.fromCoordinate("rayvion", "audio", Version.parse("1.0.0"));
 * }</pre>
 *
 * @param coordinate   the unique identity of the system
 * @param dependencies the set of trait requirements this system must have satisfied before
 *                     {@link com.rayvion.engine.system.System#init()} is called for
 *                     {@link SystemDependency.RequirementLevel#REQUIRED} entries; must not be
 *                     {@code null}
 * @param provides     the set of trait coordinates that this system advertises to other systems;
 *                     must not be {@code null}
 * @see SystemCoordinate
 * @see SystemDependency
 * @see com.rayvion.engine.system.trait.SystemTraitCoordinate
 */
public record SystemDescriptor(
        SystemCoordinate coordinate,
        Set<SystemDependency> dependencies,
        Set<SystemTraitCoordinate> provides
) {
    /**
     * Creates a descriptor with the given coordinate and no dependencies or provided traits.
     *
     * @param coordinate the unique identity of the system; must not be {@code null}
     */
    public SystemDescriptor(SystemCoordinate coordinate) {
        this(coordinate, Set.of(), Set.of());
    }

    /**
     * Convenience factory that builds a minimal descriptor from raw coordinate components.
     *
     * <p>Equivalent to:
     * <pre>{@code
     * new SystemDescriptor(new SystemCoordinate(namespaceId, id, version));
     * }</pre>
     *
     * @param namespaceId the namespace that owns this system
     * @param id          the local identifier of this system within its namespace
     * @param version     the semantic version of this system
     * @return a new {@code SystemDescriptor} with no dependencies and no provided traits
     */
    public static SystemDescriptor fromCoordinate(String namespaceId, String id, Version version) {
        return new SystemDescriptor(new SystemCoordinate(namespaceId, id, version));
    }
}
