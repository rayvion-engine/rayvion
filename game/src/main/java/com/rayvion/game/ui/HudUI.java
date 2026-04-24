package com.rayvion.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.characteristic.CharacteristicChangedEvent;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class HudUI {
    private final Stage stage;
    private final Skin skin;
    private final CharacteristicSystem characteristicSystem;
    private final long playerEntityId;
    private final Table healthTable;
    
    private final Texture heartFull;
    private final Texture heartHalf;
    private final Texture heartEmpty;
    
    private double lastHealth = -1;
    private double lastMaxHealth = -1;

    public HudUI(CharacteristicSystem characteristicSystem, EventManager eventManager, long playerEntityId) {
        this.characteristicSystem = characteristicSystem;
        this.playerEntityId = playerEntityId;
        this.stage = new Stage(new ScreenViewport());
        this.skin = createDefaultSkin();
        
        this.healthTable = new Table();
        this.healthTable.top().left().pad(10);
        this.healthTable.setFillParent(true);
        this.stage.addActor(healthTable);
        
        // Load textures
        this.heartFull = loadTexture("heart_full");
        this.heartHalf = loadTexture("heart_half");
        this.heartEmpty = loadTexture("heart_empty");
        
        refresh();
        
        // Subscribe to changes to refresh UI
        eventManager.subscribe(CharacteristicChangedEvent.class, event -> {
            if (event.getEntity().id() == playerEntityId && 
                (event.getCharacteristicId().equals("health") || event.getCharacteristicId().equals("max_health"))) {
                refresh();
            }
        });
    }

    private Texture loadTexture(String name) {
        String path = "textures/" + name + ".png";
        if (Gdx.files.internal(path).exists()) {
            return new Texture(Gdx.files.internal(path));
        }
        // Fallback to a colored square if texture missing
        Pixmap pixmap = new Pixmap(32, 32, Pixmap.Format.RGBA8888);
        pixmap.setColor(name.contains("full") ? Color.RED : (name.contains("half") ? Color.ORANGE : Color.GRAY));
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Skin createDefaultSkin() {
        Skin skin = new Skin();
        BitmapFont font = new BitmapFont();
        skin.add("default", font);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        return skin;
    }

    public void refresh() {
        Entity player = new Entity(playerEntityId);
        double health = characteristicSystem.getValue(player, "health");
        double maxHealth = characteristicSystem.getValue(player, "max_health");
        
        if (health == lastHealth && maxHealth == lastMaxHealth) {
            return;
        }
        
        lastHealth = health;
        lastMaxHealth = maxHealth;
        
        healthTable.clear();
        
        // Add a label
        Label healthLabel = new Label(String.format("HP: %.0f / %.0f", health, maxHealth), skin);
        healthTable.add(healthLabel).left().padBottom(5).row();
        
        Table heartsTable = new Table();
        healthTable.add(heartsTable).left();
        
        int hpPerHeart = 20;
        int totalHearts = (int) Math.ceil(maxHealth / hpPerHeart);
        double currentHp = health;
        
        for (int i = 0; i < totalHearts; i++) {
            Image heart;
            if (currentHp >= hpPerHeart) {
                heart = new Image(heartFull);
                currentHp -= hpPerHeart;
            } else if (currentHp >= hpPerHeart / 2.0) {
                heart = new Image(heartHalf);
                currentHp = 0;
            } else {
                heart = new Image(heartEmpty);
            }
            heartsTable.add(heart).size(32, 32).padRight(2);
        }
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void render() {
        // Double check refresh in case event wasn't fired or something changed
        refresh();
        stage.act();
        stage.draw();
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
        heartFull.dispose();
        heartHalf.dispose();
        heartEmpty.dispose();
    }
}
