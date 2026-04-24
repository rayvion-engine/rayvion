package com.rayvion.engine.transform.impl;

import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default, in-memory implementation of {@link TransformSystem}.
 *
 * <p>All {@link com.rayvion.engine.transform.Transform} components are stored in
 * a {@link ConcurrentHashMap} keyed by entity ID, ensuring safe concurrent access
 * from multiple threads (e.g. update and render threads) without external
 * synchronisation.
 *
 * <p>This class is intentionally minimal: it delegates all coordination to the
 * system manager and keeps no additional state beyond the transform map itself.
 */
public class TransformSystemImpl implements TransformSystem {
    /** Thread-safe map from entity ID to its current {@link com.rayvion.engine.transform.Transform}. */
    private final Map<Long, Transform> transforms = new ConcurrentHashMap<>();

    /**
     * Initialises this system.
     *
     * <p>No initialisation work is required for the in-memory transform store;
     * the underlying {@link ConcurrentHashMap} is ready upon construction.
     */
    @Override
    public void init() {
    }

    /**
     * {@inheritDoc}
     *
     * <p>Internally delegates to {@link ConcurrentHashMap#put(Object, Object)},
     * replacing any transform that was previously registered for {@code entityId}.
     */
    @Override
    public void setTransform(long entityId, Transform transform) {
        transforms.put(entityId, transform);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Internally delegates to {@link ConcurrentHashMap#get(Object)}.
     * Returns {@code null} when no transform exists for {@code entityId}.
     */
    @Override
    public Transform getTransform(long entityId) {
        return transforms.get(entityId);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Internally delegates to {@link ConcurrentHashMap#containsKey(Object)}.
     */
    @Override
    public boolean hasTransform(long entityId) {
        return transforms.containsKey(entityId);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Removes the entry from the underlying map and returns {@code true} only
     * if a non-{@code null} value was previously associated with {@code entityId}.
     */
    @Override
    public boolean removeTransform(long entityId) {
        return transforms.remove(entityId) != null;
    }
}
