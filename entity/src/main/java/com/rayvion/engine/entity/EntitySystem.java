package com.rayvion.engine.entity;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemDescriptor;

import java.util.Collection;

public interface EntitySystem extends System {
    @Override
    default SystemDescriptor getDescriptor() {
        return SystemDescriptor.fromCoordinate("com.rayvion.engine", "entity", Version.parse("0.1.0"));
    }

    Entity createEntity();

    boolean removeEntity(long id);

    boolean hasEntity(long id);

    Collection<Entity> getEntities();
}
