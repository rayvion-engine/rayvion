package com.rayvion.engine.system.manager;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.dependency.SystemDependency;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;
import com.rayvion.engine.system.trait.SystemTraitRequirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SystemManagerTest {
    private SystemManager systemManager;

    @BeforeEach
    void setUp() {
        systemManager = new SystemManager();
    }

    @Test
    @DisplayName("Should add a system with no dependencies")
    void testAddSystemWithNoDependencies() {
        MockSystem system = new MockSystem("test-system", Set.of(), Set.of());
        
        assertDoesNotThrow(() -> systemManager.addSystem(system));
        assertEquals(0, system.getDependencyAddedCount());
    }

    @Test
    @DisplayName("Should add multiple independent systems")
    void testAddMultipleIndependentSystems() {
        MockSystem system1 = new MockSystem("system1", Set.of(), Set.of());
        MockSystem system2 = new MockSystem("system2", Set.of(), Set.of());
        
        systemManager.addSystem(system1);
        systemManager.addSystem(system2);
        
        assertEquals(0, system1.getDependencyAddedCount());
        assertEquals(0, system2.getDependencyAddedCount());
    }

    @Test
    @DisplayName("Should create dependency when system depends on existing system's trait")
    void testAddSystemWithDependencyOnExistingSystem() {
        SystemTraitCoordinate trait = createTrait("trait", "1.0.0");
        
        MockSystem provider = new MockSystem("provider", Set.of(), Set.of(trait));
        MockSystem consumer = new MockSystem("consumer", 
            Set.of(createDependency(trait, SystemDependency.RequirementLevel.REQUIRED)), 
            Set.of());
        
        systemManager.addSystem(provider);
        systemManager.addSystem(consumer);
        
        assertEquals(1, consumer.getDependencyAddedCount());
        assertTrue(consumer.getAddedDependencies().contains(provider));
    }

    @Test
    @DisplayName("Should create dependency when existing system depends on new system's trait")
    void testAddSystemThatSatisfiesExistingSystemDependency() {
        SystemTraitCoordinate trait = createTrait("trait", "1.0.0");
        
        MockSystem consumer = new MockSystem("consumer", 
            Set.of(createDependency(trait, SystemDependency.RequirementLevel.REQUIRED)), 
            Set.of());
        MockSystem provider = new MockSystem("provider", Set.of(), Set.of(trait));
        
        systemManager.addSystem(consumer);
        systemManager.addSystem(provider);
        
        assertEquals(1, consumer.getDependencyAddedCount());
        assertEquals(0, provider.getDependencyAddedCount());
        assertTrue(consumer.getAddedDependencies().contains(provider));
    }

    @Test
    @DisplayName("Should handle optional dependencies")
    void testAddSystemWithOptionalDependency() {
        SystemTraitCoordinate trait = createTrait("trait", "1.0.0");
        
        MockSystem provider = new MockSystem("provider", Set.of(), Set.of(trait));
        MockSystem consumer = new MockSystem("consumer", 
            Set.of(createDependency(trait, SystemDependency.RequirementLevel.OPTIONAL)), 
            Set.of());
        
        systemManager.addSystem(provider);
        systemManager.addSystem(consumer);
        
        assertEquals(1, consumer.getDependencyAddedCount());
        assertTrue(consumer.getAddedDependencies().contains(provider));
    }

    @Test
    @DisplayName("Should handle multiple dependencies")
    void testAddSystemWithMultipleDependencies() {
        SystemTraitCoordinate trait1 = createTrait("trait1", "1.0.0");
        SystemTraitCoordinate trait2 = createTrait("trait2", "1.0.0");
        
        MockSystem provider1 = new MockSystem("provider1", Set.of(), Set.of(trait1));
        MockSystem provider2 = new MockSystem("provider2", Set.of(), Set.of(trait2));
        MockSystem consumer = new MockSystem("consumer", 
            Set.of(
                createDependency(trait1, SystemDependency.RequirementLevel.REQUIRED),
                createDependency(trait2, SystemDependency.RequirementLevel.REQUIRED)
            ), 
            Set.of());
        
        systemManager.addSystem(provider1);
        systemManager.addSystem(provider2);
        systemManager.addSystem(consumer);
        
        assertEquals(2, consumer.getDependencyAddedCount());
        assertTrue(consumer.getAddedDependencies().contains(provider1));
        assertTrue(consumer.getAddedDependencies().contains(provider2));
    }

    @Test
    @DisplayName("Should handle system providing multiple traits")
    void testAddSystemProvidingMultipleTraits() {
        SystemTraitCoordinate trait1 = createTrait("trait1", "1.0.0");
        SystemTraitCoordinate trait2 = createTrait("trait2", "1.0.0");
        
        MockSystem provider = new MockSystem("provider", Set.of(), Set.of(trait1, trait2));
        MockSystem consumer1 = new MockSystem("consumer1", 
            Set.of(createDependency(trait1, SystemDependency.RequirementLevel.REQUIRED)), 
            Set.of());
        MockSystem consumer2 = new MockSystem("consumer2", 
            Set.of(createDependency(trait2, SystemDependency.RequirementLevel.REQUIRED)), 
            Set.of());
        
        systemManager.addSystem(provider);
        systemManager.addSystem(consumer1);
        systemManager.addSystem(consumer2);
        
        assertEquals(1, consumer1.getDependencyAddedCount());
        assertEquals(1, consumer2.getDependencyAddedCount());
        assertEquals(0, provider.getDependencyAddedCount());
    }

    @Test
    @DisplayName("Should not create dependency for non-matching traits")
    void testAddSystemWithNonMatchingTraits() {
        SystemTraitCoordinate trait1 = createTrait("trait1", "1.0.0");
        SystemTraitCoordinate trait2 = createTrait("trait2", "1.0.0");
        
        MockSystem provider = new MockSystem("provider", Set.of(), Set.of(trait1));
        MockSystem consumer = new MockSystem("consumer", 
            Set.of(createDependency(trait2, SystemDependency.RequirementLevel.REQUIRED)), 
            Set.of());
        
        systemManager.addSystem(provider);
        systemManager.addSystem(consumer);
        
        assertEquals(0, consumer.getDependencyAddedCount());
        assertEquals(0, provider.getDependencyAddedCount());
    }

    @Test
    @DisplayName("Should handle circular dependencies")
    void testAddSystemsWithCircularDependencies() {
        SystemTraitCoordinate trait1 = createTrait("trait1", "1.0.0");
        SystemTraitCoordinate trait2 = createTrait("trait2", "1.0.0");
        
        MockSystem system1 = new MockSystem("system1", 
            Set.of(createDependency(trait2, SystemDependency.RequirementLevel.OPTIONAL)), 
            Set.of(trait1));
        MockSystem system2 = new MockSystem("system2", 
            Set.of(createDependency(trait1, SystemDependency.RequirementLevel.OPTIONAL)), 
            Set.of(trait2));
        
        systemManager.addSystem(system1);
        systemManager.addSystem(system2);
        
        assertEquals(1, system1.getDependencyAddedCount());
        assertEquals(1, system2.getDependencyAddedCount());
        assertTrue(system1.getAddedDependencies().contains(system2));
        assertTrue(system2.getAddedDependencies().contains(system1));
    }

    // Helper methods
    @SuppressWarnings("SameParameterValue")
    private SystemTraitCoordinate createTrait(String name, String version) {
        return new SystemTraitCoordinate(name, Version.parse(version));
    }

    private SystemDependency createDependency(SystemTraitCoordinate trait, SystemDependency.RequirementLevel level) {
        SystemTraitRequirement requirement = new SystemTraitRequirement(trait.id(), v -> v.equals(trait.version()));
        return new SystemDependency(requirement, level);
    }

    // Mock System implementation for testing
    private static class MockSystem implements System {
        private final SystemDescriptor descriptor;
        private final Set<System> addedDependencies = new HashSet<>();
        private int dependencyAddedCount = 0;

        public MockSystem(String name, Set<SystemDependency> dependencies, Set<SystemTraitCoordinate> provides) {
            this.descriptor = new SystemDescriptor(
                new SystemCoordinate(name, Version.parse("1.0.0")),
                dependencies,
                provides
            );
        }

        @Override
        public SystemDescriptor getDescriptor() {
            return descriptor;
        }

        @Override
        public void onDependencyAdded(System dependency) {
            addedDependencies.add(dependency);
            dependencyAddedCount++;
        }

        @Override
        public void init() {
            // Not used in these tests
        }

        public int getDependencyAddedCount() {
            return dependencyAddedCount;
        }

        public Set<System> getAddedDependencies() {
            return addedDependencies;
        }
    }
}
