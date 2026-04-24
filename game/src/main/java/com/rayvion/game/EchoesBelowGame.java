package com.rayvion.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.entity.EntitySystem;
import com.rayvion.engine.entity.EntitySystemImpl;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.event.impl.DefaultEventManager;
import com.rayvion.engine.graphics.GraphicsSystem;
import com.rayvion.engine.graphics.TextureGraphics;
import com.rayvion.engine.graphics.TiledWorldGraphics;
import com.rayvion.engine.graphics.impl.GraphicsSystemImpl;
import com.rayvion.engine.scheduler.SchedulerSystem;
import com.rayvion.engine.scheduler.impl.DefaultSchedulerSystem;
import com.rayvion.engine.system.manager.SystemManager;
import com.rayvion.engine.system.tick.TickSystem;
import com.rayvion.engine.system.tick.impl.DefaultTickSystem;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import com.rayvion.engine.transform.impl.TransformSystemImpl;
import com.rayvion.engine.world.WorldSystem;
import com.rayvion.engine.world.impl.WorldSystemImpl;
import com.rayvion.game.input.LibGdxInputSystem;
import com.rayvion.game.render.LibGdxRenderingSystem;

public class EchoesBelowGame extends ApplicationAdapter {
    private SystemManager systemManager;
    private LibGdxRenderingSystem renderingSystem;

    // Engine Systems
    private TickSystem tickSystem;
    private GraphicsSystem graphicsSystem;
    private TransformSystem transformSystem;
    private WorldSystem worldSystem;
    private EntitySystem entitySystem;

    @Override
    public void create() {
        systemManager = new SystemManager();

        // 1. Initialize core systems
        EventManager eventManager = new DefaultEventManager();
        SchedulerSystem schedulerSystem = new DefaultSchedulerSystem();
        tickSystem = new DefaultTickSystem();
        transformSystem = new TransformSystemImpl();
        graphicsSystem = new GraphicsSystemImpl();
        entitySystem = new EntitySystemImpl();
        worldSystem = new WorldSystemImpl();

        // 2. Add them to SystemManager
        systemManager.addSystem(schedulerSystem);
        systemManager.addSystem(tickSystem);
        systemManager.addSystem(transformSystem);
        systemManager.addSystem(graphicsSystem);
        systemManager.addSystem(entitySystem);
        systemManager.addSystem(worldSystem);

        // 3. Initialize input system
        LibGdxInputSystem inputSystem = new LibGdxInputSystem(eventManager);
        systemManager.addSystem(inputSystem);

        // 4. Initialize rendering system
        renderingSystem = new LibGdxRenderingSystem(graphicsSystem, transformSystem);
        renderingSystem.init();

        // 5. Setup a basic game state
        setupBasicGame();
    }

    private void setupBasicGame() {
        // Create a basic 10x10 tiled world with walls
        int width = 20;
        int height = 15;
        String[][] tiles = new String[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                    tiles[y][x] = "wall";
                } else {
                    tiles[y][x] = "floor";
                }
            }
        }
        // Add some random walls
        tiles[5][5] = "wall";
        tiles[5][6] = "wall";
        tiles[8][10] = "wall";
        tiles[9][10] = "wall";

        TiledWorldGraphics worldGraphics = new TiledWorldGraphics(width, height, 32.0, tiles);
        graphicsSystem.setWorldGraphics(worldGraphics);

        // Add a world
        worldSystem.addWorld(new com.rayvion.engine.world.World() {
            @Override public long getId() { return 0; }
        });

        // Spawn a placeholder entity
        Entity player = entitySystem.createEntity();
        worldSystem.addEntityToWorld(0, player.id());

        Transform t = new Transform();
        t.setX(160);
        t.setY(160);
        t.setZ(0);
        transformSystem.setTransform(player.id(), t);

        graphicsSystem.setEntityGraphics(player.id(), new TextureGraphics("player"));
    }

    @Override
    public void render() {
        // Clear screen
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render the scene
        renderingSystem.render();
    }

    @Override
    public void dispose() {
        renderingSystem.dispose();
    }
}
