package com.rayvion.game;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.bindings.BindingEvent;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.input.KeyEvent;
import com.rayvion.engine.physics.PhysicsBody;
import com.rayvion.engine.physics.PhysicsSystem;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.Tickable;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.dependency.SystemDependency;
import com.rayvion.engine.system.trait.SystemTraitRequirement;
import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.entity.Entity;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlayerMovementSystem implements System, Tickable {

    private final SystemDescriptor descriptor;
    private final long playerEntityId;
    private final long worldId;
    private PhysicsSystem physicsSystem;
    private EventManager eventManager;
    private CharacteristicSystem characteristicSystem;
    private final Set<String> activeBindings = ConcurrentHashMap.newKeySet();
    


    public PlayerMovementSystem(long worldId, long playerEntityId) {
        log.info("PlayerMovementSystem: Controlling entity ID: {} in world: {}", playerEntityId, worldId);
        this.descriptor = new SystemDescriptor(
                new SystemCoordinate("com.rayvion.game", "player-movement", Version.parse("1.0.0")),
                Set.of(
                        new SystemDependency(
                                new SystemTraitRequirement("com.rayvion.engine", "physics", v -> v.equals(Version.parse("0.1.0"))),
                                SystemDependency.RequirementLevel.REQUIRED
                        ),
                        new SystemDependency(
                                new SystemTraitRequirement("com.rayvion.engine", "event", v -> v.equals(Version.parse("0.1.0"))),
                                SystemDependency.RequirementLevel.REQUIRED
                        ),
                        new SystemDependency(
                                new SystemTraitRequirement("com.rayvion.engine", "characteristic", v -> true),
                                SystemDependency.RequirementLevel.REQUIRED
                        ),
                        new SystemDependency(
                                new SystemTraitRequirement("com.rayvion.engine", "graphics", v -> true),
                                SystemDependency.RequirementLevel.REQUIRED
                        )
                ),
                Set.of(Tickable.TRAIT)
        );
        this.worldId = worldId;
        this.playerEntityId = playerEntityId;
    }

    @Override
    public SystemDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void onDependencyAdded(System dependency) {
        if (dependency instanceof PhysicsSystem ps) {
            this.physicsSystem = ps;
        } else if (dependency instanceof EventManager em) {
            this.eventManager = em;
        } else if (dependency instanceof CharacteristicSystem cs) {
            this.characteristicSystem = cs;
        }
    }

    @Override
    public void init() {
        if (eventManager != null) {
            eventManager.subscribe(BindingEvent.class, this::handleBindingEvent);
        }
    }

    private void handleBindingEvent(BindingEvent event) {
        log.debug("PlayerMovementSystem: Received binding event: {} type: {}", event.parameter().name(), event.type());
        if (event.type() == KeyEvent.Type.KEY_DOWN) {
            activeBindings.add(event.parameter().name());
        } else if (event.type() == KeyEvent.Type.KEY_UP) {
            activeBindings.remove(event.parameter().name());
        }

        applyMovement(true);
    }

    @Override
    public void tick() {
        if (activeMovementRequested()) {
            applyMovement(true);
        }
    }

    private boolean activeMovementRequested() {
        return activeBindings.contains("Forward")
                || activeBindings.contains("Backward")
                || activeBindings.contains("Left")
                || activeBindings.contains("Right");
    }

    private void applyMovement(boolean shouldApplyVelocity) {
        PhysicsBody body = physicsSystem.getBody(worldId, playerEntityId);
        if (body == null) {
            log.debug("PlayerMovementSystem: Physics body not found for entity {} in world {} (entity might be destroyed)", playerEntityId, worldId);
            return;
        }

        double vx = 0;
        double vy = 0;

        double currentSpeed = characteristicSystem != null ? characteristicSystem.getValue(new Entity(playerEntityId), "speed") : 100.0;

        if (activeBindings.contains("Forward")) vy += currentSpeed;
        if (activeBindings.contains("Backward")) vy -= currentSpeed;
        if (activeBindings.contains("Left")) vx -= currentSpeed;
        if (activeBindings.contains("Right")) vx += currentSpeed;

        if (shouldApplyVelocity) {
            log.trace("PlayerMovementSystem: Setting velocity: {}, {}", vx, vy);
            body.setVelocity(vx, vy);
        }

        if (vx != 0 || vy != 0) {
            double angleRad = Math.atan2(vy, vx);
            body.setRotation(angleRad);
            
            if (characteristicSystem != null) {
                double angleDeg = Math.toDegrees(angleRad);
                if (angleDeg < 0) angleDeg += 360;
                characteristicSystem.setValue(new Entity(playerEntityId), "facing_angle", angleDeg);
                characteristicSystem.setValue(new Entity(playerEntityId), "animation_state", "move");
            }
        } else {
            if (characteristicSystem != null) {
                characteristicSystem.setValue(new Entity(playerEntityId), "animation_state", "idle");
            }
        }
    }
}
