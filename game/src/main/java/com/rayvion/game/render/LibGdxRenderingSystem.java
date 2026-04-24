package com.rayvion.game.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rayvion.engine.graphics.AnimationGraphics;
import com.rayvion.engine.graphics.EntityGraphics;
import com.rayvion.engine.graphics.GraphicsSystem;
import com.rayvion.engine.graphics.TextureGraphics;
import com.rayvion.engine.graphics.TiledWorldGraphics;
import com.rayvion.engine.graphics.WorldGraphics;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.entity.Entity;
import com.rayvion.game.combat.DamageFeedbackSystem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LibGdxRenderingSystem {
    private final GraphicsSystem graphicsSystem;
    private final TransformSystem transformSystem;
    private final com.rayvion.engine.graphics.CameraSystem cameraSystem;
    private final CharacteristicSystem characteristicSystem;
    private final DamageFeedbackSystem damageFeedbackSystem;
    
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Texture pixel;
    private BitmapFont font;
    
    private final Map<String, Texture> textureCache = new HashMap<>();
    private final Map<String, TextureAtlas> atlasCache = new HashMap<>();

    public LibGdxRenderingSystem(GraphicsSystem graphicsSystem, TransformSystem transformSystem, 
                                 com.rayvion.engine.graphics.CameraSystem cameraSystem, 
                                 CharacteristicSystem characteristicSystem,
                                 DamageFeedbackSystem damageFeedbackSystem) {
        this.graphicsSystem = graphicsSystem;
        this.transformSystem = transformSystem;
        this.cameraSystem = cameraSystem;
        this.characteristicSystem = characteristicSystem;
        this.damageFeedbackSystem = damageFeedbackSystem;
    }

    public void init() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(800, 600, camera);
        syncCamera();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        pixel = new Texture(pixmap);
        pixmap.dispose();

        font = new BitmapFont();
        font.getData().setScale(0.8f);
        font.setColor(Color.WHITE);
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public void render() {
        cameraSystem.update();
        syncCamera();
        
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // 1. Render World
        WorldGraphics worldGraphics = graphicsSystem.getWorldGraphics();
        if (worldGraphics instanceof TiledWorldGraphics tiledWorld) {
            renderTiledWorld(tiledWorld);
        }

        // 2. Render Entities
        for (long entityId : graphicsSystem.getEntitiesWithGraphics()) {
            EntityGraphics eg = graphicsSystem.getEntityGraphics(entityId);
            if (eg == null) continue;
            
            Transform t = transformSystem.getTransform(entityId);
            
            if (t != null) {
                renderEntity(entityId, eg, t);
                
                if (graphicsSystem.isHealthBarVisible(entityId)) {
                    renderHealthBar(entityId, t);
                }

                String prompt = graphicsSystem.getInteractionPrompt(entityId);
                if (prompt != null) {
                    renderInteractionPrompt(prompt, t);
                }
            }
        }

        batch.end();
    }
    
    private void syncCamera() {
        camera.position.set((float) cameraSystem.getX(), (float) cameraSystem.getY(), 0);
        camera.zoom = cameraSystem.getZoom();
        camera.update();
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
    
    private void renderEntity(long entityId, EntityGraphics eg, Transform t) {
        float x = (float) t.getX();
        float y = (float) t.getY();
        float rot = (float) Math.toDegrees(t.getRotationZ());

        // Use characteristics for size
        Entity entity = new Entity(entityId);
        double targetWidth = characteristicSystem.getValue(entity, "width");
        double targetHeight = characteristicSystem.getValue(entity, "height");

        float drawW = (float) targetWidth;
        float drawH = (float) targetHeight;

        boolean damaged = damageFeedbackSystem.isDamaged(entityId);
        if (damaged) {
            batch.setColor(Color.RED);
        }

        TextureRegion region = null;
        if (eg instanceof TextureGraphics(String textureId)) {
            region = getRegion(textureId);
        } else if (eg instanceof AnimationGraphics(List<String> frames, double frameDuration, boolean isLooping)) {
            if (!frames.isEmpty()) {
                double totalDuration = frames.size() * frameDuration;
                double currentTime = (System.currentTimeMillis() / 1000.0);
                
                int frameIndex;
                if (isLooping) {
                    frameIndex = (int) ((currentTime % totalDuration) / frameDuration);
                } else {
                    frameIndex = (int) (currentTime / frameDuration);
                    if (frameIndex >= frames.size()) frameIndex = frames.size() - 1;
                }
                region = getRegion(frames.get(frameIndex));
            }
        }

        if (region != null) {
            batch.draw(region, 
                    x - drawW / 2f, y - drawH / 2f, 
                    drawW / 2f, drawH / 2f, 
                    drawW, drawH, 
                    1f, 1f, rot);
        }
        
        if (damaged) {
            batch.setColor(Color.WHITE);
        }
    }

    private void renderHealthBar(long entityId, Transform t) {
        Entity entity = new Entity(entityId);
        Double health = characteristicSystem.getValue(entity, "health");
        Double maxHealth = characteristicSystem.getValue(entity, "max_health");

        if (health == null || maxHealth == null || maxHealth <= 0) return;

        float width = 32f;
        float height = 4f;
        float x = (float) t.getX() - width / 2f;
        float y = (float) t.getY() + 20f; // Positioned above the entity

        float healthPercent = (float) (health / maxHealth);
        healthPercent = Math.clamp(healthPercent, 0, 1);

        // Background
        batch.setColor(Color.BLACK);
        batch.draw(pixel, x, y, width, height);

        // Health
        Color healthColor = Color.GREEN;
        if (healthPercent < 0.25f) healthColor = Color.RED;
        else if (healthPercent < 0.5f) healthColor = Color.ORANGE;

        batch.setColor(healthColor);
        batch.draw(pixel, x, y, width * healthPercent, height);
        
        batch.setColor(Color.WHITE); // Reset color
    }

    private void renderInteractionPrompt(String message, Transform t) {
        float x = (float) t.getX();
        float y = (float) t.getY() + 40f; // Slightly higher to avoid health bars
        
        GlyphLayout layout = new GlyphLayout(font, message);
        float paddingX = 8f;
        float paddingY = 4f;
        float bgW = layout.width + paddingX * 2f;
        float bgH = layout.height + paddingY * 2f;

        // Draw background box
        batch.setColor(0, 0, 0, 0.7f);
        batch.draw(pixel, x - bgW / 2f, y - bgH / 2f, bgW, bgH);
        batch.setColor(Color.WHITE);

        // Draw centered text
        font.draw(batch, layout, x - layout.width / 2f, y + layout.height / 2f);
    }

    private TextureRegion getRegion(String textureId) {
        if (textureId.contains(":")) {
            String[] parts = textureId.split(":", 2);
            String atlasName = parts[0];
            String regionName = parts[1];
            TextureAtlas atlas = getAtlas(atlasName);
            if (atlas != null) {
                TextureAtlas.AtlasRegion region = atlas.findRegion(regionName);
                if (region != null) return region;
            }
        }
        return new TextureRegion(getTexture(textureId));
    }

    private TextureAtlas getAtlas(String atlasName) {
        return atlasCache.computeIfAbsent(atlasName, name -> {
            try {
                String path = "atlases/" + name + ".atlas";
                if (Gdx.files.internal(path).exists()) {
                    return new TextureAtlas(Gdx.files.internal(path));
                }
            } catch (Exception e) {
                Gdx.app.error("LibGdxRenderingSystem", "Failed to load atlas: " + atlasName, e);
            }
            return null;
        });
    }

    private Texture getTexture(String textureId) {
        return textureCache.computeIfAbsent(textureId, id -> {
            try {
                String path = "textures/" + id + ".png";
                if (Gdx.files.internal(path).exists()) {
                    return new Texture(Gdx.files.internal(path));
                }
            } catch (Exception ignored) {}

            // Fallback to placeholder
            Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
            if (textureId.contains("player")) {
                pixmap.setColor(Color.CYAN);
            } else if (textureId.contains("wall")) {
                pixmap.setColor(Color.GRAY);
            } else if (textureId.contains("floor")) {
                pixmap.setColor(Color.DARK_GRAY);
            } else if (textureId.contains("enemy")) {
                pixmap.setColor(Color.RED);
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
        if (pixel != null) pixel.dispose();
        if (font != null) font.dispose();
        for (Texture texture : textureCache.values()) {
            texture.dispose();
        }
        textureCache.clear();
        for (TextureAtlas atlas : atlasCache.values()) {
            atlas.dispose();
        }
        atlasCache.clear();
    }
}
