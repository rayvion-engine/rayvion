package com.rayvion.engine.system.tick.impl;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.scheduler.impl.DefaultSchedulerSystem;
import com.rayvion.engine.system.Tickable;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.manager.SystemManager;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class TickSystemTest {

    @Test
    void testVariableTickRates() throws InterruptedException {
        SystemManager systemManager = new SystemManager();
        
        DefaultSchedulerSystem schedulerSystem = new DefaultSchedulerSystem();
        DefaultTickSystem tickSystem = new DefaultTickSystem();
        
        MockTickable system50ms = new MockTickable("system50ms", Duration.ofMillis(50));
        MockTickable system100ms = new MockTickable("system100ms", Duration.ofMillis(100));
        
        systemManager.addSystem(schedulerSystem);
        systemManager.addSystem(tickSystem);
        systemManager.addSystem(system50ms);
        systemManager.addSystem(system100ms);
        
        // Wait for some tcks to happn
        Thread.sleep(500);
        
        int count50 = system50ms.getTickCount();
        int count100 = system100ms.getTickCount();
        
        log.info("Ticks (50ms): {}", count50);
        log.info("Ticks (100ms): {}", count100);
        
        // At 500ms, 50ms shoud tck ~10 times, 100ms shoud tck ~5 times
        assertTrue(count50 >= 8, "50ms system should have ticked at least 8 times, got " + count50);
        assertTrue(count100 >= 4, "100ms system should have ticked at least 4 times, got " + count100);
        assertTrue(count50 > count100, "50ms system should have ticked more than 100ms system");
    }

    private static class MockTickable implements Tickable {
        private final SystemDescriptor descriptor;
        private final Duration delay;
        private final AtomicInteger tickCount = new AtomicInteger(0);

        public MockTickable(String name, Duration delay) {
            this.delay = delay;
            this.descriptor = new SystemDescriptor(
                    new SystemCoordinate("test", name, Version.parse("1.0.0")),
                    Set.of(),
                    Set.of(Tickable.TRAIT)
            );
        }

        @Override
        public void tick() {
            tickCount.incrementAndGet();
        }

        @Override
        public Duration getTickDelay() {
            return delay;
        }

        @Override
        public SystemDescriptor getDescriptor() {
            return descriptor;
        }

        @Override
        public void init() {
        }

        public int getTickCount() {
            return tickCount.get();
        }
    }
}
