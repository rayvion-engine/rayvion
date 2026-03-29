package eth.likespro.cyberpunkeb.core.system;

import eth.likespro.cyberpunkeb.core.Camera;
import eth.likespro.cyberpunkeb.core.World;
import eth.likespro.cyberpunkeb.core.component.PlayerControlComponent;
import eth.likespro.cyberpunkeb.core.component.TransformComponent;

public class CameraSystem implements System {
    @Override
    public void update(float delta, World world) {
        Camera camera = world.getScene().getCamera();

        world.getEntities().stream()
            .filter(entity -> entity.hasComponent(PlayerControlComponent.class) && entity.hasComponent(TransformComponent.class))
            .findFirst()
            .ifPresent(entity -> {
                TransformComponent transform = entity.getComponent(TransformComponent.class);

                // Smooth player following
                float lerp = 5.0f * delta;
                camera.x += (transform.x - camera.x) * lerp;
                camera.y += (transform.y - camera.y) * lerp;
            });
    }
}
