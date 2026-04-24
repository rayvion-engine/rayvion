package com.rayvion.engine.graphics.impl;

import com.rayvion.engine.graphics.EntityGraphics;
import com.rayvion.engine.graphics.GraphicsSystem;
import com.rayvion.engine.graphics.WorldGraphics;
import com.rayvion.engine.system.System;
import com.rayvion.engine.transform.TransformSystem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link GraphicsSystem} backed by
 * {@link java.util.concurrent.ConcurrentHashMap} stores for thread-safe
 * access from rendering and game-logic threads.
 *
 * <p>This class stores three independent data sets:</p>
 * <ul>
 *   <li><b>Entity graphics map</b> – maps entity IDs to their
 *       {@link com.rayvion.engine.graphics.EntityGraphics} descriptors.</li>
 *   <li><b>Health-bar set</b> – tracks which entity IDs have health-bar
 *       display enabled.</li>
 *   <li><b>Interaction prompts map</b> – maps entity IDs to their
 *       interaction-prompt strings.</li>
 * </ul>
 *
 * <p><b>Transform requirement:</b> {@link #setEntityGraphics} enforces that
 * the entity already has a transform in the injected
 * {@link com.rayvion.engine.transform.TransformSystem} before registration
 * is allowed, throwing {@link IllegalStateException} otherwise.</p>
 *
 * <p>The {@link com.rayvion.engine.transform.TransformSystem} reference is
 * injected automatically by the engine's system manager via
 * {@link #onDependencyAdded}.</p>
 */
public class GraphicsSystemImpl implements GraphicsSystem {

    private final Map<Long, EntityGraphics> entityGraphicsMap = new ConcurrentHashMap<>();
    private final java.util.Set<Long> entitiesWithHealthBar = ConcurrentHashMap.newKeySet();
    private final Map<Long, String> interactionPrompts = new ConcurrentHashMap<>();
    private WorldGraphics worldGraphics;
    private TransformSystem transformSystem;

    /** No-op; this implementation requires no initialisation beyond construction. */
    @Override
    public void init() {
    }

    /**
     * Captures injected system dependencies.
     *
     * <p>When the engine's system manager resolves the required
     * {@code transform} trait and calls this method with the corresponding
     * {@link TransformSystem}, the reference is stored so that
     * {@link #setEntityGraphics} can validate entity transforms.</p>
     *
     * @param dependency the newly available dependency system; only
     *                   {@link TransformSystem} instances are handled;
     *                   all other types are silently ignored.
     */
    @Override
    public void onDependencyAdded(System dependency) {
        if (dependency instanceof TransformSystem ts) {
            this.transformSystem = ts;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException if the {@link TransformSystem} dependency
     *         has not been injected yet, or if no transform has been registered
     *         for {@code entityId} in that system.  Both conditions indicate a
     *         programming error: entities must be given a transform before
     *         receiving graphics data.
     */
    @Override
    public void setEntityGraphics(long entityId, EntityGraphics graphics) {
        if (transformSystem == null || !transformSystem.hasTransform(entityId)) {
            throw new IllegalStateException("Cannot set graphics for entity " + entityId + " without a Transform in TransformSystem");
        }
        entityGraphicsMap.put(entityId, graphics);
    }

    /** {@inheritDoc} */
    @Override
    public EntityGraphics getEntityGraphics(long entityId) {
        return entityGraphicsMap.get(entityId);
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasEntityGraphics(long entityId) {
        return entityGraphicsMap.containsKey(entityId);
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeEntityGraphics(long entityId) {
        return entityGraphicsMap.remove(entityId) != null;
    }

    /** {@inheritDoc} */
    @Override
    public void setWorldGraphics(WorldGraphics worldGraphics) {
        this.worldGraphics = worldGraphics;
    }

    /** {@inheritDoc} */
    @Override
    public WorldGraphics getWorldGraphics() {
        return worldGraphics;
    }

    /** {@inheritDoc} */
    @Override
    public java.util.Set<Long> getEntitiesWithGraphics() {
        return entityGraphicsMap.keySet();
    }

    /** {@inheritDoc} */
    @Override
    public void setHealthBarVisible(long entityId, boolean visible) {
        if (visible) {
            entitiesWithHealthBar.add(entityId);
        } else {
            entitiesWithHealthBar.remove(entityId);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isHealthBarVisible(long entityId) {
        return entitiesWithHealthBar.contains(entityId);
    }

    /** {@inheritDoc} */
    @Override
    public void setInteractionPrompt(long entityId, String message) {
        interactionPrompts.put(entityId, message);
    }

    /** {@inheritDoc} */
    @Override
    public String getInteractionPrompt(long entityId) {
        return interactionPrompts.get(entityId);
    }

    /** {@inheritDoc} */
    @Override
    public void removeInteractionPrompt(long entityId) {
        interactionPrompts.remove(entityId);
    }
}
