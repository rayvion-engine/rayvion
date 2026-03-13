package com.rayvion.engine.entity;

import com.rayvion.engine.entity.exceptions.EntityAlreadyExistsException;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class EntitySystemImpl implements EntitySystem {
    private final AtomicInteger eidSerial = new AtomicInteger(0);
    private final Set<Entity> entities = new HashSet<>();

    @Override
    public void init() {

    }

    @Override
    public Entity createEntity(UUID id) throws EntityAlreadyExistsException {
        if(entities.stream().anyMatch(e -> e.id().equals(id)))
            throw new EntityAlreadyExistsException("Entity with id " + id + " already exists");

        Integer eid = eidSerial.getAndIncrement();
        Entity entity = new Entity(id, eid);

        entities.add(entity);
        return entity;
    }

    @Override
    public Optional<Entity> removeEntity(UUID id) {
        Optional<Entity> entity = entities.stream().filter(e -> e.id().equals(id)).findFirst();
        entity.ifPresent(entities::remove);
        return entity;
    }
}
