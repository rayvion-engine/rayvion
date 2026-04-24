package com.rayvion.engine.input;

import com.rayvion.engine.event.Event;

/**
 * An immutable event that represents a single keyboard input action.
 *
 * <p>A {@code KeyEvent} is created by the input system whenever the keyboard
 * state changes and is published to the {@link com.rayvion.engine.event.EventManager}
 * so that any interested subscriber can react to key presses or releases.
 *
 * @param keycode the platform-specific integer key code identifying which physical
 *                key triggered this event (matches libGDX {@code Input.Keys} constants)
 * @param type    the classification of the keyboard action that produced this event
 */
public record KeyEvent(int keycode, Type type) implements Event {

    /**
     * Classifies the kind of keyboard action that produced a {@link KeyEvent}.
     */
    public enum Type {

        /**
         * Fired when a key transitions from the released state to the pressed state.
         * This is typically the event used to trigger gameplay actions (movement,
         * attacks, menu navigation, etc.).
         */
        KEY_DOWN,

        /**
         * Fired when a previously pressed key is released.
         * Use this to stop a continuous action that was started on a {@link #KEY_DOWN} event.
         */
        KEY_UP,

        /**
         * Fired for character-generating key presses, carrying the resulting character
         * rather than a raw key code.
         *
         * <p>Note: this variant is currently <em>not</em> published by
         * {@code LibGdxInputSystem} and is reserved for future text-input support.
         */
        KEY_TYPED
    }
}
