package com.rayvion.engine.transform;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemDescriptor;

import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;
import java.util.Set;

public interface TransformSystem extends System {
    @Override
    default SystemDescriptor getDescriptor() {
        return new SystemDescriptor(
                new SystemCoordinate("com.rayvion.engine", "transform", Version.parse("0.1.0")),
                Set.of(),
                Set.of(new SystemTraitCoordinate("com.rayvion.engine", "transform", Version.parse("0.1.0")))
        );
    }

    void setTransform(long entityId, Transform transform);
    
    Transform getTransform(long entityId);
    
    boolean hasTransform(long entityId);
    
    boolean removeTransform(long entityId);
}
