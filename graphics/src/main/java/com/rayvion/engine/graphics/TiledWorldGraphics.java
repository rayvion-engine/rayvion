package com.rayvion.engine.graphics;

/**
 * A {@link WorldGraphics} descriptor that represents the game world as a
 * rectangular grid of individually textured tiles.
 *
 * <p>Tile positions are addressed with zero-based {@code (x, y)} coordinates
 * where {@code x} increases to the right and {@code y} increases downward.
 * The backing {@code tiles} array is stored in row-major order as
 * {@code tiles[y][x]}.</p>
 *
 * <p>A {@code null} entry in the {@code tiles} array means the tile at that
 * position is empty (transparent / no rendering).</p>
 *
 * @param width    the number of tile columns in the world; must be positive.
 * @param height   the number of tile rows in the world; must be positive.
 * @param tileSize the size of a single tile in world-unit coordinates
 *                 (e.g., pixels or metres depending on the engine scale);
 *                 must be positive.
 * @param tiles    a 2-D array of texture-asset identifiers indexed as
 *                 {@code tiles[y][x]}; {@code null} entries represent empty tiles.
 *                 The array must have exactly {@code height} rows, each with
 *                 exactly {@code width} columns.
 */
public record TiledWorldGraphics(
    int width,
    int height,
    double tileSize,
    String[][] tiles
) implements WorldGraphics {
    /**
     * Returns the texture-asset identifier of the tile at the given grid
     * coordinates, or {@code null} if the coordinates are out of bounds or
     * the tile is empty.
     *
     * @param x the zero-based column index of the tile (left → right).
     * @param y the zero-based row index of the tile (top → bottom).
     * @return the texture ID stored at {@code tiles[y][x]}, or {@code null}
     *         if {@code x < 0}, {@code x >= width}, {@code y < 0},
     *         {@code y >= height}, or the tile entry itself is {@code null}.
     */
    public String getTileId(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return null;
        return tiles[y][x];
    }
}
