package com.rayvion.engine.system.tick;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;

import java.util.Set;

/**
 * Marker interface for the engine's tick-orchestration system.
 *
 * <p>A {@code TickSystem} is responsible for scheduling and driving the periodic
 * {@link com.rayvion.engine.system.Tickable#tick()} callbacks for every registered
 * {@link com.rayvion.engine.system.Tickable} system.  Tickable systems are grouped by
 * their requested tick delay so that all systems sharing the same period share a single
 * scheduled job, minimising overhead.
 *
 * <p>Implementations are expected to:
 * <ul>
 *   <li>Declare a {@link com.rayvion.engine.system.dependency.SystemDependency#RequirementLevel#REQUIRED
 *       REQUIRED} dependency on a {@link com.rayvion.engine.scheduler.SchedulerSystem}.</li>
 *   <li>Accept an optional dependency on any number of
 *       {@link com.rayvion.engine.system.Tickable} systems and add / remove them from
 *       their corresponding tick groups as they appear and disappear.</li>
 * </ul>
 *
 * @see com.rayvion.engine.system.Tickable
 * @see com.rayvion.engine.scheduler.SchedulerSystem
 * @see com.rayvion.engine.system.tick.impl.DefaultTickSystem
 */
public interface TickSystem extends System {
    /**
     * The trait coordinate advertised by every {@code TickSystem} implementation.
     *
     * <p>Other systems that need to locate a tick orchestrator at runtime can declare
     * an optional or required dependency on this trait instead of coupling themselves
     * to a concrete implementation class.
     */
    SystemTraitCoordinate TRAIT = new SystemTraitCoordinate("rayvion", "tick", Version.parse("1.0.0"));

    /**
     * Returns the default {@link SystemDescriptor} for a {@code TickSystem}.
     *
     * <p>The descriptor carries the system coordinate {@code rayvion:tick:1.0.0},
     * declares no dependencies (concrete implementations typically override this to
     * register their own), and exposes {@link #TRAIT} so that other systems can
     * discover the tick orchestrator by trait.
     *
     * @return a {@link SystemDescriptor} identifying this system as {@code rayvion:tick:1.0.0}
     */
    @Override
    default SystemDescriptor getDescriptor() {
        return new SystemDescriptor(
                new SystemCoordinate("rayvion", "tick", Version.parse("1.0.0")),
                Set.of(),
                Set.of(TRAIT)
        );
    }
}
