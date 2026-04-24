package com.rayvion.game.ai;

import com.rayvion.engine.ai.AiStrategy;
import com.rayvion.engine.physics.PhysicsBody;
import com.rayvion.engine.physics.PhysicsSystem;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.entity.Entity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple AI strategy that chases a target entity.
 */
@Slf4j
@RequiredArgsConstructor
public class ChasingAiStrategy implements AiStrategy {
    private final long targetEntityId;
    private final long worldId;
    private final TransformSystem transformSystem;
    private final PhysicsSystem physicsSystem;
    private final CharacteristicSystem characteristicSystem;
    private final double detectionRange;

    public ChasingAiStrategy(long targetEntityId, long worldId, TransformSystem transformSystem, PhysicsSystem physicsSystem,
                             CharacteristicSystem characteristicSystem) {
        this(targetEntityId, worldId, transformSystem, physicsSystem, characteristicSystem, AiVisibility.DEFAULT_DETECTION_RANGE);
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
            if (!AiVisibility.canDetectTarget(dx, dy, detectionRange)) {
                body.setVelocity(0, 0);
                return;
            }
            if (distance > 5) { // deadzone to prevent jitter
                double speed = characteristicSystem.getValue(new Entity(entityId), "speed");
                double vx = (dx / distance) * speed;
                double vy = (dy / distance) * speed;
                log.trace("AI Strategy: Setting velocity for entity {} to {}, {}", entityId, vx, vy);
                body.setVelocity(vx, vy);
                body.setRotation(Math.atan2(vy, vx));
            } else {
                body.setVelocity(0, 0);
            }
        }
    }
}
