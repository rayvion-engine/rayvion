package com.rayvion.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Full-screen "GAME OVER" overlay shown when the player dies.
 */
@Slf4j
public class GameOverUI {

    private final SpriteBatch batch;
    private final BitmapFont titleFont;
    private final BitmapFont subtitleFont;
    private final Texture overlayTexture;

    @Getter
    private boolean visible = false;

    private float alpha = 0f;
    private static final float FADE_SPEED = 1.5f;

    public GameOverUI() {
        this.batch = new SpriteBatch();

        // Create fonts
        this.titleFont = new BitmapFont();
        this.titleFont.getData().setScale(4f);
        this.titleFont.setColor(new Color(0.9f, 0.15f, 0.1f, 1f));

        this.subtitleFont = new BitmapFont();
        this.subtitleFont.getData().setScale(1.5f);
        this.subtitleFont.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));

        // Create a 1x1 dark overlay texture
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.05f, 0.02f, 0.02f, 1f));
        pixmap.fill();
        this.overlayTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void show() {
        this.visible = true;
        this.alpha = 0f;
        log.info("Game Over screen activated");
    }

    public void render() {
        if (!visible) return;

        // Fade in
        if (alpha < 0.85f) {
            alpha += FADE_SPEED * Gdx.graphics.getDeltaTime();
            if (alpha > 0.85f) alpha = 0.85f;
        }

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        batch.begin();

        // Draw dark overlay
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(overlayTexture, 0, 0, screenWidth, screenHeight);
        batch.setColor(1f, 1f, 1f, 1f);

        // Only draw text once sufficiently faded in
        if (alpha > 0.3f) {
            float textAlpha = Math.min(1f, (alpha - 0.3f) / 0.55f);

            // Title: "GAME OVER"
            titleFont.setColor(new Color(0.9f, 0.15f, 0.1f, textAlpha));
            GlyphLayout titleLayout = new GlyphLayout(titleFont, "GAME OVER");
            float titleX = (screenWidth - titleLayout.width) / 2f;
            float titleY = (screenHeight + titleLayout.height) / 2f + 40;
            titleFont.draw(batch, titleLayout, titleX, titleY);

            // Subtitle: "Signal lost..."
            subtitleFont.setColor(new Color(0.7f, 0.7f, 0.7f, textAlpha * 0.8f));
            GlyphLayout subLayout = new GlyphLayout(subtitleFont, "Signal lost...");
            float subX = (screenWidth - subLayout.width) / 2f;
            float subY = titleY - titleLayout.height - 30;
            subtitleFont.draw(batch, subLayout, subX, subY);

            // Hint: "Press [R] to restart"
            if (alpha >= 0.85f) {
                // Pulsing effect
                float pulse = (float) (0.5f + 0.5f * Math.sin(System.currentTimeMillis() / 400.0));
                subtitleFont.setColor(new Color(0.5f, 0.8f, 1.0f, pulse));
                GlyphLayout hintLayout = new GlyphLayout(subtitleFont, "Press [R] to restart");
                float hintX = (screenWidth - hintLayout.width) / 2f;
                float hintY = subY - subLayout.height - 40;
                subtitleFont.draw(batch, hintLayout, hintX, hintY);
            }
        }

        batch.end();
    }

    public void resize(int width, int height) {
        batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    }

    public void dispose() {
        batch.dispose();
        titleFont.dispose();
        subtitleFont.dispose();
        overlayTexture.dispose();
    }
}
