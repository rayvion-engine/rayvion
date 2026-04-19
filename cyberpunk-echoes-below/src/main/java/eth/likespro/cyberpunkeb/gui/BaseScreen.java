package eth.likespro.cyberpunkeb.gui;

import com.badlogic.gdx.Screen;

public interface BaseScreen extends Screen {
    @Override
    default public void show() { }
    @Override
    default public void render(float delta) { }
    @Override
    default public void resize(int width, int height) { }
    @Override
    default public void pause() { }
    @Override
    default public void resume() { }
    @Override
    default public void hide() { }
    @Override
    default public void dispose() { }
}
