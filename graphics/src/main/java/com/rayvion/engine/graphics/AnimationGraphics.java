package com.rayvion.engine.graphics;

import java.util.List;

public record AnimationGraphics(
    List<String> frameTextureIds,
    double frameDurationSeconds,
    boolean isLooping
) implements EntityGraphics {
}
