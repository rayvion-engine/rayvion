package com.rayvion.engine.transform.impl;

import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransformSystemImpl implements TransformSystem {
    private final Map<Long, Transform> transforms = new ConcurrentHashMap<>();

    @Override
    public void init() {
    }

    @Override
    public void setTransform(long entityId, Transform transform) {
        transforms.put(entityId, transform);
    }

    @Override
    public Transform getTransform(long entityId) {
        return transforms.get(entityId);
    }

    @Override
    public boolean hasTransform(long entityId) {
        return transforms.containsKey(entityId);
    }

    @Override
    public boolean removeTransform(long entityId) {
        return transforms.remove(entityId) != null;
    }
}
