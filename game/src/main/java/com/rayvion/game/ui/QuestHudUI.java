package com.rayvion.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.rayvion.engine.quest.Quest;
import com.rayvion.engine.quest.QuestGoal;
import com.rayvion.engine.quest.QuestSystem;
import lombok.Getter;

import java.util.Collection;

public class QuestHudUI {
    @Getter
    private final Stage stage;
    private final Skin skin;
    private final QuestSystem questSystem;
    private final Table mainTable;
    private final Texture backgroundTexture;

    public QuestHudUI(QuestSystem questSystem) {
        this.questSystem = questSystem;
        this.stage = new Stage(new ScreenViewport());
        this.skin = createDefaultSkin();

        // Create a semi-transparent background
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0, 0, 0, 0.5f));
        pixmap.fill();
        this.backgroundTexture = new Texture(pixmap);
        pixmap.dispose();

        this.mainTable = new Table();
        this.mainTable.top().right().pad(20);
        this.mainTable.setFillParent(true);
        this.stage.addActor(mainTable);

        refresh();
    }

    private Skin createDefaultSkin() {
        Skin skin = new Skin();
        BitmapFont font = new BitmapFont();
        skin.add("default", font);

        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = font;
        titleStyle.fontColor = Color.GOLD;
        skin.add("title", titleStyle);

        Label.LabelStyle descStyle = new Label.LabelStyle();
        descStyle.font = font;
        descStyle.fontColor = Color.WHITE;
        skin.add("default", descStyle);

        Label.LabelStyle goalStyle = new Label.LabelStyle();
        goalStyle.font = font;
        goalStyle.fontColor = Color.LIGHT_GRAY;
        skin.add("goal", goalStyle);

        return skin;
    }

    public void refresh() {
        mainTable.clear();

        Collection<Quest> activeQuests = questSystem.getActiveQuests();
        if (activeQuests.isEmpty()) {
            return;
        }

        Table questListTable = new Table();
        questListTable.setBackground(new TextureRegionDrawable(backgroundTexture));
        questListTable.pad(10);
        
        mainTable.add(questListTable).width(250);

        for (Quest quest : activeQuests) {
            if (quest.isCompleted()) continue;

            Label titleLabel = new Label(quest.getName(), skin, "title");
            questListTable.add(titleLabel).left().padBottom(2).row();

            Label descLabel = new Label(quest.getDescription(), skin);
            descLabel.setWrap(true);
            questListTable.add(descLabel).left().width(230).padBottom(5).row();

            for (QuestGoal goal : quest.getGoals()) {
                String status = goal.isCompleted() ? "[DONE] " : String.format("[%d%%] ", (int)(goal.getProgress() * 100));
                Label goalLabel = new Label(status + goal.getDescription(), skin, "goal");
                goalLabel.setWrap(true);
                questListTable.add(goalLabel).left().width(220).padLeft(10).padBottom(2).row();
            }
            
            // Separator if not last
            questListTable.add(new Label("", skin)).height(10).row();
        }
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void render() {
        // Polling refresh for simplicity, can be optimized with events
        refresh();
        stage.act();
        stage.draw();
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
        backgroundTexture.dispose();
    }

}
