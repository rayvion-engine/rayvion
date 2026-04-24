package com.rayvion.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.rayvion.engine.equipment.EquipmentSystem;
import com.rayvion.engine.inventory.Inventory;
import com.rayvion.engine.inventory.InventoryItem;
import com.rayvion.engine.inventory.InventorySystem;
import com.rayvion.engine.entity.Entity;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.inventory.ItemInteractEvent;
import lombok.Getter;

import java.util.Optional;

public class InventoryUI {
    @Getter
    private final Stage stage;
    private final Skin skin;
    private final InventorySystem inventorySystem;
    private final EquipmentSystem equipmentSystem;
    private final EventManager eventManager;
    private final long playerEntityId;
    private final Table rootTable;
    private Table inventoryWindow;
    
    @Getter
    private boolean visible = false;

    public InventoryUI(InventorySystem inventorySystem, EquipmentSystem equipmentSystem, EventManager eventManager, long playerEntityId) {
        this.inventorySystem = inventorySystem;
        this.equipmentSystem = equipmentSystem;
        this.eventManager = eventManager;
        this.playerEntityId = playerEntityId;
        this.stage = new Stage(new ScreenViewport());
        this.skin = createDefaultSkin();
        
        this.rootTable = new Table();
        this.rootTable.setFillParent(true);
        this.stage.addActor(rootTable);
        
        // ESC key to close
        this.stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (visible && (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.I)) {
                    setVisible(false);
                    return true;
                }
                return false;
            }
        });
        
        refresh();
    }

    private Skin createDefaultSkin() {
        Skin skin = new Skin();
        BitmapFont font = new BitmapFont();
        skin.add("default", font);

        // Window background: Dark semi-transparent
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.05f, 0.05f, 0.05f, 0.85f));
        pixmap.fill();
        skin.add("bg", new Texture(pixmap));

        // Slot background: Dark gray
        pixmap.setColor(new Color(0.2f, 0.2f, 0.2f, 1f));
        pixmap.fill();
        skin.add("slot", new Texture(pixmap));

        // Hover background: Light gray/blue
        pixmap.setColor(new Color(0.3f, 0.3f, 0.5f, 1f));
        pixmap.fill();
        skin.add("hover", new Texture(pixmap));
        
        // Equipped background: Greenish
        pixmap.setColor(new Color(0.1f, 0.4f, 0.1f, 1f));
        pixmap.fill();
        skin.add("equipped", new Texture(pixmap));

        pixmap.dispose();

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);
        
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = font;
        titleStyle.fontColor = Color.GOLD;
        skin.add("title", titleStyle);

        TextButton.TextButtonStyle slotStyle = new TextButton.TextButtonStyle();
        slotStyle.up = skin.newDrawable("slot");
        slotStyle.over = skin.newDrawable("hover");
        slotStyle.font = font;
        skin.add("slot", slotStyle);
        
        TextButton.TextButtonStyle equippedStyle = new TextButton.TextButtonStyle();
        equippedStyle.up = skin.newDrawable("equipped");
        equippedStyle.over = skin.newDrawable("hover");
        equippedStyle.font = font;
        skin.add("equipped", equippedStyle);

        return skin;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (visible) {
            refresh();
        } else {
            rootTable.clear();
        }
    }

    public void refresh() {
        rootTable.clear();
        
        if (!visible) return;

        // Darken background
        rootTable.setBackground(skin.newDrawable("bg", new Color(0, 0, 0, 0.5f)));

        inventoryWindow = new Table();
        inventoryWindow.setBackground(skin.getDrawable("bg"));
        inventoryWindow.pad(20);
        
        rootTable.add(inventoryWindow).width(500).height(400);

        // Header
        Label title = new Label("INVENTORY", skin, "title");
        inventoryWindow.add(title).colspan(4).padBottom(20).row();

        // Items Grid
        Entity player = new Entity(playerEntityId);
        Optional<Inventory> inventoryOpt = inventorySystem.getInventory(player);
        
        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();
            Optional<InventoryItem> equippedItem = equipmentSystem.getEquippedItem(playerEntityId);

            Table itemsTable = new Table();
            inventoryWindow.add(itemsTable).expand().top().row();

            int count = 0;
            for (InventoryItem item : inventory.getItems()) {
                boolean isEquipped = equippedItem.isPresent() && equippedItem.get().equals(item);
                
                String styleName = isEquipped ? "equipped" : "slot";
                TextButton slot = new TextButton(item.name(), skin, styleName);
                slot.getLabel().setWrap(true);
                
                slot.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        eventManager.publish(new ItemInteractEvent(player, item));
                        refresh();
                    }
                });

                itemsTable.add(slot).size(100, 100).pad(5);
                count++;
                if (count % 4 == 0) itemsTable.row();
            }
            
            // Fill empty slots for beauty
            for (int i = count; i < 12; i++) {
                TextButton emptySlot = new TextButton("", skin, "slot");
                itemsTable.add(emptySlot).size(100, 100).pad(5);
                if ((i + 1) % 4 == 0) itemsTable.row();
            }
        }

        inventoryWindow.row();
        
        // Footer / Close Button
        TextButton closeButton = new TextButton("CLOSE (ESC)", skin, "slot");
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
            }
        });
        inventoryWindow.add(closeButton).width(150).height(40).padTop(20);
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        if (visible) refresh();
    }

    public void render() {
        if (!visible) return;
        stage.act();
        stage.draw();
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
