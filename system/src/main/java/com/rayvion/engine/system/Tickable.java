package com.rayvion.engine.system;

import com.rayvion.engine.system.trait.SystemTraitCoordinate;
import com.github.zafarkhaja.semver.Version;

import java.time.Duration;

/**
 * Interface for systems that require periodic ticking.
 * A tickable system should provide the {@link #TRAIT} trait in its descriptor.
 */
public interface Tickable extends System {
    /**
     * The coordinate of the tickable trait.
     */
    SystemTraitCoordinate TRAIT = new SystemTraitCoordinate("rayvion", "tickable", Version.parse("1.0.0"));

    /**
     * Called periodically based on the returned delay from {@link #getTickDelay()}.
     */
    void tick();

    /**
     * Returns the delay between ticks for this system.
     *
     * @return the delay duration
     */
    default Duration getTickDelay() {
        return Duration.ofMillis(50); // Default 20 TPS
    }
}
