package eth.likespro.cyberpunkeb.core;

public interface Renderer {
    void init();
    default void render(Scene scene) { render(scene, ""); }
    void render(Scene scene, String debugText);
    void resize(int width, int height);
    void dispose();
}
