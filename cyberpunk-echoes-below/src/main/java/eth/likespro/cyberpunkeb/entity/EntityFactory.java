package eth.likespro.cyberpunkeb.entity;

import eth.likespro.cyberpunkeb.core.Entity;
import eth.likespro.cyberpunkeb.core.component.*;

public class EntityFactory {
    private static int idCounter = 0;

    public static Entity createPlayer(float x, float y) {
        Entity player = new Entity("player_" + idCounter++);

        player.addComponent(new TransformComponent(x, y));
        player.addComponent(new BoundsComponent(32, 32));
        player.addComponent(new PhysicsComponent(false));
        player.addComponent(new RenderComponent("player_character.png"));
        player.addComponent(new PlayerControlComponent(300f));

        return player;
    }
}
