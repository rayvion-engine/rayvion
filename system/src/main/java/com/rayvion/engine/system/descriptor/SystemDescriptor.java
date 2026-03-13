package com.rayvion.engine.system.descriptor;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.dependency.SystemDependency;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;

import java.util.Set;

public record SystemDescriptor(
        SystemCoordinate coordinate,
        Set<SystemDependency> dependencies,
        Set<SystemTraitCoordinate> provides
) {
    public SystemDescriptor(SystemCoordinate coordinate) {
        this(coordinate, Set.of(), Set.of());
    }

    public static SystemDescriptor fromCoordinate(String id, Version version) {
        return new SystemDescriptor(new SystemCoordinate(id, version));
    }
}
