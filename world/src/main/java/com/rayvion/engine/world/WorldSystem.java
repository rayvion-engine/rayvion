package com.rayvion.engine.world;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemDescriptor;

import java.util.Collection;

public interface WorldSystem extends System {
    @Override
    default SystemDescriptor getDescriptor() {
        return SystemDescriptor.fromCoordinate("com.rayvion.engine", "world", Version.parse("0.1.0"));
    }

    void addWorld(World world);

    boolean removeWorld(long worldId);

    World getWorld(long worldId);

    void addEntityToWorld(long worldId, long entityId);

    boolean removeEntityFromWorld(long worldId, long entityId);

    Collection<Long> getEntities(long worldId);
}
