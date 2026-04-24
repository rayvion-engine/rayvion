package com.rayvion.engine.transform;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemDescriptor;

import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;
import java.util.Set;

/**
 * Engine system responsible for managing {@link Transform} components attached
 * to entities.
 *
 * <p>{@code TransformSystem} forms the backbone of spatial awareness in the
 * Rayvion engine. Every entity that exists in the game world — whether it
 * participates in physics, rendering, or AI — is expected to own a
 * {@link Transform} that describes its current position and orientation.
 *
 * <p>Implementations must be thread-safe; the default implementation
 * ({@code TransformSystemImpl}) uses a {@link java.util.concurrent.ConcurrentHashMap}
 * for this purpose.
 *
 * <p>This interface self-describes via {@link #getDescriptor()} so that the
 * system manager can automatically register and wire it without manual
 * configuration.
 */
public interface TransformSystem extends System {
    /**
     * Returns the {@link SystemDescriptor} that uniquely identifies this system
     * within the engine registry.
     *
     * <p>The coordinate is {@code com.rayvion.engine:transform:0.1.0}. The system
     * exposes a single trait with the same coordinate so that other systems can
     * declare a dependency on the transform capability without coupling to the
     * concrete implementation.
     *
     * @return the descriptor for this system
     */
    @Override
    default SystemDescriptor getDescriptor() {
        return new SystemDescriptor(
                new SystemCoordinate("com.rayvion.engine", "transform", Version.parse("0.1.0")),
                Set.of(),
                Set.of(new SystemTraitCoordinate("com.rayvion.engine", "transform", Version.parse("0.1.0")))
        );
    }

    /**
     * Associates the given {@link Transform} with the specified entity, replacing
     * any previously stored transform for that entity.
     *
     * @param entityId  the unique identifier of the entity
     * @param transform the transform to store; must not be {@code null}
     */
    void setTransform(long entityId, Transform transform);

    /**
     * Returns the {@link Transform} currently associated with the specified entity,
     * or {@code null} if no transform has been set.
     *
     * @param entityId the unique identifier of the entity
     * @return the entity's transform, or {@code null} if absent
     */
    Transform getTransform(long entityId);

    /**
     * Returns {@code true} if a {@link Transform} is currently registered for
     * the specified entity.
     *
     * @param entityId the unique identifier of the entity
     * @return {@code true} if the entity has a transform; {@code false} otherwise
     */
    boolean hasTransform(long entityId);

    /**
     * Removes the {@link Transform} associated with the specified entity.
     *
     * @param entityId the unique identifier of the entity
     * @return {@code true} if a transform existed and was removed;
     *         {@code false} if no transform was registered for that entity
     */
    boolean removeTransform(long entityId);
}
