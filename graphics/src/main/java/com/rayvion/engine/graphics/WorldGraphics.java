package com.rayvion.engine.graphics;

/**
 * Sealed root interface for world-level graphics descriptors.
 *
 * <p>A {@code WorldGraphics} value describes the static background or environment
 * geometry that should be rendered beneath all entities.  It is not tied to any
 * single entity; instead, it is registered with the {@link GraphicsSystem} and
 * applies globally to the current scene.</p>
 *
 * <p>The only permitted subtype is {@link TiledWorldGraphics}, which represents
 * the world as a rectangular grid of texture-identified tiles.</p>
 *
 * <p>Use pattern-matching ({@code instanceof} or {@code switch}) to dispatch on
 * the concrete type at the rendering layer.</p>
 */
public sealed interface WorldGraphics permits TiledWorldGraphics {
}
