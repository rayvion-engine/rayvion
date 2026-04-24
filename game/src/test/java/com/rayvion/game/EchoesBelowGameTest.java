package com.rayvion.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.Graphics;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.quest.QuestSystem;
import com.rayvion.engine.system.manager.SystemManager;
import com.rayvion.game.render.LibGdxRenderingSystem;
import com.rayvion.game.ui.GameOverUI;
import com.rayvion.game.ui.HudUI;
import com.rayvion.game.ui.InventoryUI;
import com.rayvion.game.ui.QuestHudUI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EchoesBelowGameTest {

    private EchoesBelowGame game;
    private Graphics graphics;
    private Input input;
    private Files files;
    private Application app;
    private GL20 gl20;
    private Audio audio;

    @BeforeEach
    void setUp() {
        graphics = mock(Graphics.class);
        input = mock(Input.class);
        files = mock(Files.class);
        app = mock(Application.class);
        gl20 = mock(GL20.class);
        audio = mock(Audio.class);

        Gdx.graphics = graphics;
        Gdx.input = input;
        Gdx.files = files;
        Gdx.app = app;
        Gdx.gl = gl20;
        Gdx.gl20 = gl20;
        Gdx.audio = audio;

        // Mock FileHandle to avoid NPEs during asset loading
        FileHandle mockFileHandle = mock(FileHandle.class);
        when(files.internal(anyString())).thenReturn(mockFileHandle);
        when(mockFileHandle.exists()).thenReturn(false); // Fallback to placeholders

        game = new EchoesBelowGame() {
            @Override
            protected LibGdxRenderingSystem createRenderingSystem() {
                return mock(LibGdxRenderingSystem.class);
            }

            @Override
            protected InventoryUI createInventoryUI() {
                InventoryUI mock = mock(InventoryUI.class);
                when(mock.getStage()).thenReturn(mock(com.badlogic.gdx.scenes.scene2d.Stage.class));
                return mock;
            }

            @Override
            protected HudUI createHudUI() {
                return mock(HudUI.class);
            }

            @Override
            protected QuestHudUI createQuestHudUI() {
                return mock(QuestHudUI.class);
            }

            @Override
            protected GameOverUI createGameOverUI() {
                return mock(GameOverUI.class);
            }
        };
    }

    @Test
    void testCreate() {
        game.create();

        assertNotNull(game.getSystemManager());
        assertNotNull(game.getEventManager());
        assertNotNull(game.getQuestSystem());
        assertTrue(game.getCurrentPlayerId() >= 0);

        SystemManager sm = game.getSystemManager();
        // Verify core systems are present
        assertNotNull(sm);
    }

    @Test
    void testSetupBasicGame() {
        game.create();
        
        QuestSystem qs = game.getQuestSystem();
        assertNotNull(qs.getQuest("system_override"));
        assertNotNull(qs.getQuest("access_granted"));
        assertNotNull(qs.getQuest("executive_deletion"));
    }

    @Test
    void testRender() {
        game.create();
        // Should not throw exception
        assertDoesNotThrow(() -> game.render());
    }

    @Test
    void testResize() {
        game.create();
        assertDoesNotThrow(() -> game.resize(1024, 768));
    }

    @Test
    void testDispose() {
        game.create();
        assertDoesNotThrow(() -> game.dispose());
    }
}
