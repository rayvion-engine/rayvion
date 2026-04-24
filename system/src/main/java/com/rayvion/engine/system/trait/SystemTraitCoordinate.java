package com.rayvion.engine.system.trait;

import com.github.zafarkhaja.semver.Version;

/**
 * Uniquely identifies a trait that a system can provide or require.
 *
 * <p>Traits are the primary mechanism through which systems declare capabilities and depend on
 * one another without creating direct class-level coupling. A system advertises the traits it
 * provides via its {@link com.rayvion.engine.system.descriptor.SystemDescriptor}, and other
 * systems declare dependencies on those traits via
 * {@link com.rayvion.engine.system.dependency.SystemDependency}.
 *
 * <p>A coordinate is a three-part key: namespace, local identifier, and semantic version. All
 * three components must match for a trait to satisfy a
 * {@link SystemTraitRequirement}.
 *
 * @param namespaceId the namespace that defines this trait (e.g. {@code "rayvion"} for built-in
 *                    traits)
 * @param id          the local, human-readable name of the trait within its namespace
 *                    (e.g. {@code "tickable"} or {@code "renderable"})
 * @param version     the semantic version of this trait; matched against the version predicate
 *                    in {@link SystemTraitRequirement}
 * @see SystemTraitRequirement
 */
public record SystemTraitCoordinate(String namespaceId, String id, Version version) { }
