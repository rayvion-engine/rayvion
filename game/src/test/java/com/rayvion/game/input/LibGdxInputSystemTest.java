package com.rayvion.game.input;

import com.badlogic.gdx.Input;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.input.KeyEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LibGdxInputSystemTest {

    private EventManager eventManager;
    private LibGdxInputSystem inputSystem;

    @BeforeEach
    void setUp() {
        eventManager = mock(EventManager.class);
        inputSystem = new LibGdxInputSystem(eventManager);
    }

    @Test
    void testInit() {
        inputSystem.init();
        verify(eventManager).registerEventType(KeyEvent.class);
    }

    @Test
    void testKeyDown() {
        int keycode = Input.Keys.SPACE;
        boolean handled = inputSystem.keyDown(keycode);

        assertFalse(handled, "keyDown should return false to allow other processors to handle it");
        
        ArgumentCaptor<KeyEvent> eventCaptor = ArgumentCaptor.forClass(KeyEvent.class);
        verify(eventManager).publish(eventCaptor.capture());
        
        KeyEvent capturedEvent = eventCaptor.getValue();
        assertEquals(keycode, capturedEvent.keycode());
        assertEquals(KeyEvent.Type.KEY_DOWN, capturedEvent.type());
    }

    @Test
    void testKeyUp() {
        int keycode = Input.Keys.ENTER;
        boolean handled = inputSystem.keyUp(keycode);

        assertFalse(handled, "keyUp should return false");
        
        ArgumentCaptor<KeyEvent> eventCaptor = ArgumentCaptor.forClass(KeyEvent.class);
        verify(eventManager).publish(eventCaptor.capture());
        
        KeyEvent capturedEvent = eventCaptor.getValue();
        assertEquals(keycode, capturedEvent.keycode());
        assertEquals(KeyEvent.Type.KEY_UP, capturedEvent.type());
    }

    @Test
    void testKeyTyped() {
        boolean handled = inputSystem.keyTyped('a');
        assertFalse(handled, "keyTyped should return false");
        verifyNoInteractions(eventManager);
    }
}
