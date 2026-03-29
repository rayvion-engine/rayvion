package eth.likespro.cyberpunkeb.core.system;

import eth.likespro.cyberpunkeb.core.Entity;
import eth.likespro.cyberpunkeb.core.World;
import eth.likespro.cyberpunkeb.core.InputState;
import eth.likespro.cyberpunkeb.core.component.PhysicsComponent;
import eth.likespro.cyberpunkeb.core.component.PlayerControlComponent;

public class PlayerInputSystem implements System {
    @Override
    public void update(float delta, World world) {
        InputState inputState = world.getInputState();
        for (Entity entity : world.getEntities()) {
            if (entity.hasComponent(PlayerControlComponent.class) && entity.hasComponent(PhysicsComponent.class)) {
                PlayerControlComponent control = entity.getComponent(PlayerControlComponent.class);
                PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);

                float xVelocity = 0;
                float yVelocity = 0;
                if (inputState.left) xVelocity -= control.moveSpeed;
                if (inputState.right) xVelocity += control.moveSpeed;
                if (inputState.up) yVelocity += control.moveSpeed;
                if (inputState.down) yVelocity -= control.moveSpeed;

                physics.velocityX = xVelocity;
                physics.velocityY = yVelocity;
                
                // Directly command physics engine for now
                world.getPhysicsEngine().setVelocity(entity, xVelocity, yVelocity);
            }
        }
    }
}
