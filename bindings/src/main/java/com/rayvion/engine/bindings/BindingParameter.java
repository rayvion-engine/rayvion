package com.rayvion.engine.bindings;

/**
 * Represents an abstract action or parameter that can be bound to one or more physical keys.
 *
 * @param name the unique name of the parameter within its group
 * @param group the group this parameter belongs to
 */
public record BindingParameter(String name, BindingGroup group) {
}
