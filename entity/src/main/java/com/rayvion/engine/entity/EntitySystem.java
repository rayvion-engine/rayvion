package com.rayvion.engine.entity;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemDescriptor;

import java.util.Optional;
import java.util.UUID;

public interface EntitySystem extends System {
    @Override
    default SystemDescriptor getDescriptor() {
        return SystemDescriptor.fromCoordinate("com.rayvion.engine:entity", Version.parse("0.1.0"));
    }

    Entity createEntity(UUID id);

    Optional<Entity> removeEntity(UUID id);
}
