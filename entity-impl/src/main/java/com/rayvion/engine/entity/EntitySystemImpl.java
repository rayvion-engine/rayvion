package com.rayvion.engine.entity;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link EntitySystem} that manages entities using a thread-safe approach.
 * It uses an {@link java.util.concurrent.atomic.AtomicLong} to generate unique IDs and a
 * thread-safe set to store existing entity IDs.
 */
public class EntitySystemImpl implements EntitySystem {

    private final AtomicLong nextId = new AtomicLong(0);
    private final Set<Long> entities = ConcurrentHashMap.newKeySet();

    @Override
    public void init() {
    }

    @Override
    public Entity createEntity() {
        long id = nextId.getAndIncrement();
        entities.add(id);
        return new Entity(id);
    }

    @Override
    public boolean removeEntity(long id) {
        return entities.remove(id);
    }

    @Override
    public boolean hasEntity(long id) {
        return entities.contains(id);
    }

    @Override
    public Collection<Entity> getEntities() {
        return entities.stream().map(Entity::new).collect(Collectors.toUnmodifiableSet());
    }
}
