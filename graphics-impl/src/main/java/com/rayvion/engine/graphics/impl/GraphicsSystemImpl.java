package com.rayvion.engine.graphics.impl;

import com.rayvion.engine.graphics.EntityGraphics;
import com.rayvion.engine.graphics.GraphicsSystem;
import com.rayvion.engine.graphics.WorldGraphics;
import com.rayvion.engine.system.System;
import com.rayvion.engine.transform.TransformSystem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GraphicsSystemImpl implements GraphicsSystem {

    private final Map<Long, EntityGraphics> entityGraphicsMap = new ConcurrentHashMap<>();
    private WorldGraphics worldGraphics;
    private TransformSystem transformSystem;

    @Override
    public void init() {
    }

    @Override
    public void onDependencyAdded(System dependency) {
        if (dependency instanceof TransformSystem ts) {
            this.transformSystem = ts;
        }
    }

    @Override
    public void setEntityGraphics(long entityId, EntityGraphics graphics) {
        if (transformSystem == null || !transformSystem.hasTransform(entityId)) {
            throw new IllegalStateException("Cannot set graphics for entity " + entityId + " without a Transform in TransformSystem");
        }
        entityGraphicsMap.put(entityId, graphics);
    }

    @Override
    public EntityGraphics getEntityGraphics(long entityId) {
        return entityGraphicsMap.get(entityId);
    }

    @Override
    public boolean hasEntityGraphics(long entityId) {
        return entityGraphicsMap.containsKey(entityId);
    }

    @Override
    public boolean removeEntityGraphics(long entityId) {
        return entityGraphicsMap.remove(entityId) != null;
    }

    @Override
    public void setWorldGraphics(WorldGraphics worldGraphics) {
        this.worldGraphics = worldGraphics;
    }

    @Override
    public WorldGraphics getWorldGraphics() {
        return worldGraphics;
    }
}
