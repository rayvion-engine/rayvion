package com.rayvion.engine.system.trait;

import com.github.zafarkhaja.semver.Version;

import java.util.function.Predicate;

public record SystemTraitRequirement(String namespaceId, String id, Predicate<Version> version) {
    public boolean isSatisfiedBy(SystemTraitCoordinate coordinate) {
        return coordinate.namespaceId().equals(namespaceId) && coordinate.id().equals(id) && version.test(coordinate.version());
    }
}
