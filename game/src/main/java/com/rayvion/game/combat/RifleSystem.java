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
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitRequirement;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import com.rayvion.engine.world.WorldSystem;
import com.rayvion.engine.physics.PhysicsSystem;
import com.rayvion.engine.physics.PhysicsBody;
import com.rayvion.engine.entity.EntitySystem;
import com.rayvion.engine.audio.AudioSystem;
import com.rayvion.engine.graphics.AnimationGraphics;
import com.rayvion.engine.graphics.CameraSystem;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;

@Slf4j
public class RifleSystem implements System, Tickable {

    private final SystemDescriptor descriptor;
    private final long playerEntityId;
    private final long worldId;

    private EquipmentSystem equipmentSystem;
    private WorldSystem worldSystem;
    private TransformSystem transformSystem;
    private CharacteristicSystem characteristicSystem;
    private GraphicsSystem graphicsSystem;
    private EventManager eventManager;
    private PhysicsSystem physicsSystem;
    private EntitySystem entitySystem;
    private AudioSystem audioSystem;
    private CameraSystem cameraSystem;

    private long lastAttackTime = 0;
    private final long cooldownMs = 200; // Faster cooldown for rifle
    
    // Tracks current temporary animations for any entity (player firing, enemies hit)
    private final Map<Long, Long> animationEndTimes = new ConcurrentHashMap<>();

    private static class VisualEffect {
        long entityId;
        long endTime;
        double vx, vy;
        double rotationSpeed;

        VisualEffect(long entityId, long endTime) {
            this(entityId, endTime, 0, 0, 0);
        }

        VisualEffect(long entityId, long endTime, double vx, double vy, double rotationSpeed) {
            this.entityId = entityId;
            this.endTime = endTime;
            this.vx = vx;
            this.vy = vy;
            this.rotationSpeed = rotationSpeed;
        }
    }

    private final List<VisualEffect> activeEffects = new ArrayList<>();

    private static class Bullet {
        long entityId;
        double x, y;
        double dirX, dirY;
        double speed;
        double distanceTraveled;
        double maxRange;
        double damage;

        Bullet(long entityId, double x, double y, double dirX, double dirY, double speed, double maxRange, double damage) {
            this.entityId = entityId;
            this.x = x;
            this.y = y;
            this.dirX = dirX;
            this.dirY = dirY;
            this.speed = speed;
            this.maxRange = maxRange;
            this.damage = damage;
            this.distanceTraveled = 0;
        }
    }

    private final List<Bullet> activeBullets = new ArrayList<>();

    public RifleSystem(long worldId, long playerEntityId) {
        this.worldId = worldId;
        this.playerEntityId = playerEntityId;
        this.descriptor = new SystemDescriptor(
                new SystemCoordinate("com.rayvion.game", "rifle-combat", Version.parse("1.0.0")),
                Set.of(
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "equipment", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "world", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "transform", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "characteristic", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "graphics", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "event", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "physics", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "entity", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "audio", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "camera", v -> true), SystemDependency.RequirementLevel.REQUIRED)
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
        else if (dependency instanceof PhysicsSystem ps) physicsSystem = ps;
        else if (dependency instanceof EntitySystem es) entitySystem = es;
        else if (dependency instanceof AudioSystem as) audioSystem = as;
        else if (dependency instanceof CameraSystem cs) cameraSystem = cs;
    }

    @Override
    public void init() {
        if (eventManager != null) {
            eventManager.subscribe(BindingEvent.class, this::handleBindingEvent);
            eventManager.subscribe(ItemInteractEvent.class, this::handleItemInteract);
        }
    }

