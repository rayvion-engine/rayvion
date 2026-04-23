package com.rayvion.engine.transform;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transform {
    private double x;
    private double y;
    private double z;
    
    // Euler angles for 3D rotation, or just rotation around Z for 2D.
    private double rotationX;
    private double rotationY;
    private double rotationZ;
    
    public Transform(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
