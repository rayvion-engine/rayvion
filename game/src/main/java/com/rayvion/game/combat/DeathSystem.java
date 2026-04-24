package com.rayvion.game.combat;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.ai.AiSystem;
import com.rayvion.engine.characteristic.CharacteristicChangedEvent;
import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.entity.EntityDeathEvent;
import com.rayvion.engine.entity.EntitySystem;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.graphics.GraphicsSystem;
import com.rayvion.engine.physics.PhysicsSystem;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.dependency.SystemDependency;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitRequirement;
import com.rayvion.engine.transform.TransformSystem;
import com.rayvion.engine.world.WorldSystem;
import com.rayvion.engine.audio.AudioSystem;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * System that monitors entity health and handles cleanup when it reaches zero.
 */
@Slf4j
public class DeathSystem implements System {

    private final SystemDescriptor descriptor;
    private final long managedWorldId;

    private EventManager eventManager;
    private EntitySystem entitySystem;
    private WorldSystem worldSystem;
    private PhysicsSystem physicsSystem;
    private CharacteristicSystem characteristicSystem;
    private TransformSystem transformSystem;
    private GraphicsSystem graphicsSystem;
    private AiSystem aiSystem;
    private AudioSystem audioSystem;

    public DeathSystem(long worldId) {
        this.managedWorldId = worldId;
        this.descriptor = new SystemDescriptor(
                new SystemCoordinate("com.rayvion.game", "death-system", Version.parse("1.0.0")),
                Set.of(
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "event", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "entity", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "world", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "physics", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "characteristic", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "audio", v -> true), SystemDependency.RequirementLevel.REQUIRED)
                ),
                Set.of()
        );
    }

    @Override
    public SystemDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void onDependencyAdded(System dependency) {
        if (dependency instanceof EventManager em) eventManager = em;
        else if (dependency instanceof EntitySystem es) entitySystem = es;
        else if (dependency instanceof WorldSystem ws) worldSystem = ws;
        else if (dependency instanceof PhysicsSystem ps) physicsSystem = ps;
        else if (dependency instanceof CharacteristicSystem cs) characteristicSystem = cs;
        else if (dependency instanceof TransformSystem ts) transformSystem = ts;
        else if (dependency instanceof GraphicsSystem gs) graphicsSystem = gs;
        else if (dependency instanceof AiSystem as) aiSystem = as;
        else if (dependency instanceof AudioSystem as) audioSystem = as;
    }

    @Override
    public void init() {
        if (eventManager != null) {
            eventManager.registerEventType(EntityDeathEvent.class);
            eventManager.subscribe(CharacteristicChangedEvent.class, this::handleCharacteristicChanged);
            log.info("DeathSystem initialized for world {}", managedWorldId);
        }
    }

    private void handleCharacteristicChanged(CharacteristicChangedEvent<?> event) {
        if ("health".equals(event.getCharacteristicId()) && event.getNewValue() instanceof Double health) {
            if (health <= 0) {
                handleDeath(event.getEntity());
            }
        }
    }

    private void handleDeath(Entity entity) {
        long entityId = entity.id();
        log.info("DeathSystem: Entity {} has died in world {}", entityId, managedWorldId);

        if (audioSystem != null) {
            audioSystem.playSound("death");
        }

        // 2. Remove from physics
        if (physicsSystem != null) {
            physicsSystem.removeBody(managedWorldId, entityId);
        }

        // 3. Remove from AI
        if (aiSystem != null) {
            aiSystem.removeStrategy(entityId);
        }

        // 4. Remove from graphics
        if (graphicsSystem != null) {
            graphicsSystem.removeEntityGraphics(entityId);
        }

        // 5. Remove from transform
        if (transformSystem != null) {
            transformSystem.removeTransform(entityId);
        }

        // 6. Remove from world
        if (worldSystem != null) {
            worldSystem.removeEntityFromWorld(managedWorldId, entityId);
        }

        // 7. Remove from entity system
        if (entitySystem != null) {
            entitySystem.removeEntity(entityId);
        }

        // 8. Notify others (must be last so subscribers see the cleanup results)
        if (eventManager != null) {
            eventManager.publish(new EntityDeathEvent(entityId, managedWorldId));
        }
    }
}
