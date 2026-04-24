package com.rayvion.engine.graphics.impl;

import com.rayvion.engine.graphics.EntityGraphics;
import com.rayvion.engine.graphics.TextureGraphics;
import com.rayvion.engine.graphics.TiledWorldGraphics;
import com.rayvion.engine.graphics.WorldGraphics;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GraphicsSystemImplTest {

    private GraphicsSystemImpl graphicsSystem;
    private MockTransformSystem transformSystem;

    @BeforeEach
    void setUp() {
        graphicsSystem = new GraphicsSystemImpl();
        transformSystem = new MockTransformSystem();
        graphicsSystem.onDependencyAdded(transformSystem);
    }

    @Test
    void testInit() {
        assertDoesNotThrow(() -> graphicsSystem.init());
    }

    @Test
    void testOnDependencyAdded() {
        GraphicsSystemImpl newSystem = new GraphicsSystemImpl();
        MockTransformSystem ts = new MockTransformSystem();
        newSystem.onDependencyAdded(ts);
        
        // To verify it was added, we can try to set graphics which requires transformSystem
        ts.setHasTransform(1L, true);
        assertDoesNotThrow(() -> newSystem.setEntityGraphics(1L, new TextureGraphics("test")));

        // Tst with unrelatd dependency
        newSystem.onDependencyAdded(new com.rayvion.engine.system.System() {
            @Override
            public com.rayvion.engine.system.descriptor.SystemDescriptor getDescriptor() { return null; }
            @Override
            public void init() {}
            @Override
            public void onDependencyAdded(com.rayvion.engine.system.System dependency) {}
        });
        // No excepton shoud occur, and it shoudn't break existng transformSystem
        assertTrue(newSystem.hasEntityGraphics(1L));
    }

    @Test
    void testSetEntityGraphics_Success() {
        long entityId = 1L;
        EntityGraphics graphics = new TextureGraphics("tex");
        transformSystem.setHasTransform(entityId, true);

        graphicsSystem.setEntityGraphics(entityId, graphics);
        assertTrue(graphicsSystem.hasEntityGraphics(entityId));
        assertEquals(graphics, graphicsSystem.getEntityGraphics(entityId));
    }

    @Test
    void testSetEntityGraphics_NoTransformSystem() {
        GraphicsSystemImpl newSystem = new GraphicsSystemImpl();
        assertThrows(IllegalStateException.class, () -> newSystem.setEntityGraphics(1L, new TextureGraphics("tex")));
    }

    @Test
    void testSetEntityGraphics_NoEntityTransform() {
        long entityId = 1L;
        transformSystem.setHasTransform(entityId, false);
        assertThrows(IllegalStateException.class, () -> graphicsSystem.setEntityGraphics(entityId, new TextureGraphics("tex")));
    }

    @Test
    void testGetEntityGraphics() {
        long entityId = 1L;
        assertNull(graphicsSystem.getEntityGraphics(entityId));

        transformSystem.setHasTransform(entityId, true);
        EntityGraphics graphics = new TextureGraphics("tex");
        graphicsSystem.setEntityGraphics(entityId, graphics);
        assertEquals(graphics, graphicsSystem.getEntityGraphics(entityId));
    }

    @Test
    void testHasEntityGraphics() {
        long entityId = 1L;
        assertFalse(graphicsSystem.hasEntityGraphics(entityId));

        transformSystem.setHasTransform(entityId, true);
        graphicsSystem.setEntityGraphics(entityId, new TextureGraphics("tex"));
        assertTrue(graphicsSystem.hasEntityGraphics(entityId));
    }

    @Test
    void testRemoveEntityGraphics() {
        long entityId = 1L;
        assertFalse(graphicsSystem.removeEntityGraphics(entityId));

        transformSystem.setHasTransform(entityId, true);
        graphicsSystem.setEntityGraphics(entityId, new TextureGraphics("tex"));
        assertTrue(graphicsSystem.removeEntityGraphics(entityId));
        assertFalse(graphicsSystem.hasEntityGraphics(entityId));
    }

    @Test
    void testGetEntitiesWithGraphics() {
        long entity1 = 1L;
        long entity2 = 2L;
        EntityGraphics graphics1 = new TextureGraphics("tex1");
        EntityGraphics graphics2 = new TextureGraphics("tex2");

        transformSystem.setHasTransform(entity1, true);
        transformSystem.setHasTransform(entity2, true);

        graphicsSystem.setEntityGraphics(entity1, graphics1);
        graphicsSystem.setEntityGraphics(entity2, graphics2);

        Set<Long> entities = graphicsSystem.getEntitiesWithGraphics();
        assertEquals(2, entities.size());
        assertTrue(entities.contains(entity1));
        assertTrue(entities.contains(entity2));

        graphicsSystem.removeEntityGraphics(entity1);
        entities = graphicsSystem.getEntitiesWithGraphics();
        assertEquals(1, entities.size());
        assertFalse(entities.contains(entity1));
        assertTrue(entities.contains(entity2));
    }

    @Test
    void testWorldGraphics() {
        assertNull(graphicsSystem.getWorldGraphics());
        WorldGraphics worldGraphics = new TiledWorldGraphics(1, 1, 1.0, new String[][]{{"tile"}});
        graphicsSystem.setWorldGraphics(worldGraphics);
        assertEquals(worldGraphics, graphicsSystem.getWorldGraphics());
    }

    @Test
    void testHealthBarVisibility() {
        long entityId = 1L;
        assertFalse(graphicsSystem.isHealthBarVisible(entityId));

        graphicsSystem.setHealthBarVisible(entityId, true);
        assertTrue(graphicsSystem.isHealthBarVisible(entityId));

        graphicsSystem.setHealthBarVisible(entityId, false);
        assertFalse(graphicsSystem.isHealthBarVisible(entityId));
    }

    @Test
    void testInteractionPrompts() {
        long entityId = 1L;
        assertNull(graphicsSystem.getInteractionPrompt(entityId));

        String prompt = "Press E to interact";
        graphicsSystem.setInteractionPrompt(entityId, prompt);
        assertEquals(prompt, graphicsSystem.getInteractionPrompt(entityId));

        graphicsSystem.removeInteractionPrompt(entityId);
        assertNull(graphicsSystem.getInteractionPrompt(entityId));
    }

    private static class MockTransformSystem implements TransformSystem {
        private final java.util.Set<Long> transforms = new java.util.HashSet<>();

        void setHasTransform(long id, boolean has) {
            if (has) transforms.add(id);
            else transforms.remove(id);
        }

        @Override
        public com.rayvion.engine.system.descriptor.SystemDescriptor getDescriptor() { return null; }

        @Override
        public void setTransform(long entityId, Transform transform) {}

        @Override
        public Transform getTransform(long entityId) { return null; }

        @Override
        public boolean hasTransform(long entityId) {
            return transforms.contains(entityId);
        }

        @Override
        public boolean removeTransform(long entityId) { return false; }

        @Override
        public void init() {}

        @Override
        public void onDependencyAdded(com.rayvion.engine.system.System dependency) {}
    }
}
