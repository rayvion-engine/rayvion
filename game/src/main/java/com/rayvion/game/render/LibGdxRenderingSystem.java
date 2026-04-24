package com.rayvion.game.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rayvion.engine.graphics.EntityGraphics;
import com.rayvion.engine.graphics.GraphicsSystem;
import com.rayvion.engine.graphics.TextureGraphics;
import com.rayvion.engine.graphics.TiledWorldGraphics;
import com.rayvion.engine.graphics.WorldGraphics;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;

import java.util.HashMap;
import java.util.Map;

public class LibGdxRenderingSystem {
    private final GraphicsSystem graphicsSystem;
    private final TransformSystem transformSystem;
    
    private SpriteBatch batch;
    private OrthographicCamera camera;
    
    private final Map<String, Texture> textureCache = new HashMap<>();

    public LibGdxRenderingSystem(GraphicsSystem graphicsSystem, TransformSystem transformSystem) {
        this.graphicsSystem = graphicsSystem;
        this.transformSystem = transformSystem;
    }

    public void init() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera(800, 600);
        camera.position.set(160, 160, 0);
        camera.update();
    }

    public void render() {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // 1. Render World
        WorldGraphics worldGraphics = graphicsSystem.getWorldGraphics();
        if (worldGraphics instanceof TiledWorldGraphics tiledWorld) {
            renderTiledWorld(tiledWorld);
        }

        // 2. Render Entities
        for (long i = 0; i < 1000; i++) {
            if (graphicsSystem.hasEntityGraphics(i)) {
                EntityGraphics eg = graphicsSystem.getEntityGraphics(i);
                Transform t = transformSystem.getTransform(i);
                
                if (eg != null && t != null) {
                    renderEntity(eg, t);
                }
            }
        }

        batch.end();
    }
    
    private void renderTiledWorld(TiledWorldGraphics tiledWorld) {
        float tileSize = (float) tiledWorld.tileSize();
        for (int y = 0; y < tiledWorld.height(); y++) {
            for (int x = 0; x < tiledWorld.width(); x++) {
                String tileId = tiledWorld.getTileId(x, y);
                if (tileId != null) {
                    Texture texture = getTexture(tileId);
                    batch.draw(texture, x * tileSize, y * tileSize, tileSize, tileSize);
                }
            }
        }
    }
    
    private void renderEntity(EntityGraphics eg, Transform t) {
        if (eg instanceof TextureGraphics(String textureId)) {
            Texture texture = getTexture(textureId);
            float x = (float) t.getX();
            float y = (float) t.getY();
            float rot = (float) Math.toDegrees(t.getRotationZ());

            float width = texture.getWidth();
            float height = texture.getHeight();
            
            // Make entity size constant
            float scale = 32f / Math.max(width, height);
            float drawW = width * scale;
            float drawH = height * scale;

            batch.draw(new TextureRegion(texture), 
                    x - drawW / 2f, y - drawH / 2f, 
                    drawW / 2f, drawH / 2f, 
                    drawW, drawH, 
                    1f, 1f, rot);
        }
    }

    private Texture getTexture(String id) {
        return textureCache.computeIfAbsent(id, fileName -> {
            try {
                // Try loading from resources/textures/
                String path = "textures/" + fileName + ".png";
                if (Gdx.files.internal(path).exists()) {
                    return new Texture(Gdx.files.internal(path));
                }
            } catch (Exception ignored) {}

            // Fallback to placeholder
            Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
            if (id.contains("player")) {
                pixmap.setColor(Color.CYAN);
            } else if (id.contains("wall")) {
                pixmap.setColor(Color.GRAY);
            } else if (id.contains("floor")) {
                pixmap.setColor(Color.DARK_GRAY);
            } else {
                pixmap.setColor(Color.MAGENTA);
            }
            pixmap.fill();
            Texture texture = new Texture(pixmap);
            pixmap.dispose();
            return texture;
        });
    }

    public void dispose() {
        if (batch != null) batch.dispose();
        for (Texture texture : textureCache.values()) {
            texture.dispose();
        }
        textureCache.clear();
    }
}
