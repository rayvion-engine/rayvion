package eth.likespro.cyberpunkeb.entity;

import eth.likespro.cyberpunkeb.core.Entity;
import eth.likespro.cyberpunkeb.core.component.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityFactoryTest {
    @Test
    void playerShouldHaveTransformComponent() {
        Entity player = EntityFactory.createPlayer(0, 0);
        assertTrue(player.hasComponent(TransformComponent.class));
    }

    @Test
    void playerShouldHaveBoundsComponent() {
        Entity player = EntityFactory.createPlayer(0, 0);
        assertTrue(player.hasComponent(BoundsComponent.class));
    }

    @Test
    void playerShouldHavePhysicsComponent() {
        Entity player = EntityFactory.createPlayer(0, 0);
        assertTrue(player.hasComponent(PhysicsComponent.class));
    }

    @Test
    void playerShouldHaveRenderComponent() {
        Entity player = EntityFactory.createPlayer(0, 0);
        assertTrue(player.hasComponent(RenderComponent.class));
    }

    @Test
    void playerShouldHavePlayerControlComponent() {
        Entity player = EntityFactory.createPlayer(0, 0);
        assertTrue(player.hasComponent(PlayerControlComponent.class));
    }
}