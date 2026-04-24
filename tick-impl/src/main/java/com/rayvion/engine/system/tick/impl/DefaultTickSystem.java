package com.rayvion.engine.system.tick.impl;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.commons.task.Task;
import com.rayvion.engine.commons.task.descriptor.TaskDescriptor;
import com.rayvion.engine.commons.task.descriptor.TaskIdentity;
import com.rayvion.engine.commons.task.pipeline.Workflow;
import com.rayvion.engine.scheduler.Schedule;
import com.rayvion.engine.scheduler.SchedulerSystem;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.Tickable;
import com.rayvion.engine.system.dependency.SystemDependency;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.tick.TickSystem;
import com.rayvion.engine.system.trait.SystemTraitRequirement;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class DefaultTickSystem implements TickSystem {
    private final SystemDescriptor descriptor;
    private final Map<Duration, TickGroup> groups = new ConcurrentHashMap<>();
    private SchedulerSystem schedulerSystem;
    private boolean initialized = false;

    public DefaultTickSystem() {
        this.descriptor = new SystemDescriptor(
                new SystemCoordinate("rayvion", "tick", Version.parse("1.0.0")),
                Set.of(
                        new SystemDependency(
                                new SystemTraitRequirement(
                                        SchedulerSystem.TRAIT.namespaceId(),
                                        SchedulerSystem.TRAIT.id(),
                                        v -> v.equals(SchedulerSystem.TRAIT.version())
                                ),
                                SystemDependency.RequirementLevel.REQUIRED
                        ),
                        new SystemDependency(
                                new SystemTraitRequirement(
                                        Tickable.TRAIT.namespaceId(),
                                        Tickable.TRAIT.id(),
                                        v -> v.equals(Tickable.TRAIT.version())
                                ),
                                SystemDependency.RequirementLevel.OPTIONAL
                        )
                ),
                Set.of()
        );
    }

    @Override
    public SystemDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void onDependencyAdded(System dependency) {
        if (dependency instanceof SchedulerSystem scheduler) {
            this.schedulerSystem = scheduler;
        } else if (dependency instanceof Tickable tickable) {
            Duration delay = tickable.getTickDelay();
            TickGroup group = groups.computeIfAbsent(delay, d -> new TickGroup(d, schedulerSystem));
            group.addSystem(tickable);
        }
    }

    @Override
    public void onDependencyRemoved(System dependency) {
        if (dependency instanceof Tickable tickable) {
            Duration delay = tickable.getTickDelay();
            TickGroup group = groups.get(delay);
            if (group != null) {
                group.removeSystem(tickable);
            }
        }
    }

    @Override
    public void init() {
        if (initialized) return;
        initialized = true;
        // The schedulerSystem is guaranteed to be present if it's a REQUIRED dependency and init() is called by SystemManager.
    }

    private static class TickGroup {
        private final Duration delay;
        private final CopyOnWriteArrayList<Tickable> systems = new CopyOnWriteArrayList<>();
        private final SchedulerSystem scheduler;
        private boolean scheduled = false;

        public TickGroup(Duration delay, SchedulerSystem scheduler) {
            this.delay = delay;
            this.scheduler = scheduler;
        }

        public synchronized void addSystem(Tickable tickable) {
            systems.add(tickable);
            if (!scheduled && scheduler != null) {
                schedule();
            }
        }

        public void removeSystem(Tickable tickable) {
            systems.remove(tickable);
        }

        private void schedule() {
            Workflow workflow = new Workflow();
            Task<Void> tickTask = new Task<>(
                    new TaskDescriptor<>(
                            new TaskIdentity(null, "tick-" + delay.toMillis()),
                            Set.of(),
                            Set.of()
                    ),
                    results -> {
                        for (Tickable system : systems) {
                            try {
                                system.tick();
                            } catch (Exception e) {
                                log.error("Error ticking system: {}", e.getMessage(), e);
                            }
                        }
                        return CompletableFuture.completedFuture(null);
                    }
            );
            workflow.addTask(tickTask);
            scheduler.schedule(workflow, Schedule.fixedDelay(Duration.ZERO, delay));
            scheduled = true;
        }
    }
}
