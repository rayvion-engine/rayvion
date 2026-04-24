package com.rayvion.engine.entity;

import com.rayvion.engine.event.Event;

/**
 * Event published when an entity dies (typically when its health drops to zero or below).
 *
 * @param entityId the ID of the entity that died
 * @param worldId  the ID of the world where the entity was located
 */
public record EntityDeathEvent(long entityId, long worldId) implements Event {
}

