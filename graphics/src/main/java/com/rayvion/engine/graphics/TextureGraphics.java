package com.rayvion.engine.graphics;

/**
 * An {@link EntityGraphics} descriptor that renders an entity using a single,
 * static texture asset.
 *
 * @param textureId the identifier of the texture asset to render; must match
 *                  a key registered with the graphics back-end's texture atlas
 *                  or asset manager.
 */
public record TextureGraphics(String textureId) implements EntityGraphics {
}
