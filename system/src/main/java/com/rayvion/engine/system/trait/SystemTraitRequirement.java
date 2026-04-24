package com.rayvion.engine.system.trait;

import com.github.zafarkhaja.semver.Version;

import java.util.function.Predicate;

/**
 * Describes a version-aware requirement for a specific system trait.
 *
 * <p>A {@code SystemTraitRequirement} pairs a trait's namespace and identifier with a
 * {@link Predicate} over {@link Version}. When the
 * {@code SystemManager} evaluates dependencies, it asks each
 * {@link com.rayvion.engine.system.dependency.SystemDependency} whether it is satisfied by a
 * candidate system's descriptor, which in turn delegates to this class to check both the trait
 * identity and the version range.
 *
 * <p>Example — require any {@code 1.x} version of the {@code rayvion:tickable} trait:
 * <pre>{@code
 * new SystemTraitRequirement(
 *     "rayvion",
 *     "tickable",
 *     v -> v.getMajorVersion() == 1
 * );
 * }</pre>
 *
 * @param namespaceId the namespace of the required trait; must match the candidate trait's
 *                    {@link SystemTraitCoordinate#namespaceId()} exactly
 * @param id          the local identifier of the required trait; must match the candidate
 *                    trait's {@link SystemTraitCoordinate#id()} exactly
 * @param version     a predicate applied to the candidate trait's version; return {@code true}
 *                    to accept the version
 * @see SystemTraitCoordinate
 * @see com.rayvion.engine.system.dependency.SystemDependency
 */
public record SystemTraitRequirement(String namespaceId, String id, Predicate<Version> version) {

    /**
     * Returns {@code true} if the given trait coordinate satisfies this requirement.
     *
     * <p>Satisfaction requires that both the namespace and local identifier match exactly,
     * <em>and</em> that the coordinate's version passes the {@link #version()} predicate.
     *
     * @param coordinate the trait coordinate of a candidate system to test; must not be
     *                   {@code null}
     * @return {@code true} if the coordinate satisfies this requirement
     */
    public boolean isSatisfiedBy(SystemTraitCoordinate coordinate) {
        return coordinate.namespaceId().equals(namespaceId) && coordinate.id().equals(id) && version.test(coordinate.version());
    }
}
