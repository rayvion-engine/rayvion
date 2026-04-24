package com.rayvion.engine.system.descriptor;

import com.github.zafarkhaja.semver.Version;

/**
 * Uniquely identifies a system within the engine's system registry.
 *
 * <p>A coordinate is a three-part key composed of a namespace, a local identifier, and a semantic
 * version. Together they allow the {@code SystemManager} to
 * unambiguously distinguish systems even when multiple versions of the same logical system coexist.
 *
 * <p>Example usage:
 * <pre>{@code
 * SystemCoordinate coord = new SystemCoordinate("rayvion", "graphics", Version.parse("2.0.0"));
 * }</pre>
 *
 * @param namespaceId the namespace that owns this system (e.g. {@code "rayvion"} for built-in
 *                    systems, or a mod/plugin identifier for third-party systems)
 * @param id          the local, human-readable identifier of the system within its namespace
 *                    (e.g. {@code "graphics"} or {@code "audio"})
 * @param version     the semantic version of the system; used by
 *                    {@link com.rayvion.engine.system.trait.SystemTraitRequirement} to enforce
 *                    version constraints on traits
 */
public record SystemCoordinate(String namespaceId, String id, Version version) { }
