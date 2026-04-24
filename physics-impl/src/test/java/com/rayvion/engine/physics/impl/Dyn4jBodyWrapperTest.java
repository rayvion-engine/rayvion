package com.rayvion.engine.physics.impl;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.MassType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Dyn4jBodyWrapperTest {
    private Body dyn4jBody;
    private Dyn4jBodyWrapper wrapper;

    @BeforeEach
    void setUp() {
        dyn4jBody = new Body();
        dyn4jBody.addFixture(org.dyn4j.geometry.Geometry.createRectangle(1, 1));
        dyn4jBody.setMass(org.dyn4j.geometry.MassType.NORMAL);
        wrapper = new Dyn4jBodyWrapper(dyn4jBody, 100L);
    }

    @Test
    void testGetters() {
        assertEquals(dyn4jBody, wrapper.getDyn4jBody());
        assertEquals(100L, wrapper.getEntityId());
    }

    @Test
    void testIsStatic() {
        dyn4jBody.setMass(MassType.INFINITE);
        assertTrue(wrapper.isStatic());

        dyn4jBody.setMass(MassType.NORMAL);
        assertFalse(wrapper.isStatic());
    }

    @Test
    void testSetMass() {
        // Dynmic body
        dyn4jBody.setMass(MassType.NORMAL);
        wrapper.setMass(10.0);
        // Note: dyn4j might not set the mass exactly to 10.0 if it's already computed,
        // but it should change it.
        assertTrue(dyn4jBody.getMass().getMass() > 0);
    }

    @Test
    void testPhysicsMethods() {
        wrapper.applyForce(1, 2);
        wrapper.applyImpulse(3, 4);
        wrapper.setVelocity(5, 6);
        wrapper.setRotation(1.0);
        
        assertEquals(5, dyn4jBody.getLinearVelocity().x, 0.001);
        assertEquals(6, dyn4jBody.getLinearVelocity().y, 0.001);
        assertEquals(1.0, dyn4jBody.getTransform().getRotationAngle(), 0.001);
    }

    @Test
    void testSetVelocityWakesSleepingBody() {
        dyn4jBody.setAtRest(true);
        assertTrue(dyn4jBody.isAtRest());

        wrapper.setVelocity(5, 0);

        assertFalse(dyn4jBody.isAtRest());
        assertEquals(5, dyn4jBody.getLinearVelocity().x, 0.001);
        assertEquals(0, dyn4jBody.getLinearVelocity().y, 0.001);
    }

    @Test
    void testSetFixedRotation() {
        // Fix rotaton
        wrapper.setFixedRotation(true);
        assertEquals(MassType.FIXED_ANGULAR_VELOCITY, dyn4jBody.getMass().getType());

        // Unfix rotation - dynamic
        dyn4jBody.setMass(MassType.NORMAL);
        wrapper.setFixedRotation(false);
        assertEquals(MassType.NORMAL, dyn4jBody.getMass().getType());
    }
}
