 package com.rayvion.game.combat;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.bindings.BindingEvent;
import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.equipment.EquipmentSystem;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.graphics.GraphicsSystem;
import com.rayvion.engine.graphics.TextureGraphics;
import com.rayvion.engine.graphics.EntityGraphics;
import com.rayvion.engine.input.KeyEvent;
import com.rayvion.engine.inventory.InventoryItem;
import com.rayvion.engine.inventory.ItemInteractEvent;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.Tickable;
import com.rayvion.engine.system.dependency.SystemDependency;
import com.rayvion.engine.graphics.AnimationGraphics;
import com.rayvion.engine.entity.EntitySystem;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitRequirement;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import com.rayvion.engine.world.WorldSystem;
import com.rayvion.engine.audio.AudioSystem;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.Map;


import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SwordSystem implements System, Tickable {

    private final SystemDescriptor descriptor;
    private final long playerEntityId;
    private final long worldId;

    private EquipmentSystem equipmentSystem;
    private WorldSystem worldSystem;
    private TransformSystem transformSystem;
    private CharacteristicSystem characteristicSystem;
    private GraphicsSystem graphicsSystem;
    private EventManager eventManager;
    private AudioSystem audioSystem;

    private final Map<Long, Long> lastAttackTimes = new ConcurrentHashMap<>();
    private final long cooldownMs = 500;
    private EntitySystem entitySystem;

    private final Map<Long, Long> animationEndTimes = new ConcurrentHashMap<>();

    public SwordSystem(long worldId, long playerEntityId) {
        this.worldId = worldId;
        this.playerEntityId = playerEntityId;
        this.descriptor = new SystemDescriptor(
                new SystemCoordinate("com.rayvion.game", "sword-combat", Version.parse("1.0.0")),
                Set.of(
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "equipment", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "world", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "transform", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "characteristic", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "graphics", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "event", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "audio", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "entity", v -> true), SystemDependency.RequirementLevel.REQUIRED)
                ),
                Set.of(Tickable.TRAIT)
        );
    }

    @Override
    public SystemDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void onDependencyAdded(System dependency) {
        if (dependency instanceof EquipmentSystem es) equipmentSystem = es;
        else if (dependency instanceof WorldSystem ws) worldSystem = ws;
        else if (dependency instanceof TransformSystem ts) transformSystem = ts;
        else if (dependency instanceof CharacteristicSystem cs) characteristicSystem = cs;
        else if (dependency instanceof GraphicsSystem gs) graphicsSystem = gs;
        else if (dependency instanceof EventManager em) eventManager = em;
        else if (dependency instanceof AudioSystem as) audioSystem = as;
        else if (dependency instanceof EntitySystem es) entitySystem = es;
    }

    @Override
    public void init() {
        if (eventManager != null) {
            eventManager.registerEventType(EntityAttackEvent.class);
            eventManager.subscribe(BindingEvent.class, this::handleBindingEvent);
            eventManager.subscribe(ItemInteractEvent.class, this::handleItemInteract);
            eventManager.subscribe(EntityAttackEvent.class, this::handleEntityAttack);
        }
    }

    private void handleEntityAttack(EntityAttackEvent event) {
        performAttack(event.attackerId());
    }

    private void handleItemInteract(ItemInteractEvent event) {
        if (isSword(event.item())) {
            long entityId = event.entity().id();
            Optional<InventoryItem> currentlyEquipped = equipmentSystem.getEquippedItem(entityId);

            if (currentlyEquipped.isPresent() && currentlyEquipped.get().equals(event.item())) {
                equipmentSystem.unequip(entityId);
            } else {
                equipmentSystem.equip(entityId, event.item());
            }
        }
    }

    private void handleBindingEvent(BindingEvent event) {
        if (event.type() == KeyEvent.Type.KEY_DOWN && "Attack".equals(event.parameter().name())) {
            performAttack();
        }
    }

    private void performAttack() {
        performAttack(playerEntityId);
    }

    private void performAttack(long attackerId) {
        long currentTime = java.lang.System.currentTimeMillis();
        long lastAttackTime = lastAttackTimes.getOrDefault(attackerId, 0L);
        if (currentTime - lastAttackTime < cooldownMs) {
            return; // Cooldown active
        }

        Optional<InventoryItem> equippedOpt = equipmentSystem.getEquippedItem(attackerId);
        if (equippedOpt.isEmpty() || !isSword(equippedOpt.get())) {
            return; // No sword equipped
        }

        lastAttackTimes.put(attackerId, currentTime);
        log.info("SwordSystem: Entity {} is attacking with {}", attackerId, equippedOpt.get().name());

        if (audioSystem != null) {
            audioSystem.playSound("sword_swing");
        }

        // Trigger attack animation state via characteristic system
        characteristicSystem.setValue(new Entity(attackerId), "animation_state", "attack");
        animationEndTimes.put(attackerId, currentTime + 150); // 150ms animation duration

        // Spawn slash effect
        spawnSlashEffect(attackerId);

        // Detect enemies in front
        Transform attackerTransform = transformSystem.getTransform(attackerId);
        if (attackerTransform == null) return;

        Entity attackerEntity = new Entity(attackerId);
        Double facingAngleOpt = characteristicSystem.getValue(attackerEntity, "facing_angle");
        double facingAngle = facingAngleOpt != null ? facingAngleOpt : 0.0;
        Double strengthOpt = characteristicSystem.getValue(attackerEntity, "strength");
        double strength = strengthOpt != null ? strengthOpt : 10.0;

        Collection<Long> entities = worldSystem.getEntities(worldId);
        for (Long entityId : entities) {
            if (entityId == attackerId) continue;

            Transform enemyTransform = transformSystem.getTransform(entityId);
            if (enemyTransform == null) continue;

            // Skip ground items
            Double isGroundItem = characteristicSystem.getValue(new Entity(entityId), "is_ground_item");
            if (isGroundItem != null && isGroundItem > 0.5) continue;

            double dx = enemyTransform.getX() - attackerTransform.getX();
            double dy = enemyTransform.getY() - attackerTransform.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < 60.0) {
                // Calculate angle to enemy
                double angleToEnemy = Math.toDegrees(Math.atan2(dy, dx));
                if (angleToEnemy < 0) angleToEnemy += 360;

                // Normalize facing angle to 0-360
                double normFacing = facingAngle % 360;
                if (normFacing < 0) normFacing += 360;

                double angleDiff = Math.abs(normFacing - angleToEnemy);
                if (angleDiff > 180) angleDiff = 360 - angleDiff;

                // 45 degrees cone of attack
                if (angleDiff <= 45.0) {
                    Entity enemy = new Entity(entityId);
                    Double healthOpt = characteristicSystem.getValue(enemy, "health");
                    if (healthOpt != null && healthOpt > 0) {
                        double health = healthOpt;
                        Double defenseOpt = characteristicSystem.getValue(enemy, "defense");
                        double defense = defenseOpt != null ? defenseOpt : 0.0;
                        
                        double damage = Math.max(0, strength - defense);
                        health -= damage;
                        characteristicSystem.setValue(enemy, "health", health);
                        log.info("SwordSystem: Hit enemy {} for {} damage! Remaining health: {}", entityId, damage, health);
                    }
                }
            }
        }
    }

    private void spawnSlashEffect(long attackerId) {
        Transform attackerTransform = transformSystem.getTransform(attackerId);
        if (attackerTransform == null) return;

        Entity attackerEntity = new Entity(attackerId);
        Double facingAngleOpt = characteristicSystem.getValue(attackerEntity, "facing_angle");
        double facingAngle = facingAngleOpt != null ? facingAngleOpt : 0.0;
        double radAngle = Math.toRadians(facingAngle);

        // Position slash slightly in front of attacker
        double offset = 30.0;
        double slashX = attackerTransform.getX() + Math.cos(radAngle) * offset;
        double slashY = attackerTransform.getY() + Math.sin(radAngle) * offset;

        Entity slash = entitySystem.createEntity();
        worldSystem.addEntityToWorld(worldId, slash.id());

        Transform slashTransform = new Transform();
        slashTransform.setX(slashX);
        slashTransform.setY(slashY);
        slashTransform.setRotationZ(radAngle);
        transformSystem.setTransform(slash.id(), slashTransform);

        characteristicSystem.setValue(slash, "width", 64.0);
        characteristicSystem.setValue(slash, "height", 64.0);

        // Use the newly generated slash textures
        AnimationGraphics slashAnim = new AnimationGraphics(
            List.of("slash_1", "slash_2", "slash_3"),
            0.1,
            true
        );
        graphicsSystem.setEntityGraphics(slash.id(), slashAnim);

        // Schedule removal
        long removalTime = java.lang.System.currentTimeMillis() + 250;
        animationEndTimes.put(slash.id(), removalTime);
    }

    private boolean isSword(InventoryItem item) {
        return item.id().contains("sword");
    }

    @Override
    public void tick() {
        // Revert animation state and cleanup temporary effects
        long currentTime = java.lang.System.currentTimeMillis();
        Iterator<Map.Entry<Long, Long>> animIt = animationEndTimes.entrySet().iterator();
        while (animIt.hasNext()) {
            Map.Entry<Long, Long> entry = animIt.next();
            if (currentTime >= entry.getValue()) {
                long entityId = entry.getKey();
                
                Entity entity = new Entity(entityId);
                if (characteristicSystem.hasCharacteristic(entity, "animation_state")) {
                    // Revert to idle
                    characteristicSystem.setValue(entity, "animation_state", "idle");
                } else {
                    // It was likely a temporary effect entity like a slash
                    if (worldSystem.getEntities(worldId).contains(entityId)) {
                        worldSystem.removeEntityFromWorld(worldId, entityId);
                        transformSystem.removeTransform(entityId);
                        graphicsSystem.removeEntityGraphics(entityId);
                        entitySystem.removeEntity(entityId);
                    }
                }
                animIt.remove();
            }
        }
    }
    
    @Override
    public Duration getTickDelay() {
        return Duration.ofMillis(50); // Tick often enough to catch animation end quickly
    }
}
