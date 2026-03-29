package eth.likespro.cyberpunkeb.core;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Camera {
    public float x;
    public float y;
    public float zoom = 1.0f;

    public Camera(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
