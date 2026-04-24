package com.rayvion.engine.graphics.impl;

import com.rayvion.engine.graphics.CameraSystem;
import com.rayvion.engine.system.System;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;

/**
 * Default implementation of {@link CameraSystem}.
 *
 * <p>The camera state is stored as mutable {@code double}/{@code float} fields
 * updated on every call to {@link #update()}.  Entity tracking and screen-shake
 * are both applied inside that single method to keep the order of operations
 * deterministic:</p>
 * <ol>
 *   <li>If {@link #targetEntity} is not {@code -1} and the entity has a
 *       registered transform, the camera position is snapped to the entity's
 *       world coordinates.</li>
 *   <li>If a shake effect is still active (i.e.,
 *       {@link java.lang.System#currentTimeMillis()} &lt;
 *       {@link #shakeEndTime}), a random uniform offset in
 *       [&minus;{@link #shakeIntensity}, +{@link #shakeIntensity}] is added
 *       to both axes.</li>
 * </ol>
 *
 * <p>The {@link com.rayvion.engine.transform.TransformSystem} dependency is
 * injected by the engine's system manager via {@link #onDependencyAdded}.  If
 * the dependency has not arrived yet when {@link #update()} is called, entity
 * tracking is silently skipped.</p>
 */
public class CameraSystemImpl implements CameraSystem {
    private long targetEntity = -1;
    private double x, y;
    private float zoom = 1.0f;
    private TransformSystem transformSystem;
    
    private double shakeIntensity = 0;
    private long shakeEndTime = 0;
    private final java.util.Random random = new java.util.Random();

    /** No-op; this implementation requires no initialisation beyond construction. */
    @Override
    public void init() {
    }

    /**
     * Captures injected system dependencies.
     *
     * <p>When the engine's system manager provides the resolved
     * {@link TransformSystem}, it is stored so that {@link #update()} can
     * read entity positions for camera tracking.  Any other dependency type
     * is silently ignored.</p>
     *
     * @param dependency the newly available dependency system.
     */
    @Override
    public void onDependencyAdded(System dependency) {
        if (dependency instanceof TransformSystem ts) {
            this.transformSystem = ts;
        }
    }

    @Override
    public void setTargetEntity(long entityId) {
        this.targetEntity = entityId;
    }

    @Override
    public long getTargetEntity() {
        return targetEntity;
    }

    @Override
    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    @Override
    public float getZoom() {
        return zoom;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementation detail: entity tracking is performed first, setting
     * {@link #x} and {@link #y} to the target's transform coordinates.  The
     * shake offset is then added on top, so the shake displacement is
     * independent of whether the camera is tracking an entity or positioned
     * manually.</p>
     */
    @Override
    public void update() {
        if (targetEntity != -1 && transformSystem != null && transformSystem.hasTransform(targetEntity)) {
            Transform t = transformSystem.getTransform(targetEntity);
            this.x = t.getX();
            this.y = t.getY();
        }
        
        long currentTime = java.lang.System.currentTimeMillis();
        if (currentTime < shakeEndTime) {
            this.x += (random.nextDouble() * 2.0 - 1.0) * shakeIntensity;
            this.y += (random.nextDouble() * 2.0 - 1.0) * shakeIntensity;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Implementation detail: {@code shakeEndTime} is set to
     * {@code System.currentTimeMillis() + durationMs}.  If this method is
     * called while a shake is already running, both the intensity and the
     * end-time are replaced immediately, effectively restarting the effect.</p>
     */
    @Override
    public void shake(double intensity, long durationMs) {
        this.shakeIntensity = intensity;
        this.shakeEndTime = java.lang.System.currentTimeMillis() + durationMs;
    }
}
