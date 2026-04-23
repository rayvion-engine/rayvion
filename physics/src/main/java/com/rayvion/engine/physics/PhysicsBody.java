package com.rayvion.engine.physics;

public interface PhysicsBody {
    void setMass(double mass);
    void applyForce(double fx, double fy);
    void applyImpulse(double ix, double iy);
    void setVelocity(double vx, double vy);
    boolean isStatic();
}
