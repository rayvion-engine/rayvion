package com.rayvion.engine.graphics;

/**
 * Sealed root interface for entity graphics descriptors.
 *
 * <p>An {@code EntityGraphics} value describes <em>how</em> an entity should be
 * rendered by the graphics back-end, but carries no rendering state itself.
 * Concrete variants are:</p>
 * <ul>
 *   <li>{@link TextureGraphics} – renders the entity using a single static texture.</li>
 *   <li>{@link AnimationGraphics} – renders the entity using a sequence of textures
 *       that are cycled over time to produce an animation.</li>
 * </ul>
 *
 * <p>Use pattern-matching ({@code instanceof} or {@code switch}) to distinguish
 * between permitted subtypes at the call-site.</p>
 */
public sealed interface EntityGraphics permits TextureGraphics, AnimationGraphics {
}
