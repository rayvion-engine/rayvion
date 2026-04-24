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

/**
 * Default implementation of {@link TickSystem}.
 *
 * <p>{@code DefaultTickSystem} groups {@link Tickable} systems by their requested
 * tick period ({@link Tickable#getTickDelay()}) and submits a single recurring
 * {@link com.rayvion.engine.scheduler.Schedule#fixedDelay fixed-delay} workflow
 * per group to the engine's {@link SchedulerSystem}.  This approach avoids
 * spawning one scheduler entry per tickable system and keeps CPU overhead
 * proportional to the number of distinct tick rates rather than the total number
 * of tickable systems.
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>The {@link com.rayvion.engine.system.manager.SystemManager} injects the
 *       required {@link SchedulerSystem} via
 *       {@link #onDependencyAdded(com.rayvion.engine.system.System)}.</li>
 *   <li>Optional {@link Tickable} dependencies are also supplied through
 *       {@link #onDependencyAdded(com.rayvion.engine.system.System)}, which
 *       assigns them to an existing or newly created {@link TickGroup}.</li>
 *   <li>{@link #init()} is called once by the system manager to signal that all
 *       required dependencies are available; it is idempotent.</li>
 *   <li>When a {@link Tickable} is removed, it is unregistered from its group
 *       via {@link #onDependencyRemoved(com.rayvion.engine.system.System)}.</li>
 * </ol>
 *
 * <h2>Thread safety</h2>
 * <p>The {@code groups} map is backed by a {@link ConcurrentHashMap} and each
 * {@link TickGroup}'s system list uses a {@link CopyOnWriteArrayList}, so adding
 * and removing tickables while the scheduler is already firing is safe.
 *
 * @see TickSystem
 * @see Tickable
 * @see SchedulerSystem
 */
@Slf4j
public class DefaultTickSystem implements TickSystem {
    /** Cached system descriptor built once in the constructor. */
    private final SystemDescriptor descriptor;

    /**
     * Map from tick delay to the {@link TickGroup} that owns all tickable systems
     * sharing that period.  Keyed by the exact {@link Duration} returned by
     * {@link Tickable#getTickDelay()}.
     */
    private final Map<Duration, TickGroup> groups = new ConcurrentHashMap<>();

    /**
     * The required scheduler dependency, injected when the system manager
     * calls {@link #onDependencyAdded(com.rayvion.engine.system.System)}.
     * Will be {@code null} until that callback is received.
     */
    private SchedulerSystem schedulerSystem;

    /** Guards against re-entrant or duplicate calls to {@link #init()}. */
    private boolean initialized = false;

