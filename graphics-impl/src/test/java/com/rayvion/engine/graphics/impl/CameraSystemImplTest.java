package com.rayvion.engine.graphics.impl;

import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CameraSystemImplTest {

    private CameraSystemImpl cameraSystem;
    private MockTransformSystem transformSystem;

    @BeforeEach
    void setUp() {
        cameraSystem = new CameraSystemImpl();
        transformSystem = new MockTransformSystem();
    }

    @Test
    void testInit() {
        assertDoesNotThrow(() -> cameraSystem.init());
        // Should not throw anything
    }

    @Test
    void testDependencyAddition() {
        cameraSystem.onDependencyAdded(transformSystem);
        // Verify it works by checking update later
        
        cameraSystem.onDependencyAdded(new System() {
            @Override
            public SystemDescriptor getDescriptor() { return null; }
            @Override
            public void init() {}
        });
    }

    @Test
    void testTargetEntityProperties() {
        assertEquals(-1, cameraSystem.getTargetEntity());
        cameraSystem.setTargetEntity(123L);
        assertEquals(123L, cameraSystem.getTargetEntity());
    }

    @Test
    void testPositionProperties() {
        assertEquals(0, cameraSystem.getX());
        assertEquals(0, cameraSystem.getY());
        cameraSystem.setPosition(10.5, 20.7);
        assertEquals(10.5, cameraSystem.getX());
        assertEquals(20.7, cameraSystem.getY());
    }

    @Test
    void testZoomProperties() {
        assertEquals(1.0f, cameraSystem.getZoom());
        cameraSystem.setZoom(2.5f);
        assertEquals(2.5f, cameraSystem.getZoom());
    }

    @Test
    void testUpdateWithoutTarget() {
        cameraSystem.setPosition(50, 60);
        cameraSystem.update();
        assertEquals(50, cameraSystem.getX());
        assertEquals(60, cameraSystem.getY());
    }

    @Test
    void testUpdateWithoutTransformSystem() {
        cameraSystem.setTargetEntity(1L);
        cameraSystem.setPosition(50, 60);
        cameraSystem.update();
        assertEquals(50, cameraSystem.getX());
        assertEquals(60, cameraSystem.getY());
    }

    @Test
    void testUpdateWithMissingTransform() {
        cameraSystem.onDependencyAdded(transformSystem);
        cameraSystem.setTargetEntity(1L);
        cameraSystem.setPosition(50, 60);
        cameraSystem.update();
        assertEquals(50, cameraSystem.getX());
        assertEquals(60, cameraSystem.getY());
    }

    @Test
    void testUpdateSuccess() {
        cameraSystem.onDependencyAdded(transformSystem);
        cameraSystem.setTargetEntity(1L);
        transformSystem.setTransform(1L, 100, 200);
        
        cameraSystem.update();
        
        assertEquals(100, cameraSystem.getX());
        assertEquals(200, cameraSystem.getY());
    }

    @Test
    void testShake() {
        cameraSystem.setPosition(0, 0);
        cameraSystem.shake(10.0, 1000); // 1 second shake
        
        cameraSystem.update();
        
        // Since it's random, we check if it's NOT (0,0)
        assertNotEquals(0.0, cameraSystem.getX(), "X should have changed due to shake");
        assertNotEquals(0.0, cameraSystem.getY(), "Y should have changed due to shake");
    }

    @Test
    void testShakeExpired() throws InterruptedException {
        cameraSystem.setPosition(0, 0);
        cameraSystem.shake(10.0, 10); // 10ms shake
        
        Thread.sleep(50); // Wait for it to expire
        
        cameraSystem.update();
        
        assertEquals(0.0, cameraSystem.getX());
        assertEquals(0.0, cameraSystem.getY());
    }

    private static class MockTransformSystem implements TransformSystem {
        private final Map<Long, Transform> transforms = new HashMap<>();

        void setTransform(long entityId, double x, double y) {
            transforms.put(entityId, new Transform(x, y, 0));
        }

        @Override
        public void setTransform(long entityId, Transform transform) {
            transforms.put(entityId, transform);
        }

        @Override
        public Transform getTransform(long entityId) {
            return transforms.get(entityId);
        }

        @Override
        public boolean hasTransform(long entityId) {
            return transforms.containsKey(entityId);
        }

        @Override
        public boolean removeTransform(long entityId) {
            return transforms.remove(entityId) != null;
        }

        @Override
        public void init() {}

        @Override
        public void onDependencyAdded(System dependency) {}
    }
}
