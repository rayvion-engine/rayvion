package com.rayvion.engine.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntitySystemImplTest {
    private EntitySystemImpl entitySystem;

    @BeforeEach
    void setUp() {
        entitySystem = new EntitySystemImpl();
    }

    @Test
    void testInit() {
        assertDoesNotThrow(() -> entitySystem.init());
    }

    @Test
    void testCreateEntity() {
        Entity entity = entitySystem.createEntity();

        assertNotNull(entity);
        assertEquals(0L, entity.id());
        assertTrue(entitySystem.hasEntity(entity.id()));
    }

    @Test
    void testCreateMultipleEntities() {
        Entity entity1 = entitySystem.createEntity();
        Entity entity2 = entitySystem.createEntity();

        assertEquals(0L, entity1.id());
        assertEquals(1L, entity2.id());
        assertNotEquals(entity1.id(), entity2.id());
    }

    @Test
    void testRemoveEntity() {
        Entity createdEntity = entitySystem.createEntity();

        boolean removed = entitySystem.removeEntity(createdEntity.id());

        assertTrue(removed);
        assertFalse(entitySystem.hasEntity(createdEntity.id()));
    }

    @Test
    void testRemoveNonExistentEntity() {
        boolean removed = entitySystem.removeEntity(999L);
        assertFalse(removed);
    }

    @Test
    void testIdIncrementsCorrectly() {
        Entity entity1 = entitySystem.createEntity();
        Entity entity2 = entitySystem.createEntity();
        Entity entity3 = entitySystem.createEntity();

        assertEquals(0L, entity1.id());
        assertEquals(1L, entity2.id());
        assertEquals(2L, entity3.id());
    }

    @Test
    void testGetEntities() {
        Entity e1 = entitySystem.createEntity();
        Entity e2 = entitySystem.createEntity();
        
        var entities = entitySystem.getEntities();
        assertEquals(2, entities.size());
        assertTrue(entities.contains(e1));
        assertTrue(entities.contains(e2));
    }
}
