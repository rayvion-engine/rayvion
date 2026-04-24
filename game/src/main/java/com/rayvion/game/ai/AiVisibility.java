package com.rayvion.game.ai;

final class AiVisibility {
    static final double DEFAULT_DETECTION_RANGE = 320.0;

    private AiVisibility() {
    }

    static boolean canDetectTarget(double dx, double dy, double detectionRange) {
        return dx * dx + dy * dy <= detectionRange * detectionRange;
    }
}
