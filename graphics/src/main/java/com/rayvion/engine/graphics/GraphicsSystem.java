package com.rayvion.engine.graphics;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemDescriptor;

public interface GraphicsSystem extends System {
    @Override
    default SystemDescriptor getDescriptor() {
        return SystemDescriptor.fromCoordinate("com.rayvion.engine", "graphics", Version.parse("0.1.0"));
    }

    void setEntityGraphics(long entityId, EntityGraphics graphics);
    
    EntityGraphics getEntityGraphics(long entityId);
    
    boolean hasEntityGraphics(long entityId);
    
    boolean removeEntityGraphics(long entityId);
    
    void setWorldGraphics(WorldGraphics worldGraphics);
    
    WorldGraphics getWorldGraphics();
}
