package com.rayvion.engine.bindings;

import com.rayvion.engine.event.Event;
import com.rayvion.engine.input.KeyEvent;

/**
 * Event emitted when a bound key is pressed or released.
 *
 * @param parameter the parameter associated with the binding
 * @param type the type of the key event (press or release)
 */
public record BindingEvent(BindingParameter parameter, KeyEvent.Type type) implements Event {
}
