package eth.likespro.cyberpunkeb;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import eth.likespro.cyberpunkeb.gui.menu.MainMenu;

public class MainGame extends Game {
    public SpriteBatch batch;
    public ShapeRenderer shapeRenderer;
    public BitmapFont font;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        this.setScreen(new MainMenu(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        font.dispose();
        super.dispose();
    }
}
