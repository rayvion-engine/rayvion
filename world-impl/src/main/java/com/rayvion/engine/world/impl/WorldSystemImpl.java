package com.rayvion.engine.world.impl;

import com.rayvion.engine.world.World;
import com.rayvion.engine.world.WorldSystem;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WorldSystemImpl implements WorldSystem {
    private final Map<Long, World> worlds = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> worldEntities = new ConcurrentHashMap<>();

    @Override
    public void init() {
    }

    @Override
    public void addWorld(World world) {
        worlds.put(world.getId(), world);
        worldEntities.putIfAbsent(world.getId(), ConcurrentHashMap.newKeySet());
    }

    @Override
    public boolean removeWorld(long worldId) {
        worldEntities.remove(worldId);
        return worlds.remove(worldId) != null;
    }

    @Override
    public World getWorld(long worldId) {
        return worlds.get(worldId);
    }

    @Override
    public void addEntityToWorld(long worldId, long entityId) {
        Set<Long> entities = worldEntities.get(worldId);
        if (entities != null) {
            entities.add(entityId);
        }
    }

    @Override
    public boolean removeEntityFromWorld(long worldId, long entityId) {
        Set<Long> entities = worldEntities.get(worldId);
        if (entities != null) {
            return entities.remove(entityId);
        }
        return false;
    }

    @Override
    public Collection<Long> getEntities(long worldId) {
        Set<Long> entities = worldEntities.get(worldId);
        if (entities == null) return Collections.emptySet();
        return Collections.unmodifiableSet(entities);
    }
}
