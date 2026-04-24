package com.rayvion.game.mechanism;

import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.entity.EntitySystem;
import com.rayvion.engine.graphics.GraphicsSystem;
import com.rayvion.engine.graphics.TextureGraphics;
import com.rayvion.engine.physics.PhysicsSystem;
import com.rayvion.engine.system.Tickable;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import com.rayvion.engine.audio.AudioSystem;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.github.zafarkhaja.semver.Version;
import java.util.Set;

/**
 * System that manages interactive mechanisms like levers and gates.
 */
@Slf4j
public class MechanismSystem implements Tickable, com.rayvion.engine.system.System {
    @Override
    public SystemDescriptor getDescriptor() {
        return new SystemDescriptor(
                new SystemCoordinate("com.rayvion.game", "mechanism", Version.parse("0.1.0")),
                Set.of(),
                Set.of(Tickable.TRAIT)
        );
    }
    private final EntitySystem entitySystem;
    private final TransformSystem transformSystem;
    private final GraphicsSystem graphicsSystem;
    private final PhysicsSystem physicsSystem;
    private final AudioSystem audioSystem;
    private final com.rayvion.engine.inventory.InventorySystem inventorySystem;
    private final long managedWorldId;

    private final Map<Long, Long> leverToGate = new ConcurrentHashMap<>();
    private final Map<Long, Integer> gateOpeningProgress = new ConcurrentHashMap<>(); // 0: closed, 1: partial 1, 2: partial 2, 3: open
    private final Map<Long, Boolean> leverStates = new ConcurrentHashMap<>(); // false: off, true: on

    private final double interactionRange = 48.0;

    public MechanismSystem(EntitySystem entitySystem,
                           TransformSystem transformSystem,
                           GraphicsSystem graphicsSystem,
                           PhysicsSystem physicsSystem,
                           AudioSystem audioSystem,
                           com.rayvion.engine.inventory.InventorySystem inventorySystem,
                           long worldId) {
        this.entitySystem = entitySystem;
        this.transformSystem = transformSystem;
        this.graphicsSystem = graphicsSystem;
        this.physicsSystem = physicsSystem;
        this.audioSystem = audioSystem;
        this.inventorySystem = inventorySystem;
        this.managedWorldId = worldId;
    }

    @Override
    public void init() {
    }

    public void registerLever(long leverId, long gateId) {
        leverToGate.put(leverId, gateId);
        leverStates.put(leverId, false);
        gateOpeningProgress.put(gateId, 0);
    }

    public void tryInteract(Entity interactor) {
        if (!transformSystem.hasTransform(interactor.id())) return;
        Transform interactorTransform = transformSystem.getTransform(interactor.id());

        long closestLeverId = -1;
        double minDistance = Double.MAX_VALUE;

        for (Long leverId : leverToGate.keySet()) {
            if (!transformSystem.hasTransform(leverId)) continue;
            if (leverStates.getOrDefault(leverId, false)) continue; // Already pulled

            Transform leverTransform = transformSystem.getTransform(leverId);
            double dist = calculateDistance(interactorTransform, leverTransform);

            if (dist < interactionRange && dist < minDistance) {
                minDistance = dist;
                closestLeverId = leverId;
            }
        }

        if (closestLeverId != -1) {
            pullLever(closestLeverId);
        }
    }

    private void pullLever(long leverId) {
        leverStates.put(leverId, true);
        graphicsSystem.setEntityGraphics(leverId, new TextureGraphics("lever_on"));
        
        if (audioSystem != null) {
            audioSystem.playSound("pickup"); // Fallback sound for now
        }

        long gateId = leverToGate.get(leverId);
        if (gateId != -1) {
            gateOpeningProgress.put(gateId, 1);
            graphicsSystem.setEntityGraphics(gateId, new TextureGraphics("gate_opening_1"));
            log.info("Lever {} pulled, opening gate {}", leverId, gateId);
        }
    }

    @Override
    public void tick() {
        for (Map.Entry<Long, Integer> entry : gateOpeningProgress.entrySet()) {
            long gateId = entry.getKey();
            int progress = entry.getValue();

            if (progress > 0 && progress < 3) {
                int newProgress = progress + 1;
                gateOpeningProgress.put(gateId, newProgress);

                String textureId = "gate_opening_" + progress; // Current texture is already set, advance to next
                if (newProgress == 2) {
                    graphicsSystem.setEntityGraphics(gateId, new TextureGraphics("gate_opening_2"));
                } else if (newProgress == 3) {
                    graphicsSystem.setEntityGraphics(gateId, new TextureGraphics("gate_open"));
                    // Fully open: remove physics body
                    boolean removed = physicsSystem.removeBody(managedWorldId, gateId);
                    log.info("Gate {} is now fully open. Physics body removed: {}", gateId, removed);
                }
            }
        }

        // Handle interaction prompts for interactor entities
        Collection<Entity> interactors = inventorySystem.getEntitiesWithInventory();
        java.util.Set<Long> leversWithPrompts = new java.util.HashSet<>();

        for (Entity interactor : interactors) {
            if (transformSystem.hasTransform(interactor.id())) {
                Transform interactorTransform = transformSystem.getTransform(interactor.id());
                for (Long leverId : leverToGate.keySet()) {
                    if (leverStates.getOrDefault(leverId, false)) {
                        continue;
                    }

                    if (transformSystem.hasTransform(leverId)) {
                        Transform leverTransform = transformSystem.getTransform(leverId);
                        double dist = calculateDistance(interactorTransform, leverTransform);
                        if (dist < interactionRange) {
                            leversWithPrompts.add(leverId);
                            graphicsSystem.setInteractionPrompt(leverId, "[E] Pull Lever");
                        }
                    }
                }
            }
        }

        // Clear prompts for levers that are no longer in range of any interactor
        for (Long leverId : leverToGate.keySet()) {
            if (!leversWithPrompts.contains(leverId)) {
                graphicsSystem.removeInteractionPrompt(leverId);
            }
        }
    }

    private double calculateDistance(Transform t1, Transform t2) {
        double dx = t1.getX() - t2.getX();
        double dy = t1.getY() - t2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    @Override
    public Duration getTickDelay() {
        return Duration.ofMillis(200); // 5 frames per second for animation
    }
}
