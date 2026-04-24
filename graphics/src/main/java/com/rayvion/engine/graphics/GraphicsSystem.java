package com.rayvion.engine.graphics;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.dependency.SystemDependency;
import com.rayvion.engine.system.trait.SystemTraitRequirement;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;
import java.util.Set;

/**
 * Engine system responsible for managing all rendering data for entities and
 * the game world.
 *
 * <p>{@code GraphicsSystem} acts as a registry that maps entity IDs to their
 * {@link EntityGraphics} descriptors, holds the active {@link WorldGraphics}
 * descriptor for the current scene, and tracks ancillary display state such as
 * health-bar visibility and interaction prompts.</p>
 *
 * <p>It does not perform rendering itself; the graphics back-end (e.g., a
 * LibGDX renderer) queries this system each frame to determine what to draw.
 * </p>
 *
 * <h2>Dependencies</h2>
 * <p>Requires the {@code transform} system trait (major version 0) to be
 * present before entity graphics can be registered. An entity <em>must</em>
 * have a transform in the {@link com.rayvion.engine.transform.TransformSystem}
 * before calling {@link #setEntityGraphics}.</p>
 *
 * <h2>System coordinates</h2>
 * <ul>
 *   <li>Coordinate: {@code com.rayvion.engine : graphics : 0.1.0}</li>
 *   <li>Provided trait: {@code com.rayvion.engine : graphics : 0.1.0}</li>
 * </ul>
 */
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

    /**
     * Associates an {@link EntityGraphics} descriptor with the given entity.
     *
     * <p>The entity must already have a transform registered in the
     * {@link com.rayvion.engine.transform.TransformSystem}; the graphics
     * back-end uses that transform to position the entity on screen.</p>
     *
     * @param entityId the unique identifier of the entity.
     * @param graphics the graphics descriptor to associate; must not be {@code null}.
     * @throws IllegalStateException if no transform exists for {@code entityId}
     *                               in the {@code TransformSystem}.
     */
    void setEntityGraphics(long entityId, EntityGraphics graphics);

    /**
     * Returns the {@link EntityGraphics} descriptor currently associated with
     * the given entity, or {@code null} if none has been registered.
     *
     * @param entityId the unique identifier of the entity.
     * @return the registered {@link EntityGraphics} descriptor, or {@code null}.
     */
    EntityGraphics getEntityGraphics(long entityId);

    /**
     * Returns {@code true} if the given entity has an {@link EntityGraphics}
     * descriptor registered with this system.
     *
     * @param entityId the unique identifier of the entity.
     * @return {@code true} if a graphics descriptor is registered;
     *         {@code false} otherwise.
     */
    boolean hasEntityGraphics(long entityId);

    /**
     * Removes the {@link EntityGraphics} descriptor for the given entity.
     *
     * @param entityId the unique identifier of the entity.
     * @return {@code true} if a descriptor was removed; {@code false} if no
     *         descriptor was registered for the entity.
     */
    boolean removeEntityGraphics(long entityId);

    /**
     * Sets the world-level graphics descriptor for the current scene.
     *
     * <p>Replaces any previously registered {@link WorldGraphics} descriptor.
     * Pass {@code null} to clear the world graphics entirely.</p>
     *
     * @param worldGraphics the world graphics descriptor, or {@code null} to
     *                      clear it.
     */
    void setWorldGraphics(WorldGraphics worldGraphics);

    /**
     * Returns the currently active world-level graphics descriptor, or
     * {@code null} if none has been set.
     *
     * @return the active {@link WorldGraphics} descriptor, or {@code null}.
     */
    WorldGraphics getWorldGraphics();

    /**
     * Returns a live view of the IDs of all entities that currently have an
     * {@link EntityGraphics} descriptor registered.
     *
     * <p>The returned set reflects subsequent additions and removals without
     * requiring a fresh call. Callers must not modify the returned set.</p>
     *
     * @return an unmodifiable live key-set of entity IDs with registered graphics.
     */
    Set<Long> getEntitiesWithGraphics();

    /**
     * Controls whether a health bar should be displayed above the given entity.
     *
     * @param entityId the unique identifier of the entity.
     * @param visible  {@code true} to enable health-bar rendering;
     *                 {@code false} to disable it.
     */
    void setHealthBarVisible(long entityId, boolean visible);

    /**
     * Returns {@code true} if a health bar is currently enabled for the given
     * entity.
     *
     * @param entityId the unique identifier of the entity.
     * @return {@code true} if the health bar is visible; {@code false} otherwise.
     */
    boolean isHealthBarVisible(long entityId);

    /**
     * Registers an interaction prompt message for the given entity.
     *
     * <p>The graphics back-end may display this message as a tooltip or on-screen
     * label when the player is in range and able to interact with the entity.
     * Replaces any previously set prompt for the same entity.</p>
     *
     * @param entityId the unique identifier of the entity.
     * @param message  the prompt text to display; must not be {@code null}.
     */
    void setInteractionPrompt(long entityId, String message);

    /**
     * Returns the interaction prompt message currently registered for the given
     * entity, or {@code null} if none has been set.
     *
     * @param entityId the unique identifier of the entity.
     * @return the prompt text, or {@code null} if no prompt is registered.
     */
    String getInteractionPrompt(long entityId);

    /**
     * Removes the interaction prompt for the given entity, if one exists.
     *
     * <p>If no prompt is registered this method is a no-op.</p>
     *
     * @param entityId the unique identifier of the entity whose prompt should
     *                 be cleared.
     */
    void removeInteractionPrompt(long entityId);
}
