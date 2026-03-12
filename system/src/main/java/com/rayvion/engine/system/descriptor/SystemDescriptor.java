package com.rayvion.engine.system.descriptor;

import com.rayvion.engine.system.dependency.SystemDependency;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;

import java.util.Set;

public record SystemDescriptor(
        SystemCoordinate coordinate,
        Set<SystemDependency> dependencies,
        Set<SystemTraitCoordinate> provides
) { }
