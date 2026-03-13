package com.rayvion.engine.entity;

import com.rayvion.engine.entity.exceptions.EntityAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

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
    void testCreateEntity() throws EntityAlreadyExistsException {
        UUID id = UUID.randomUUID();
        Entity entity = entitySystem.createEntity(id);

        assertNotNull(entity);
        assertEquals(id, entity.id());
        assertEquals(0, entity.eid());
    }

    @Test
    void testCreateMultipleEntities() throws EntityAlreadyExistsException {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        Entity entity1 = entitySystem.createEntity(id1);
        Entity entity2 = entitySystem.createEntity(id2);

        assertEquals(0, entity1.eid());
        assertEquals(1, entity2.eid());
        assertNotEquals(entity1.id(), entity2.id());
    }

    @Test
    void testCreateEntityWithDuplicateIdThrowsException() throws EntityAlreadyExistsException {
        UUID id = UUID.randomUUID();
        entitySystem.createEntity(id);

        EntityAlreadyExistsException exception = assertThrows(
                EntityAlreadyExistsException.class,
                () -> entitySystem.createEntity(id)
        );

        assertTrue(exception.getMessage().contains(id.toString()));
    }

    @Test
    void testRemoveEntity() throws EntityAlreadyExistsException {
        UUID id = UUID.randomUUID();
        Entity createdEntity = entitySystem.createEntity(id);

        Optional<Entity> removedEntity = entitySystem.removeEntity(id);

        assertTrue(removedEntity.isPresent());
        assertEquals(createdEntity, removedEntity.get());
    }

    @Test
    void testRemoveNonExistentEntity() {
        UUID id = UUID.randomUUID();
        Optional<Entity> removedEntity = entitySystem.removeEntity(id);

        assertFalse(removedEntity.isPresent());
    }

    @Test
    void testRemoveEntityAllowsRecreation() throws EntityAlreadyExistsException {
        UUID id = UUID.randomUUID();
        Entity firstEntity = entitySystem.createEntity(id);
        entitySystem.removeEntity(id);

        Entity secondEntity = entitySystem.createEntity(id);

        assertNotNull(secondEntity);
        assertEquals(id, secondEntity.id());
        assertNotEquals(firstEntity.eid(), secondEntity.eid());
    }

    @Test
    void testEidSerialIncrementsCorrectly() throws EntityAlreadyExistsException {
        Entity entity1 = entitySystem.createEntity(UUID.randomUUID());
        Entity entity2 = entitySystem.createEntity(UUID.randomUUID());
        Entity entity3 = entitySystem.createEntity(UUID.randomUUID());

        assertEquals(0, entity1.eid());
        assertEquals(1, entity2.eid());
        assertEquals(2, entity3.eid());
    }

    @Test
    void testEidSerialDoesNotResetAfterRemoval() throws EntityAlreadyExistsException {
        Entity entity1 = entitySystem.createEntity(UUID.randomUUID());
        entitySystem.removeEntity(entity1.id());
        Entity entity2 = entitySystem.createEntity(UUID.randomUUID());

        assertEquals(0, entity1.eid());
        assertEquals(1, entity2.eid());
    }
}
