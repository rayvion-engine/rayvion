package com.rayvion.engine.transform;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the spatial state of an entity in the game world, encapsulating
 * its position and orientation.
 *
 * <p>Position is expressed as a 3-D coordinate ({@code x}, {@code y}, {@code z}).
 * Orientation is expressed as Euler angles around each axis ({@code rotationX},
 * {@code rotationY}, {@code rotationZ}). For purely 2-D games only
 * {@code z} and {@code rotationZ} are typically used, while the remaining
 * components stay at their default value of {@code 0.0}.
 *
 * <p>Instances are mutable by design so that systems such as
 * {@link TransformSystem} can update an entity's position and rotation
 * in-place without the overhead of object allocation each tick.
 *
 * <p>Lombok's {@code @Data} annotation generates {@code equals}, {@code hashCode},
 * {@code toString}, and accessor/mutator methods for every field.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transform {
    /** The position of the entity along the X axis (horizontal). */
    private double x;

    /** The position of the entity along the Y axis (vertical in 2-D, depth in 3-D). */
    private double y;

    /** The position of the entity along the Z axis (depth in 3-D, layer in 2-D). */
    private double z;
    
    /**
     * Rotation around the X axis in degrees (pitch).
     * Unused in purely 2-D contexts; defaults to {@code 0.0}.
     */
    private double rotationX;

    /**
     * Rotation around the Y axis in degrees (yaw).
     * Unused in purely 2-D contexts; defaults to {@code 0.0}.
     */
    private double rotationY;

    /**
     * Rotation around the Z axis in degrees (roll / 2-D rotation).
     * This is the primary rotation component used in 2-D games.
     */
    private double rotationZ;
    
    /**
     * Constructs a {@code Transform} with the given position and all rotation
     * components initialised to {@code 0.0}.
     *
     * @param x the initial X position
     * @param y the initial Y position
     * @param z the initial Z position
     */
    public Transform(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
