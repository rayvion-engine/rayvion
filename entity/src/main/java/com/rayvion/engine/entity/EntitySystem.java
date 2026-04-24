package com.rayvion.engine.entity;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;

import java.util.Collection;
import java.util.Set;

/**
 * System responsible for managing the lifecycle of entities within the engine.
 * Provides methods to create, remove, and query entities.
 */
public interface EntitySystem extends System {
    /**
     * The coordinate of the entity system trait.
     */
    SystemTraitCoordinate TRAIT = new SystemTraitCoordinate("com.rayvion.engine", "entity", Version.parse("0.1.0"));

    @Override
    default SystemDescriptor getDescriptor() {
        return new SystemDescriptor(
                new com.rayvion.engine.system.descriptor.SystemCoordinate("com.rayvion.engine", "entity", Version.parse("0.1.0")),
                Set.of(),
                Set.of(TRAIT)
        );
    }

    /**
     * Creates a new entity with a unique ID.
     *
     * @return the newly created entity
     */
    Entity createEntity();

    /**
     * Removes an entity from the system by its ID.
     *
     * @param id the ID of the entity to remove
     * @return {@code true} if the entity was found and removed, {@code false} otherwise
     */
    boolean removeEntity(long id);

    /**
     * Checks if an entity with the given ID exists in the system.
     *
     * @param id the ID of the entity to check
     * @return {@code true} if the entity exists, {@code false} otherwise
     */
    boolean hasEntity(long id);

    /**
     * Returns a collection of all entities currently managed by this system.
     *
     * @return an unmodifiable collection of all entities
     */
    Collection<Entity> getEntities();
}

