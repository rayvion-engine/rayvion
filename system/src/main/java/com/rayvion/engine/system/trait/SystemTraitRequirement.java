package com.rayvion.engine.system.trait;

import com.github.zafarkhaja.semver.Version;

import java.util.function.Predicate;

public record SystemTraitRequirement(String id, Predicate<Version> version) {
    public boolean isSatisfiedBy(SystemTraitCoordinate coordinate) {
        return coordinate.id().equals(id) && coordinate.version().satisfies(version);
    }
}
