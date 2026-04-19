package eth.likespro.cyberpunkeb.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import eth.likespro.cyberpunkeb.MainGame;
import eth.likespro.cyberpunkeb.core.*;
import eth.likespro.cyberpunkeb.core.system.CameraSystem;
import eth.likespro.cyberpunkeb.core.system.PlayerInputSystem;
import eth.likespro.cyberpunkeb.entity.EntityFactory;
import eth.likespro.cyberpunkeb.gui.menu.MainMenu;
import eth.likespro.cyberpunkeb.physics.Dyn4jPhysicsAdapter;
import eth.likespro.cyberpunkeb.renderer.LibgdxRendererAdapter;

public class GameScreen implements BaseScreen {
    private final MainGame game;
    private final World world;
    private final Renderer renderer;

    public GameScreen(final MainGame game) {
        this.game = game;
        
        renderer = new LibgdxRendererAdapter();
        world = new World(new Dyn4jPhysicsAdapter(), renderer);

        world.init();
        world.setBackground("bg.png");
        
        // Register ECS Systems
        world.addSystem(new PlayerInputSystem());
        world.addSystem(new CameraSystem());

        world.addEntity(EntityFactory.createPlayer(400, 300));
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenu(game));
            dispose();
            return;
        }
        
        InputState input = world.getInputState();
        input.left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
        input.right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        input.up = Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP);
        input.down = Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN);

        world.update(delta);

        world.render("Use WASD/Arrows to move, ESC to return.");
    }

    @Override
    public void resize(int width, int height) {
        renderer.resize(width, height);
    }

    @Override
    public void dispose() {
        world.dispose();
    }
}
