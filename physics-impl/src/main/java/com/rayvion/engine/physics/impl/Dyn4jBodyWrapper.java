package com.rayvion.engine.physics.impl;

import com.rayvion.engine.physics.PhysicsBody;
import lombok.Getter;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;

/**
 * Implementation of {@link PhysicsBody} that wraps a Dyn4j {@link Body}.
 */
@Getter
public class Dyn4jBodyWrapper implements PhysicsBody {
    /**
     * The underlying Dyn4j body.
     */
    private final Body dyn4jBody;

    /**
     * The ID of the entity associated with this body.
     */
    private final long entityId;

    /**
     * Constructs a new Dyn4jBodyWrapper.
     * 
     * @param dyn4jBody the Dyn4j body to wrap
     * @param entityId the ID of the entity associated with this body
     */
    public Dyn4jBodyWrapper(Body dyn4jBody, long entityId) {
        this.dyn4jBody = dyn4jBody;
        this.entityId = entityId;
    }

    @Override
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
        if (vx != 0.0 || vy != 0.0) {
            dyn4jBody.setAtRest(false);
        }
    }

    @Override
    public void setRotation(double radians) {
        dyn4jBody.getTransform().setRotation(radians);
    }

    @Override
    public void setFixedRotation(boolean fixed) {
        if (fixed) {
            dyn4jBody.setMassType(MassType.FIXED_ANGULAR_VELOCITY);
        } else {
            dyn4jBody.setMassType(isStatic() ? MassType.INFINITE : MassType.NORMAL);
        }
    }

    @Override
    public boolean isStatic() {
        return dyn4jBody.getMass().isInfinite();
    }
}
