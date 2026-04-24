package com.rayvion.engine.graphics;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.dependency.SystemDependency;
import com.rayvion.engine.system.trait.SystemTraitRequirement;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;
import java.util.Set;

public interface GraphicsSystem extends System {
    @Override
    default SystemDescriptor getDescriptor() {
        return new SystemDescriptor(
                new SystemCoordinate("com.rayvion.engine", "graphics", Version.parse("0.1.0")),
                Set.of(new SystemDependency(
                        new SystemTraitRequirement("com.rayvion.engine", "transform", version -> version.getMajorVersion() == 0),
                        SystemDependency.RequirementLevel.REQUIRED
                )),
                Set.of(new SystemTraitCoordinate("com.rayvion.engine", "graphics", Version.parse("0.1.0")))
        );
    }

    void setEntityGraphics(long entityId, EntityGraphics graphics);
    
    EntityGraphics getEntityGraphics(long entityId);
    
    boolean hasEntityGraphics(long entityId);
    
    boolean removeEntityGraphics(long entityId);
    
    void setWorldGraphics(WorldGraphics worldGraphics);
    
    WorldGraphics getWorldGraphics();

    java.util.Set<Long> getEntitiesWithGraphics();
}
