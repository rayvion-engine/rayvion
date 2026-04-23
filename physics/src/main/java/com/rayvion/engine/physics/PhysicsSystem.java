package com.rayvion.engine.physics;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemDescriptor;

public interface PhysicsSystem extends System {
    @Override
    default SystemDescriptor getDescriptor() {
        return SystemDescriptor.fromCoordinate("com.rayvion.engine", "physics", Version.parse("0.1.0"));
    }

    PhysicsBody createBoxBody(long worldId, long entityId, double width, double height, boolean isStatic);

    PhysicsBody createCircleBody(long worldId, long entityId, double radius, boolean isStatic);

    PhysicsBody getBody(long worldId, long entityId);
    
    boolean removeBody(long worldId, long entityId);

    void update(double delta);
}
