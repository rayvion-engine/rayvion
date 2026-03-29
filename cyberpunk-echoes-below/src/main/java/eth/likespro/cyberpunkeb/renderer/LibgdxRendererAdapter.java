package eth.likespro.cyberpunkeb.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import eth.likespro.cyberpunkeb.core.Entity;
import eth.likespro.cyberpunkeb.core.Renderer;
import eth.likespro.cyberpunkeb.core.Scene;
import eth.likespro.cyberpunkeb.core.component.BoundsComponent;
import eth.likespro.cyberpunkeb.core.component.RenderComponent;
import eth.likespro.cyberpunkeb.core.component.TransformComponent;

import java.util.HashMap;
import java.util.Map;

public class LibgdxRendererAdapter implements Renderer {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private OrthographicCamera camera;
    private Viewport viewport;
    
    private Map<String, Texture> textureCache;

    @Override
    public void init() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        textureCache = new HashMap<>();
        
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        viewport = new FitViewport(800, 600, camera);
    }
    
    private Texture getTexture(String id) {
        if (!textureCache.containsKey(id)) {
            try {
                textureCache.put(id, new Texture(id));
            } catch (Exception e) {
                System.err.println("Failed to load texture: " + id);
                textureCache.put(id, null);
            }
        }
        return textureCache.get(id);
    }

    @Override
    public void render(Scene scene, String debugText) {
        camera.position.x = scene.getCamera().x;
        camera.position.y = scene.getCamera().y;
        camera.zoom = scene.getCamera().zoom;
        camera.update();
        
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        
        // Draw background relative to camera
        if (scene.getBackgroundId() != null && getTexture(scene.getBackgroundId()) != null) {
            float bgX = camera.position.x - 400;
            float bgY = camera.position.y - 300;
            batch.draw(getTexture(scene.getBackgroundId()), bgX, bgY, 800, 600);
        }

        // Draw entities with textures
        for (Entity entity : scene.getEntities()) {
            TransformComponent transform = entity.getComponent(TransformComponent.class);
            BoundsComponent bounds = entity.getComponent(BoundsComponent.class);
            RenderComponent render = entity.getComponent(RenderComponent.class);
            
            if (transform == null || bounds == null) continue;

            if (render != null && render.textureId != null && getTexture(render.textureId) != null) {
                float drawX = transform.x - (bounds.width / 2f);
                float drawY = transform.y - (bounds.height / 2f);
                batch.draw(getTexture(render.textureId), drawX, drawY, bounds.width, bounds.height);
            }
        }

        if (debugText != null) {
            font.draw(batch, debugText, 10, 590);
        }
        batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GREEN);

        // Draw entities without textures
        for (Entity entity : scene.getEntities()) {
            TransformComponent transform = entity.getComponent(TransformComponent.class);
            BoundsComponent bounds = entity.getComponent(BoundsComponent.class);
            RenderComponent render = entity.getComponent(RenderComponent.class);
            
            if (transform == null || bounds == null) continue;

            if (render == null || render.textureId == null || getTexture(render.textureId) == null) {
                float drawX = transform.x - (bounds.width / 2f);
                float drawY = transform.y - (bounds.height / 2f);
                shapeRenderer.rect(drawX, drawY, bounds.width, bounds.height);
            }
        }
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height);
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        for (Texture tex : textureCache.values()) {
            if (tex != null) tex.dispose();
        }
    }
}
