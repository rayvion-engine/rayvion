package eth.likespro.cyberpunkeb.gui.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import eth.likespro.cyberpunkeb.MainGame;
import eth.likespro.cyberpunkeb.gui.BaseScreen;
import eth.likespro.cyberpunkeb.gui.GameScreen;

public class MainMenu implements BaseScreen {
    private final Stage stage;
    private final Skin skin;
    private final ShapeRenderer shapeRenderer;
    private float time = 0f;

    public MainMenu(final MainGame game) {
        stage = new Stage(new ScreenViewport());
        shapeRenderer = new ShapeRenderer();

        skin = new Skin();
        com.badlogic.gdx.graphics.g2d.BitmapFont menuFont = new com.badlogic.gdx.graphics.g2d.BitmapFont();
        skin.add("default", menuFont);

        // Setup button style
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = menuFont;
        // Cyberpunk Yellow/Cyan
        textButtonStyle.fontColor = Color.valueOf("#FCEB00");
        textButtonStyle.overFontColor = Color.valueOf("#00FFFF");
        textButtonStyle.downFontColor = Color.valueOf("#FF00FF");
        skin.add("default", textButtonStyle);

        // Setup label style
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = menuFont;
        labelStyle.fontColor = Color.valueOf("#FF00FF"); // Magenta
        skin.add("default", labelStyle);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label titleLabel = new Label("Cyberpunk: Echoes Below", skin);
        titleLabel.setFontScale(3.0f);
        TextButton playButton = new TextButton("ENTER THE MATRIX", skin);
        playButton.getLabel().setFontScale(1.5f);

        table.add(titleLabel).padBottom(50).row();
        table.add(playButton).width(200).height(50).row();

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game));
                dispose();
            }
        });
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        time += delta;

        Gdx.gl.glClearColor(0.05f, 0.0f, 0.1f, 1); // Dark purple
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Neon grid
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.valueOf("#00FFFF")); // Cyan grid

        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();

        // Horizontal scrolling lines
        float lineSpacing = 40f;
        float speed = 100f;
        float offset = (time * speed) % lineSpacing;

        for (float y = offset; y < height; y += lineSpacing) {
            // Fade out as it goes up
            float alpha = 1.0f - (y / height);
            shapeRenderer.setColor(0, 1, 1, alpha);
            shapeRenderer.line(0, y, width, y);
        }

        // Vertical lines
        float centerX = width / 2f;
        for (int i = -10; i <= 10; i++) {
            float bottomX = centerX + (i * 100f);
            shapeRenderer.setColor(Color.valueOf("#FF00FF")); // Magenta
            shapeRenderer.line(centerX, height, bottomX, 0);
        }

        shapeRenderer.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        shapeRenderer.dispose();
    }
}