    private void handleItemInteract(ItemInteractEvent event) {
        if (isRifle(event.item())) {
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
        long currentTime = java.lang.System.currentTimeMillis();
        if (currentTime - lastAttackTime < cooldownMs) {
            return; // Cooldown active
        }

        Optional<InventoryItem> equippedOpt = equipmentSystem.getEquippedItem(playerEntityId);
        if (equippedOpt.isEmpty() || !isRifle(equippedOpt.get())) {
            return; // No rifle equipped
        }

        lastAttackTime = currentTime;
        log.info("RifleSystem: Player {} is shooting with {}", playerEntityId, equippedOpt.get().name());

        if (audioSystem != null) {
            audioSystem.playSound("rifle_shoot");
        }

        // Trigger shooting animation state via characteristic system
        characteristicSystem.setValue(new Entity(playerEntityId), "animation_state", "shoot");
        animationEndTimes.put(playerEntityId, currentTime + 150); // 150ms animation duration

        if (cameraSystem != null) {
            cameraSystem.shake(2.0, 100);
        }

        Transform playerTransform = transformSystem.getTransform(playerEntityId);
        if (playerTransform == null) return;

        Entity playerEntity = new Entity(playerEntityId);
        Double facingAngleOpt = characteristicSystem.getValue(playerEntity, "facing_angle");
        double facingAngle = facingAngleOpt != null ? facingAngleOpt : 0.0;
        
        Double strengthOpt = characteristicSystem.getValue(playerEntity, "strength");
        double strength = strengthOpt != null ? strengthOpt : 10.0;

        double radAngle = Math.toRadians(facingAngle);
        
        // Apply recoil
        PhysicsBody playerBody = physicsSystem.getBody(worldId, playerEntityId);
        if (playerBody != null) {
            double recoilForce = -1500.0; // Pushing back
            playerBody.applyImpulse(Math.cos(radAngle) * recoilForce, Math.sin(radAngle) * recoilForce);
        }

        double dirX = Math.cos(radAngle);
        double dirY = Math.sin(radAngle);
        
        // Spawn bullet slightly ahead of the player (radius 16 + a little extra) to avoid immediate wall collisions
        double spawnOffset = 20.0;
        double spawnX = playerTransform.getX() + dirX * spawnOffset;
        double spawnY = playerTransform.getY() + dirY * spawnOffset;
        
        // Spawn bullet entity
        Entity bulletEntity = entitySystem.createEntity();
        worldSystem.addEntityToWorld(worldId, bulletEntity.id());
        
        Transform bulletTransform = new Transform();
        bulletTransform.setX(spawnX);
        bulletTransform.setY(spawnY);
        bulletTransform.setRotationZ(radAngle); // Set rotation to match firing angle
        transformSystem.setTransform(bulletEntity.id(), bulletTransform);
        
        graphicsSystem.setEntityGraphics(bulletEntity.id(), new TextureGraphics("bullet")); 
        
        // Set visual dimensions
        characteristicSystem.setValue(bulletEntity, "width", 48.0);
        characteristicSystem.setValue(bulletEntity, "height", 24.0);
        
        synchronized (activeBullets) {
            activeBullets.add(new Bullet(bulletEntity.id(), spawnX, spawnY, dirX, dirY, 400.0, 600.0, strength));
        }

        // Spawn muzzle flash
        Entity flash = entitySystem.createEntity();
        worldSystem.addEntityToWorld(worldId, flash.id());
        Transform flashT = new Transform();
        flashT.setX(spawnX);
        flashT.setY(spawnY);
        flashT.setRotationZ(radAngle);
        transformSystem.setTransform(flash.id(), flashT);
        graphicsSystem.setEntityGraphics(flash.id(), new AnimationGraphics(List.of("muzzle_flash_1", "muzzle_flash_2"), 0.05, false));
        characteristicSystem.setValue(flash, "width", 32.0);
        characteristicSystem.setValue(flash, "height", 32.0);
        synchronized (activeEffects) {
            activeEffects.add(new VisualEffect(flash.id(), currentTime + 100));
        }

        // Spawn shell casing
        Entity shell = entitySystem.createEntity();
        worldSystem.addEntityToWorld(worldId, shell.id());
        Transform shellT = new Transform();
        // Eject to the side
        double sideAngle = radAngle - Math.PI / 2.0;
        double shellX = playerTransform.getX() + Math.cos(sideAngle) * 10.0;
        double shellY = playerTransform.getY() + Math.sin(sideAngle) * 10.0;
        shellT.setX(shellX);
        shellT.setY(shellY);
        shellT.setRotationZ(randomAngle());
        transformSystem.setTransform(shell.id(), shellT);
        graphicsSystem.setEntityGraphics(shell.id(), new TextureGraphics("shell_casing"));
        characteristicSystem.setValue(shell, "width", 12.0);
        characteristicSystem.setValue(shell, "height", 12.0);
        
        double shellSpeed = 50.0 + Math.random() * 50.0;
        double svx = Math.cos(sideAngle) * shellSpeed;
        double svy = Math.sin(sideAngle) * shellSpeed;
        synchronized (activeEffects) {
            activeEffects.add(new VisualEffect(shell.id(), currentTime + 500, svx, svy, Math.random() * 10.0 - 5.0));
        }
        
        log.info("RifleSystem: Spawned bullet entity {} at ({}, {}) with rotation {}", bulletEntity.id(), spawnX, spawnY, radAngle);
    }
    
    private double randomAngle() {
        return Math.random() * Math.PI * 2.0;
    }
    

    private boolean isRifle(InventoryItem item) {
        String id = item.id().toLowerCase();
        String type = item.type().toLowerCase();
        return "rifle".equals(type) || "riffle".equals(type) || id.contains("rifle") || id.contains("riffle");
    }

    @Override
    public void tick() {
        long currentTime = java.lang.System.currentTimeMillis();
        
        // 1. Update temporary animations via AnimationStateSystem
        Iterator<Map.Entry<Long, Long>> animIt = animationEndTimes.entrySet().iterator();
        while (animIt.hasNext()) {
            Map.Entry<Long, Long> entry = animIt.next();
            if (currentTime >= entry.getValue()) {
                long entityId = entry.getKey();
                Entity entity = new Entity(entityId);
                
                if (characteristicSystem.hasCharacteristic(entity, "animation_state")) {
                    characteristicSystem.setValue(entity, "animation_state", "idle");
                }
                animIt.remove();
            }
        }
        
        // 1b. Update visual effects
        double deltaTime = getTickDelay().toMillis() / 1000.0;
        synchronized (activeEffects) {
            Iterator<VisualEffect> effectIt = activeEffects.iterator();
            while (effectIt.hasNext()) {
                VisualEffect effect = effectIt.next();
                if (currentTime >= effect.endTime) {
                    destroyEffectEntity(effect.entityId);
                    effectIt.remove();
                } else {
                    // Update position and rotation for moving effects (shells)
                    Transform t = transformSystem.getTransform(effect.entityId);
                    if (t != null) {
                        t.setX(t.getX() + effect.vx * deltaTime);
                        t.setY(t.getY() + effect.vy * deltaTime);
                        t.setRotationZ(t.getRotationZ() + effect.rotationSpeed * deltaTime);
                        transformSystem.setTransform(effect.entityId, t);
                        
                        // Add some friction to shells
                        effect.vx *= 0.9;
                        effect.vy *= 0.9;
                    }
                }
            }
        }
        
        // 2. Update bullets safely
        Collection<Long> entities = worldSystem.getEntities(worldId);
        
        synchronized (activeBullets) {
            if (!activeBullets.isEmpty()) {
                log.trace("RifleSystem: Updating {} active bullets", activeBullets.size());
            }

            Iterator<Bullet> bulletIt = activeBullets.iterator();
            while (bulletIt.hasNext()) {
                Bullet bullet = bulletIt.next();
                double distanceThisTick = bullet.speed * deltaTime;
                double stepSize = 8.0; // Smaller steps for accurate collision detection
                double distanceCoveredThisTick = 0;
                boolean bulletDestroyed = false;

                while (distanceCoveredThisTick < distanceThisTick) {
                    double move = Math.min(stepSize, distanceThisTick - distanceCoveredThisTick);
                    bullet.x += bullet.dirX * move;
                    bullet.y += bullet.dirY * move;
                    bullet.distanceTraveled += move;
                    distanceCoveredThisTick += move;

                    // Check wall collision
                    if (physicsSystem.isPointBlocked(worldId, bullet.x, bullet.y)) {
                        log.info("RifleSystem: Bullet {} hit a wall.", bullet.entityId);
                        bulletDestroyed = true;
                        break;
                    }

                    // Check entity collision
                    for (Long entityId : entities) {
                        if (entityId == playerEntityId || entityId == bullet.entityId) continue;

                        Transform enemyTransform = transformSystem.getTransform(entityId);
                        if (enemyTransform == null) continue;

                        // Skip ground items
                        Double isGroundItem = characteristicSystem.getValue(new Entity(entityId), "is_ground_item");
                        if (isGroundItem != null && isGroundItem > 0.5) continue;

                        double dx = enemyTransform.getX() - bullet.x;
                        double dy = enemyTransform.getY() - bullet.y;
                        double distSq = dx * dx + dy * dy;

                        if (distSq < 16.0 * 16.0) {
                            Entity enemy = new Entity(entityId);
                            Double healthOpt = characteristicSystem.getValue(enemy, "health");
                            if (healthOpt != null && healthOpt > 0) {
                                double defenseOpt = characteristicSystem.getValue(enemy, "defense") != null ? characteristicSystem.getValue(enemy, "defense") : 0.0;
                                double damage = Math.max(0, bullet.damage - defenseOpt);
                                double newHealth = healthOpt - damage;

                                characteristicSystem.setValue(enemy, "health", newHealth);
                                log.info("RifleSystem: Bullet hit enemy {} for {} damage!", entityId, damage);

                                // For hit feedback, we still use manual override for now as it's a global effect
                                graphicsSystem.setEntityGraphics(entityId, new TextureGraphics("enemy_hit"));
                                animationEndTimes.put(entityId, currentTime + 200);
                                bulletDestroyed = true;
                                break;
                            }
                        }
                    }

                    if (bulletDestroyed) break;

                    if (bullet.distanceTraveled >= bullet.maxRange) {
                        log.info("RifleSystem: Bullet {} reached max range.", bullet.entityId);
                        bulletDestroyed = true;
                        break;
                    }
                }

                if (bulletDestroyed) {
                    destroyBulletEntity(bullet.entityId);
                    bulletIt.remove();
                } else {
                    // Update transform for rendering
                    Transform t = transformSystem.getTransform(bullet.entityId);
                    if (t != null) {
                        t.setX(bullet.x);
                        t.setY(bullet.y);
                        transformSystem.setTransform(bullet.entityId, t);
                    }
                }
            }
        }
    }

    private void destroyBulletEntity(long entityId) {
        worldSystem.removeEntityFromWorld(worldId, entityId);
        transformSystem.removeTransform(entityId);
        graphicsSystem.removeEntityGraphics(entityId);
        entitySystem.removeEntity(entityId);
    }

    private void destroyEffectEntity(long entityId) {
        worldSystem.removeEntityFromWorld(worldId, entityId);
        transformSystem.removeTransform(entityId);
        graphicsSystem.removeEntityGraphics(entityId);
        entitySystem.removeEntity(entityId);
    }

}
