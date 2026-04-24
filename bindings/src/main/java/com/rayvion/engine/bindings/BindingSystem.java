package com.rayvion.engine.bindings;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * System that manages key bindings and emits high-level binding events based on raw input.
 * <p>
 * This system allows mapping physical keys to abstract {@link BindingParameter}s.
 * When a bound key is pressed or released, the system publishes a {@link BindingEvent}.
 * If multiple keys are bound to the same parameter, the parameter remains active as long
 * as at least one of its bound keys is held down.
 */
public interface BindingSystem extends System {
    /**
     * The unique coordinate for this system trait.
     */
    SystemTraitCoordinate TRAIT = new SystemTraitCoordinate("com.rayvion.engine", "bindings", Version.parse("0.1.0"));

    @Override
    default SystemDescriptor getDescriptor() {
        return new SystemDescriptor(
                new com.rayvion.engine.system.descriptor.SystemCoordinate("com.rayvion.engine", "bindings", Version.parse("0.1.0")),
                Set.of(),
                Set.of(TRAIT)
        );
    }

    /**
     * Creates a new binding group or returns an existing one with the same name.
     *
     * @param name the name of the group
     * @return the created or existing binding group
     */
    BindingGroup createGroup(String name);

    /**
     * Creates a new binding parameter within a group or returns an existing one.
     *
     * @param group the group to add the parameter to
     * @param name  the name of the parameter
     * @return the created or existing binding parameter
     */
    BindingParameter createParameter(BindingGroup group, String name);

    /**
     * Maps a physical key code to a binding parameter.
     *
     * @param parameter the parameter to bind
     * @param keycode   the key code to associate with the parameter
     */
    void addBinding(BindingParameter parameter, int keycode);

    /**
     * Removes a mapping between a physical key code and a binding parameter.
     *
     * @param parameter the parameter to unbind
     * @param keycode   the key code to dissociate from the parameter
     */
    void removeBinding(BindingParameter parameter, int keycode);

    /**
     * Returns all key codes currently bound to the specified parameter.
     *
     * @param parameter the parameter to query
     * @return a collection of key codes bound to the parameter
     */
    Collection<Integer> getBindings(BindingParameter parameter);

    /**
     * Retrieves a binding group by its name.
     *
     * @param name the name of the group
     * @return an optional containing the group if found, or empty otherwise
     */
    Optional<BindingGroup> getGroup(String name);

    /**
     * Retrieves a binding parameter by its name within a specific group.
     *
     * @param group the group containing the parameter
     * @param name  the name of the parameter
     * @return an optional containing the parameter if found, or empty otherwise
     */
    Optional<BindingParameter> getParameter(BindingGroup group, String name);
}
