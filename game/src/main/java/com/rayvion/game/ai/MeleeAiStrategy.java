package com.rayvion.game.ai;

import com.rayvion.engine.ai.AiStrategy;
import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.physics.PhysicsBody;
import com.rayvion.engine.physics.PhysicsSystem;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import com.rayvion.game.combat.EntityAttackEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * An AI strategy that chases a target entity and attacks with a melee weapon when close.
 */
@Slf4j
public class MeleeAiStrategy implements AiStrategy {
    private static final double DEFAULT_TILE_SIZE = 32.0;
    private static final int DEFAULT_GRID_WIDTH = 40;
    private static final int DEFAULT_GRID_HEIGHT = 30;
    private static final double ATTACK_RANGE = 50.0;

    private final long targetEntityId;
    private final long worldId;
    private final TransformSystem transformSystem;
    private final PhysicsSystem physicsSystem;
    private final CharacteristicSystem characteristicSystem;
    private final EventManager eventManager;
    private final PathfindingAiStrategy pathfindingStrategy;
    private final double detectionRange;

    public MeleeAiStrategy(long targetEntityId, long worldId, TransformSystem transformSystem, PhysicsSystem physicsSystem,
                           CharacteristicSystem characteristicSystem, EventManager eventManager) {
        this(targetEntityId, worldId, transformSystem, physicsSystem, characteristicSystem, eventManager,
            DEFAULT_TILE_SIZE, DEFAULT_GRID_WIDTH, DEFAULT_GRID_HEIGHT, AiVisibility.DEFAULT_DETECTION_RANGE);
    }

    public MeleeAiStrategy(long targetEntityId, long worldId, TransformSystem transformSystem, PhysicsSystem physicsSystem,
                           CharacteristicSystem characteristicSystem, EventManager eventManager,
                           double tileSize, int gridWidth, int gridHeight) {
        this(targetEntityId, worldId, transformSystem, physicsSystem, characteristicSystem, eventManager,
            tileSize, gridWidth, gridHeight, AiVisibility.DEFAULT_DETECTION_RANGE);
    }

    public MeleeAiStrategy(long targetEntityId, long worldId, TransformSystem transformSystem, PhysicsSystem physicsSystem,
                           CharacteristicSystem characteristicSystem, EventManager eventManager,
                           double tileSize, int gridWidth, int gridHeight, double detectionRange) {
        this.targetEntityId = targetEntityId;
        this.worldId = worldId;
        this.transformSystem = transformSystem;
        this.physicsSystem = physicsSystem;
        this.characteristicSystem = characteristicSystem;
        this.eventManager = eventManager;
        this.detectionRange = detectionRange;
        this.pathfindingStrategy = new PathfindingAiStrategy(
            targetEntityId, worldId, transformSystem, physicsSystem, characteristicSystem, tileSize, gridWidth, gridHeight, detectionRange
        );
    }

    @Override
    public void update(long entityId) {
        if (!transformSystem.hasTransform(entityId) || !transformSystem.hasTransform(targetEntityId)) {
            return;
        }

        Transform enemyTransform = transformSystem.getTransform(entityId);
        Transform targetTransform = transformSystem.getTransform(targetEntityId);

        double dx = targetTransform.getX() - enemyTransform.getX();
        double dy = targetTransform.getY() - enemyTransform.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        PhysicsBody body = physicsSystem.getBody(worldId, entityId);
        if (body != null) {
            double angleRadians = Math.atan2(dy, dx);
            double angleDegrees = Math.toDegrees(angleRadians);
            if (angleDegrees < 0) angleDegrees += 360;
            Entity enemy = new Entity(entityId);

            if (!AiVisibility.canDetectTarget(dx, dy, detectionRange)) {
                body.setVelocity(0, 0);
                return;
            }

            if (distance > ATTACK_RANGE) {
                pathfindingStrategy.update(entityId);
                characteristicSystem.setValue(enemy, "facing_angle", angleDegrees);
            } else {
                body.setVelocity(0, 0);
                body.setRotation(angleRadians);
                characteristicSystem.setValue(enemy, "facing_angle", angleDegrees);
                
                // Fire attack event
                eventManager.publish(new EntityAttackEvent(entityId));
            }
        }
    }
}
