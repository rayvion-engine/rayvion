package com.rayvion.game.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.input.InputSystem;
import com.rayvion.engine.input.KeyEvent;

import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * libGDX-backed implementation of {@link InputSystem} that bridges the libGDX
 * input pipeline with the engine's event bus.
 *
 * <p>{@code LibGdxInputSystem} extends libGDX's {@code InputAdapter} and is
 * registered as the active input processor (via {@code Gdx.input.setInputProcessor})
 * so that libGDX delivers raw keyboard callbacks to this class. Each callback is
 * translated into a strongly-typed {@link KeyEvent} and published through the
 * {@link EventManager}, decoupling gameplay systems from the underlying platform.
 *
 * <p>Lifecycle:
 * <ol>
 *   <li>{@link #init()} — registers the {@code KeyEvent} type with the event manager.</li>
 *   <li>During each frame libGDX calls {@link #keyDown} / {@link #keyUp} as appropriate.</li>
 * </ol>
 */
@Slf4j
public class LibGdxInputSystem extends InputAdapter implements InputSystem {
    private final EventManager eventManager;

    /**
     * Constructs a new {@code LibGdxInputSystem}.
     *
     * @param eventManager the engine's event bus used to register event types and
     *                     publish {@link KeyEvent} instances to subscribers
     */
    public LibGdxInputSystem(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    /**
     * Initialises the input system by registering the {@link KeyEvent} type with
     * the {@link EventManager}.
     *
     * <p>This must be called before any key events are published. After this
     * method returns, subscribers may start listening for {@code KeyEvent}
     * instances on the event bus.
     */
    @Override
    public void init() {
        // Register the KeyEvent type so it can be published
        eventManager.registerEventType(KeyEvent.class);
    }

    /**
     * Called by libGDX when a key is pressed down.
     *
     * <p>Publishes a {@link KeyEvent} of type {@link KeyEvent.Type#KEY_DOWN} to
     * the event bus so that subscribers (e.g. {@code PlayerMovementSystem}) can
     * react immediately to the key press.
     *
     * @param keycode the libGDX key code of the key that was pressed
     *                (see {@code com.badlogic.gdx.Input.Keys})
     * @return {@code false} so that subsequent input processors in the libGDX
     *         chain also receive this event
     */
    @Override
    public boolean keyDown(int keycode) {
        log.debug("LibGdxInputSystem: Key Down: {}", keycode);
        eventManager.publish(new KeyEvent(keycode, KeyEvent.Type.KEY_DOWN));
        return false; // Return false to allow other processors to handle it if needed
    }

    /**
     * Called by libGDX when a previously pressed key is released.
     *
     * <p>Publishes a {@link KeyEvent} of type {@link KeyEvent.Type#KEY_UP} to
     * the event bus, allowing subscribers to stop continuous actions that were
     * initiated on a corresponding {@link KeyEvent.Type#KEY_DOWN} event.
     *
     * @param keycode the libGDX key code of the key that was released
     *                (see {@code com.badlogic.gdx.Input.Keys})
     * @return {@code false} so that subsequent input processors in the libGDX
     *         chain also receive this event
     */
    @Override
    public boolean keyUp(int keycode) {
        eventManager.publish(new KeyEvent(keycode, KeyEvent.Type.KEY_UP));
        return false;
    }

    /**
     * Called by libGDX when a character-generating key is typed.
     *
     * <p>Currently a no-op — this implementation focuses on key-press / key-release
     * events rather than character input. Future text-field or chat support may
     * publish a {@link KeyEvent} of type {@link KeyEvent.Type#KEY_TYPED} here.
     *
     * @param character the Unicode character produced by the key press
     * @return {@code false} so that subsequent input processors in the libGDX
     *         chain also receive this event
     */
    @Override
    public boolean keyTyped(char character) {
        // For now, focusing on key presses
        return false;
    }
}
