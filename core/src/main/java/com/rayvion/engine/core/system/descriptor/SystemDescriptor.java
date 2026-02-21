package com.rayvion.engine.core.system.descriptor;

import com.rayvion.engine.core.system.dependency.SystemDependency;
import com.rayvion.engine.core.system.trait.SystemTraitCoordinate;

import java.util.Set;

public record SystemDescriptor(
        SystemCoordinate coordinate,
        Set<SystemDependency> dependencies,
        Set<SystemTraitCoordinate> provides
) { }
