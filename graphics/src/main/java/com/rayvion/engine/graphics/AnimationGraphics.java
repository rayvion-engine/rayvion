package com.rayvion.engine.graphics;

import java.util.List;

/**
 * An {@link EntityGraphics} descriptor that renders an entity using a looped or
 * one-shot sequence of texture frames, producing a sprite animation.
 *
 * <p>The graphics back-end is responsible for advancing the current frame index
 * at a cadence determined by {@code frameDurationSeconds}.  The first frame is
 * displayed at time 0; subsequent frames are shown after each
 * {@code frameDurationSeconds} interval elapses.</p>
 *
 * @param frameTextureIds     an ordered list of texture-asset identifiers that
 *                            define the animation frames; must not be empty.
 * @param frameDurationSeconds the number of seconds each individual frame is
 *                            displayed before advancing to the next one;
 *                            must be greater than zero.
 * @param isLooping           {@code true} if the animation should restart from
 *                            the first frame after the last frame finishes;
 *                            {@code false} to freeze on the last frame.
 */
public record AnimationGraphics(
    List<String> frameTextureIds,
    double frameDurationSeconds,
    boolean isLooping
) implements EntityGraphics {
}
