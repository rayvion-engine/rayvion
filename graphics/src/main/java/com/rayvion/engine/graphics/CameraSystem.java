package com.rayvion.engine.graphics;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.dependency.SystemDependency;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;
import com.rayvion.engine.system.trait.SystemTraitRequirement;

import java.util.Set;

/**
 * Engine system that controls the 2-D game camera.
 *
 * <p>{@code CameraSystem} manages the viewport into the game world. It supports
 * three complementary modes of operation:</p>
 * <ol>
 *   <li><b>Entity tracking</b> – when a target entity is set via
 *       {@link #setTargetEntity}, the camera automatically follows that entity's
 *       position every frame by reading from the
 *       {@link com.rayvion.engine.transform.TransformSystem}.</li>
 *   <li><b>Manual positioning</b> – {@link #setPosition} allows the camera to
 *       be placed at an explicit world coordinate, overriding entity tracking
 *       until the next entity-follow update.</li>
 *   <li><b>Screen shake</b> – {@link #shake} applies a short-lived random
 *       positional offset each frame to simulate impact or explosion effects.
 *       Shake is applied on top of either tracking or manual position.</li>
 * </ol>
 *
 * <h2>Dependencies</h2>
 * <p>Requires the {@code transform} system trait (major version 0) to read
 * entity positions during {@link #update()}.</p>
 *
 * <h2>System coordinates</h2>
 * <ul>
 *   <li>Coordinate: {@code com.rayvion.engine : camera : 0.1.0}</li>
 *   <li>Provided trait: {@code com.rayvion.engine : camera : 0.1.0}</li>
 * </ul>
 */
public interface CameraSystem extends System {
    /**
     * {@inheritDoc}
     *
     * @return a descriptor identifying this system as
     *         {@code com.rayvion.engine : camera : 0.1.0}, requiring the
     *         {@code transform} trait and advertising the {@code camera} trait.
     */
    @Override
    default SystemDescriptor getDescriptor() {
        return new SystemDescriptor(
                new SystemCoordinate("com.rayvion.engine", "camera", Version.parse("0.1.0")),
                Set.of(new SystemDependency(
                        new SystemTraitRequirement("com.rayvion.engine", "transform", version -> version.majorVersion() == 0),
                        SystemDependency.RequirementLevel.REQUIRED
                )),
                Set.of(new SystemTraitCoordinate("com.rayvion.engine", "camera", Version.parse("0.1.0")))
        );
    }

    /**
     * Sets the entity for the camera to follow.
     *
     * <p>Once set, {@link #update()} will move the camera to the entity's
     * transform position every frame. Pass {@code -1} to stop following.</p>
     *
     * @param entityId the ID of the entity to follow, or {@code -1} to
     *                 disable entity tracking.
     */
    void setTargetEntity(long entityId);

    /**
     * Returns the ID of the entity the camera is currently following.
     *
     * @return the entity ID, or {@code -1} if not following any entity.
     */
    long getTargetEntity();

    /**
     * Moves the camera to the specified world-coordinate position.
     *
     * <p>If a target entity is set, this manually-set position will be
     * overridden on the next call to {@link #update()}.</p>
     *
     * @param x the X coordinate in world units.
     * @param y the Y coordinate in world units.
     */
    void setPosition(double x, double y);

    /**
     * Returns the current X coordinate of the camera in world units.
     *
     * @return the X coordinate; includes any active shake offset.
     */
    double getX();

    /**
     * Returns the current Y coordinate of the camera in world units.
     *
     * @return the Y coordinate; includes any active shake offset.
     */
    double getY();

    /**
     * Sets the camera zoom level.
     *
     * <p>A value of {@code 1.0} is the default (no zoom). Values greater than
     * {@code 1.0} zoom in; values between {@code 0} and {@code 1.0} zoom out.
     * Negative values are undefined.</p>
     *
     * @param zoom the desired zoom level; must be positive.
     */
    void setZoom(float zoom);

    /**
     * Returns the current zoom level.
     *
     * @return the zoom level; {@code 1.0f} by default.
     */
    float getZoom();

    /**
     * Advances the camera state for the current frame.
     *
     * <p>If a target entity is set and a valid transform exists for it in the
     * {@link com.rayvion.engine.transform.TransformSystem}, the camera position
     * is updated to match that entity's world coordinates.  Any active
     * shake effect is then applied on top by adding a random offset within
     * &plusmn;{@code intensity} on each axis.</p>
     *
     * @see #shake(double, long)
     * @see #setTargetEntity(long)
     */
    void update();

    /**
     * Triggers a screen-shake effect for the given duration.
     *
     * <p>While the effect is active, each call to {@link #update()} adds a
     * uniformly-distributed random offset in the range
     * [{@code -intensity}, {@code +intensity}] to both the X and Y camera
     * coordinates. The effect ends automatically after {@code durationMs}
     * milliseconds.</p>
     *
     * <p>Calling this method while a shake is already in progress replaces the
     * previous shake parameters immediately.</p>
     *
     * @param intensity  the maximum positional displacement in world units
     *                   per frame; must be non-negative.
     * @param durationMs the total duration of the shake effect in milliseconds;
     *                   pass {@code 0} to cancel any active shake immediately.
     */
    void shake(double intensity, long durationMs);
}
