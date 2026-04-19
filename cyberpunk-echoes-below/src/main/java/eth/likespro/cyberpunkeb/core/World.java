package eth.likespro.cyberpunkeb.core;

import java.util.ArrayList;
import java.util.List;

import eth.likespro.cyberpunkeb.core.system.System;
import lombok.Getter;

public class World {
    @Getter
    private final List<Entity> entities;
    @Getter
    private final PhysicsEngine physicsEngine;
    private final Renderer renderer;
    @Getter
    private final InputState inputState;
    private final List<System> systems;
    private final List<Entity> entitiesToAdd;
    private final List<Entity> entitiesToRemove;
    @Getter
    private final Scene scene;

    public World(PhysicsEngine physicsEngine, Renderer renderer) {
        this.physicsEngine = physicsEngine;
        this.renderer = renderer;
        this.entities = new ArrayList<>();
        this.inputState = new InputState();
        this.systems = new ArrayList<>();
        this.entitiesToAdd = new ArrayList<>();
        this.entitiesToRemove = new ArrayList<>();
        this.scene = new Scene();
    }

    public void addSystem(System system) {
        systems.add(system);
    }

    public void init() {
        physicsEngine.init();
        renderer.init();
    }

    public void setBackground(String backgroundId) {
        scene.setBackgroundId(backgroundId);
    }

    public void addEntity(Entity entity) {
        entitiesToAdd.add(entity);
    }

    public void removeEntity(Entity entity) {
        entitiesToRemove.add(entity);
    }

    public void update(float delta) {
        // Add & remove pending entities
        for (Entity e : entitiesToAdd) {
            entities.add(e);
            physicsEngine.addEntity(e);
        }
        entitiesToAdd.clear();
        
        for (Entity e : entitiesToRemove) {
            entities.remove(e);
            physicsEngine.removeEntity(e);
        }
        entitiesToRemove.clear();

        for (System system : systems) {
            system.update(delta, this);
        }
        
        physicsEngine.update(delta);
    }

    public void render(String debugText) {
        scene.setEntities(entities);
        renderer.render(scene, debugText);
    }

    public void dispose() {
        renderer.dispose();
    }
}
