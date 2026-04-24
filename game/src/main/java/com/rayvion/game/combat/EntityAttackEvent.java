package com.rayvion.game.combat;

import com.rayvion.engine.event.Event;

/**
 * Event fired when an entity attempts to perform an attack with their equipped weapon.
 */
public record EntityAttackEvent(long attackerId) implements Event {
}
