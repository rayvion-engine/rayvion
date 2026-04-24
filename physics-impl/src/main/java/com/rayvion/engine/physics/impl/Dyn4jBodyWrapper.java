package com.rayvion.engine.physics.impl;

import com.rayvion.engine.physics.PhysicsBody;
import lombok.Getter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;

@Getter
public class Dyn4jBodyWrapper implements PhysicsBody {
    private final Body dyn4jBody;
    private final long entityId;

    public Dyn4jBodyWrapper(Body dyn4jBody, long entityId) {
        this.dyn4jBody = dyn4jBody;
        this.entityId = entityId;
    }

    public void setMass(double mass) {
        if (!isStatic()) {
            dyn4jBody.setMass(MassType.NORMAL);
            org.dyn4j.geometry.Mass m = dyn4jBody.getMass();
            dyn4jBody.setMass(new org.dyn4j.geometry.Mass(m.getCenter(), mass, m.getInertia()));
        }
    }

    @Override
    public void applyForce(double fx, double fy) {
        dyn4jBody.applyForce(new Vector2(fx, fy));
    }

    @Override
    public void applyImpulse(double ix, double iy) {
        dyn4jBody.applyImpulse(new Vector2(ix, iy));
    }

    @Override
    public void setVelocity(double vx, double vy) {
        dyn4jBody.setLinearVelocity(new Vector2(vx, vy));
    }

    @Override
    public boolean isStatic() {
        return dyn4jBody.getMass().isInfinite();
    }
}
