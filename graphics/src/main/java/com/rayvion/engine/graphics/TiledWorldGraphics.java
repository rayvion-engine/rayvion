package com.rayvion.engine.graphics;

public record TiledWorldGraphics(
    int width,
    int height,
    double tileSize,
    String[][] tiles
) implements WorldGraphics {
    public String getTileId(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return null;
        return tiles[y][x];
    }
}
