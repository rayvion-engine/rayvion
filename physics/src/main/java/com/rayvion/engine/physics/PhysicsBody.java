package com.rayvion.engine.physics;

/**
 * Represents a physical body in the physics simulation.
 * <p>
 * A physics body can be dynamic (affected by forces and collisions) or static (immovable).
 * It provides methods to manipulate physical properties like mass, velocity, and rotation,
 * as well as applying forces and impulses.
 * </p>
 */
public interface PhysicsBody {
    /**
     * Sets the mass of the physics body.
     * 
     * @param mass the new mass in kilograms
     */
    void setMass(double mass);

    /**
     * Applies a force to the center of the physics body.
     * 
     * @param fx the force in the x-direction (Newtons)
     * @param fy the force in the y-direction (Newtons)
     */
    void applyForce(double fx, double fy);

    /**
     * Applies an impulse to the center of the physics body.
     * An impulse is an instantaneous change in velocity.
     * 
     * @param ix the impulse in the x-direction (Newton-seconds)
     * @param iy the impulse in the y-direction (Newton-seconds)
     */
    void applyImpulse(double ix, double iy);

    /**
     * Sets the linear velocity of the physics body.
     * 
     * @param vx the velocity in the x-direction (meters per second)
     * @param vy the velocity in the y-direction (meters per second)
     */
    void setVelocity(double vx, double vy);

    /**
     * Sets the rotation of the physics body.
     * 
     * @param radians the rotation in radians
     */
    void setRotation(double radians);

    /**
     * Sets whether the physics body has a fixed rotation.
     * If true, the body will not rotate due to forces or collisions.
     * 
     * @param fixed true to fix the rotation, false otherwise
     */
    void setFixedRotation(boolean fixed);

    /**
     * Checks if the physics body is static.
     * A static body has infinite mass and cannot be moved by forces or collisions.
     * 
     * @return true if the body is static, false otherwise
     */
    boolean isStatic();
}