    /**
     * Constructs a {@code DefaultTickSystem} with a fully wired
     * {@link SystemDescriptor}.
     *
     * <p>The descriptor advertises:
     * <ul>
     *   <li>A <em>required</em> dependency on the
     *       {@link SchedulerSystem#TRAIT scheduler} trait – the tick system
     *       cannot operate without a scheduler.</li>
     *   <li>An <em>optional</em> dependency on the
     *       {@link Tickable#TRAIT tickable} trait – zero or more tickable
     *       systems may be registered at any time.</li>
     *   <li>The {@link TickSystem#TRAIT} trait, making this system discoverable
     *       by others that depend on a tick orchestrator.</li>
     * </ul>
     */
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
                Set.of(TickSystem.TRAIT)
        );
    }

    /**
     * {@inheritDoc}
     *
     * @return the {@link SystemDescriptor} built during construction, never {@code null}
     */
    @Override
    public SystemDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Handles the injection of a resolved dependency.
     *
     * <ul>
     *   <li>If the dependency is a {@link SchedulerSystem}, it is stored for later
     *       use by {@link TickGroup#schedule()} calls.  This callback is guaranteed
     *       to be invoked before any {@link Tickable} is added because the scheduler
     *       is a {@code REQUIRED} dependency.</li>
     *   <li>If the dependency is a {@link Tickable}, it is logged and assigned to the
     *       {@link TickGroup} whose delay matches {@link Tickable#getTickDelay()}.  If
     *       no group exists for that delay, a new one is created and immediately
     *       scheduled with the current {@link #schedulerSystem}.</li>
     * </ul>
     *
     * @param dependency the newly resolved dependency; never {@code null}
     */
    @Override
    public void onDependencyAdded(System dependency) {
        if (dependency instanceof SchedulerSystem scheduler) {
            this.schedulerSystem = scheduler;
        } else if (dependency instanceof Tickable tickable) {
            log.info("TickSystem: Found Tickable dependency: {}", dependency.getDescriptor().coordinate().id());
            Duration delay = tickable.getTickDelay();
            TickGroup group = groups.computeIfAbsent(delay, d -> new TickGroup(d, schedulerSystem));
            group.addSystem(tickable);
        }
    }

    /**
     * Handles the removal of a previously injected dependency.
     *
     * <p>Only {@link Tickable} dependencies are handled here.  If the removed
     * system was registered in a {@link TickGroup}, it is unregistered so that
     * its {@link Tickable#tick()} is no longer called.  The group itself is
     * <em>not</em> cancelled from the scheduler even if it becomes empty, so
     * future tickables with the same delay can reuse the existing schedule slot.
     *
     * @param dependency the dependency being removed; never {@code null}
     */
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

    /**
     * Initialises the {@code DefaultTickSystem}.
     *
     * <p>This method is idempotent: subsequent calls after the first are silently
     * ignored.  By the time {@code init()} is invoked by the
     * {@link com.rayvion.engine.system.manager.SystemManager}, the
     * {@link SchedulerSystem} is guaranteed to have been injected via
     * {@link #onDependencyAdded(System)} because it is a
     * {@link com.rayvion.engine.system.dependency.SystemDependency.RequirementLevel#REQUIRED
     * REQUIRED} dependency.
     */
    @Override
    public void init() {
        if (initialized) return;
        initialized = true;
        // The schedulerSystem is guaranteed to be present if it's a REQUIRED dependency and init() is called by SystemManager.
    }

    /**
     * Groups {@link Tickable} systems that share the same tick period and
     * manages the single recurring scheduler entry that drives them.
     *
     * <p>The first call to {@link #addSystem(Tickable)} triggers
     * {@link #schedule()}, which submits a fixed-delay workflow to the
     * {@link SchedulerSystem}.  Subsequent systems added to the same group are
     * simply appended to the {@link CopyOnWriteArrayList} and will be picked up
     * by the next scheduled invocation of the workflow without requiring a new
     * scheduler entry.
     */
    private static class TickGroup {
        /** The period shared by every {@link Tickable} in this group. */
        private final Duration delay;

        /**
         * Thread-safe list of tickable systems.  {@link CopyOnWriteArrayList} is
         * chosen so that the scheduler thread can iterate without locking while
         * concurrent add / remove calls are in progress.
         */
        private final CopyOnWriteArrayList<Tickable> systems = new CopyOnWriteArrayList<>();

        /** The scheduler used to submit the recurring workflow. */
        private final SchedulerSystem scheduler;

        /** {@code true} once {@link #schedule()} has been called. */
        private boolean scheduled = false;

        /**
         * Constructs a {@code TickGroup} for the given period.
         *
         * @param delay     the tick period shared by all systems in this group;
         *                  must not be {@code null}
         * @param scheduler the scheduler that will drive this group's workflow;
         *                  may be {@code null} if the scheduler has not yet been
         *                  injected (scheduling will be deferred)
         */
        public TickGroup(Duration delay, SchedulerSystem scheduler) {
            this.delay = delay;
            this.scheduler = scheduler;
        }

        /**
         * Registers a {@link Tickable} system with this group.
         *
         * <p>If this is the first system to be added <em>and</em> a
         * {@link SchedulerSystem} is available, the recurring workflow is
         * submitted to the scheduler immediately.  This method is
         * {@code synchronized} to prevent a race between the first-add check
         * and the scheduling call.
         *
         * @param tickable the system to register; must not be {@code null}
         */
        public synchronized void addSystem(Tickable tickable) {
            systems.add(tickable);
            if (!scheduled && scheduler != null) {
                schedule();
            }
        }

        /**
         * Unregisters a {@link Tickable} system from this group.
         *
         * <p>The system will no longer receive {@link Tickable#tick()} calls
         * after this method returns.  The underlying scheduler entry is
         * <em>not</em> cancelled, so the group remains ready to accept new
         * systems in the future.
         *
         * @param tickable the system to unregister; must not be {@code null}
         */
        public void removeSystem(Tickable tickable) {
            systems.remove(tickable);
        }

        /**
         * Submits the tick workflow to the {@link SchedulerSystem}.
         *
         * <p>Creates a single {@link Task} named {@code tick-<delayMs>} that
         * iterates over every registered {@link Tickable} and calls
         * {@link Tickable#tick()}.  Exceptions thrown by individual systems are
         * caught and logged so that a misbehaving system cannot disrupt the
         * remaining ones in the group.  The workflow is scheduled with a
         * {@link Schedule#fixedDelay(Duration, Duration) fixed delay} of
         * {@link #delay} starting immediately (zero initial delay).
         */
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
