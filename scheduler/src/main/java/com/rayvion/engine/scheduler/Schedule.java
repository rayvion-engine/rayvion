package com.rayvion.engine.scheduler;

import java.time.Duration;

/**
 * Defines the execution scheduling strategy for a Workflow.
 */
public sealed interface Schedule permits
        Schedule.OneTime,
        Schedule.FixedRate,
        Schedule.FixedDelay {

    static Schedule once(Duration delay) {
        return new OneTime(delay);
    }

    static Schedule fixedRate(Duration initialDelay, Duration period) {
        return new FixedRate(initialDelay, period);
    }

    static Schedule fixedDelay(Duration initialDelay, Duration delay) {
        return new FixedDelay(initialDelay, delay);
    }

    /**
     * Schedule a workflow to run exactly once after a specified delay.
     */
    record OneTime(Duration delay) implements Schedule {}

    /**
     * Schedule a workflow to run periodically at a fixed rate.
     * Each execution starts after the specified period has elapsed since the start of the previous execution.
     */
    record FixedRate(Duration initialDelay, Duration period) implements Schedule {}

    /**
     * Schedule a workflow to run periodically with a fixed delay.
     * Each execution starts after the specified delay has elapsed since the completion of the previous execution.
     */
    record FixedDelay(Duration initialDelay, Duration delay) implements Schedule {}
}
