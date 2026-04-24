package com.rayvion.engine.input;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;

import java.util.Set;

/**
 * Contract for the engine's input system.
 *
 * <p>The {@code InputSystem} is responsible for capturing raw hardware input
 * (keyboard, mouse, gamepad, etc.) and translating it into engine events that
 * are published through the {@link com.rayvion.engine.event.EventManager}.
 * Consumers subscribe to typed events such as {@link KeyEvent} rather than
 * polling device state directly, keeping gameplay code decoupled from the
 * underlying input backend.
 *
 * <p>This interface serves as both a contract and a <em>marker</em>: it
 * inherits the lifecycle management of {@link com.rayvion.engine.system.System}
 * and provides a fixed {@link #getDescriptor()} so that the engine's
 * {@code SystemManager} can locate and register the implementation at startup.
 *
 * <p>The sole built-in implementation is {@code LibGdxInputSystem}, which
 * bridges libGDX's {@code InputAdapter} callbacks to the event bus.
 */
public interface InputSystem extends System {

    /**
     * Returns the {@link SystemDescriptor} that identifies this system to the engine.
     *
     * <p>The descriptor is pre-configured with:
     * <ul>
     *   <li><strong>Coordinate</strong>: group {@code com.rayvion.engine},
     *       name {@code input}, version {@code 0.1.0}.</li>
     *   <li><strong>Dependencies</strong>: none — the input system has no
     *       required peer systems.</li>
     *   <li><strong>Traits</strong>: exposes the
     *       {@code (com.rayvion.engine, input, 0.1.0)} trait so that other
     *       systems can declare a dependency on "any input system" without
     *       binding to a specific implementation.</li>
     * </ul>
     *
     * @return the immutable descriptor for the input system
     */
    @Override
    default SystemDescriptor getDescriptor() {
        return new SystemDescriptor(
                new SystemCoordinate("com.rayvion.engine", "input", Version.parse("0.1.0")),
                Set.of(),
                Set.of(new SystemTraitCoordinate("com.rayvion.engine", "input", Version.parse("0.1.0")))
        );
    }
}
