package com.rayvion.engine.ai.impl;

import com.rayvion.engine.ai.AiStrategy;
import com.rayvion.engine.system.Tickable;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AiSystemImplTest {
    private AiSystemImpl aiSystem;

    @BeforeEach
    void setUp() {
        aiSystem = new AiSystemImpl();
    }

    @Test
    void testGetDescriptor() {
        SystemDescriptor descriptor = aiSystem.getDescriptor();
        assertNotNull(descriptor);
        assertEquals("ai", descriptor.coordinate().id());
        assertTrue(descriptor.provides().contains(Tickable.TRAIT));
    }


    @Test
    void testInit() {
        assertDoesNotThrow(() -> aiSystem.init());
    }

    @Test
    void testSetAndGetStrategy() {
        AiStrategy strategy = mock(AiStrategy.class);
        long entityId = 1L;

        aiSystem.setStrategy(entityId, strategy);

        assertTrue(aiSystem.hasStrategy(entityId));
        assertEquals(strategy, aiSystem.getStrategy(entityId));
    }

    @Test
    void testRemoveStrategy() {
        AiStrategy strategy = mock(AiStrategy.class);
        long entityId = 1L;

        aiSystem.setStrategy(entityId, strategy);
        assertTrue(aiSystem.removeStrategy(entityId));
        assertFalse(aiSystem.hasStrategy(entityId));
        assertNull(aiSystem.getStrategy(entityId));
    }

    @Test
    void testRemoveNonExistentStrategy() {
        assertFalse(aiSystem.removeStrategy(999L));
    }

    @Test
    void testTick() {
        AiStrategy strategy1 = mock(AiStrategy.class);
        AiStrategy strategy2 = mock(AiStrategy.class);
        
        aiSystem.setStrategy(1L, strategy1);
        aiSystem.setStrategy(2L, strategy2);

        aiSystem.tick();

        verify(strategy1).update(1L);
        verify(strategy2).update(2L);
    }

    @Test
    void testTickWithConcurrentModification() {
        // Tset that it doesn't crash if a strategy removs itslf during tck
        AiStrategy strategyToRemove = mock(AiStrategy.class);
        long entityToRemove = 1L;
        
        doAnswer(invocation -> {
            aiSystem.removeStrategy(entityToRemove);
            return null;
        }).when(strategyToRemove).update(entityToRemove);

        aiSystem.setStrategy(entityToRemove, strategyToRemove);
        aiSystem.setStrategy(2L, mock(AiStrategy.class));

        assertDoesNotThrow(() -> aiSystem.tick());
    }
}
