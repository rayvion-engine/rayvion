package eth.likespro.cyberpunkeb.core.system;

import eth.likespro.cyberpunkeb.core.Entity;
import eth.likespro.cyberpunkeb.core.PhysicsEngine;
import eth.likespro.cyberpunkeb.core.World;
import eth.likespro.cyberpunkeb.core.component.PlayerControlComponent;
import eth.likespro.cyberpunkeb.core.component.TransformComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CameraSystemTest {

    private CameraSystem cameraSystem;
    private World world;

    private final PhysicsEngine physicsEngine = new PhysicsEngine() {
        @Override
        public void init() {

        }

        @Override
        public void update(float delta) {

        }

        @Override
        public void addEntity(Entity entity) {

        }

        @Override
        public void removeEntity(Entity entity) {

        }

        @Override
        public void applyForce(Entity entity, float fx, float fy) {

        }

        @Override
        public void setVelocity(Entity entity, float vx, float vy) {

        }
    };

    @BeforeEach
    void setUp() {
        cameraSystem = new CameraSystem();
        world = new World(physicsEngine, null);


        Entity npc = new Entity("npc");
        npc.addComponent(new TransformComponent(-100, -100));
        world.addEntity(npc);

        Entity abstractEntity = new Entity("abstract_entity");
        world.addEntity(abstractEntity);

        // Update the world to add pending entities
        // It does not affect the camera system as we have not registered it in the world
        world.update(0.016f);
    }

    @Test
    void mustNotMoveCameraIfNoPlayerExist() {
        cameraSystem.update(0.016f, world);

        assertEquals(0, world.getScene().getCamera().x, 0.01);
    }

    @Test
    void movesCameraToPlayer() {
        Entity player = new Entity("player");
        player.addComponent(new TransformComponent(100, 100));
        player.addComponent(new PlayerControlComponent(300f));
        world.addEntity(player);

        // Update the world to add pending entities
        // It does not affect the camera system as we have not registered it in the world
        world.update(0.016f);

        cameraSystem.update(0.016f, world);

        assertEquals(8, world.getScene().getCamera().x, 0.01);
    }

    @AfterEach
    void tearDown() {
    }
}