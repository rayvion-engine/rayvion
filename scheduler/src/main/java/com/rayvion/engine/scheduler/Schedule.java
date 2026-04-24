package com.rayvion.engine.scheduler;

import java.time.Duration;

/**
 * Defines the execution scheduling strategy for a {@link com.rayvion.engine.commons.task.pipeline.Workflow}.
 * <p>
 * This interface provides a set of predefined scheduling strategies to control when and how often
 * a workflow should be executed by the {@link SchedulerSystem}.
 * </p>
 */
public sealed interface Schedule permits
        Schedule.OneTime,
        Schedule.FixedRate,
        Schedule.FixedDelay {

    /**
     * Creates a schedule that executes a workflow exactly once after a specified delay.
     *
     * @param delay the duration to wait before the first and only execution
     * @return a one-time execution schedule
     */
    static Schedule once(Duration delay) {
        return new OneTime(delay);
    }

    /**
     * Creates a schedule that executes a workflow periodically at a fixed rate.
     * <p>
     * Each execution starts after the specified period has elapsed since the start of the previous execution.
     * If an execution takes longer than the period, subsequent executions may start late, but will not concurrent.
     * </p>
     *
     * @param initialDelay the duration to wait before the first execution
     * @param period the interval between the starts of successive executions
     * @return a fixed-rate periodic schedule
     */
    static Schedule fixedRate(Duration initialDelay, Duration period) {
        return new FixedRate(initialDelay, period);
    }

    /**
     * Creates a schedule that executes a workflow periodically with a fixed delay.
     * <p>
     * Each execution starts after the specified delay has elapsed since the completion of the previous execution.
     * This ensures a constant delay between the end of one execution and the start of the next.
     * </p>
     *
     * @param initialDelay the duration to wait before the first execution
     * @param delay the duration to wait between the completion of one execution and the start of the next
     * @return a fixed-delay periodic schedule
     */
    static Schedule fixedDelay(Duration initialDelay, Duration delay) {
        return new FixedDelay(initialDelay, delay);
    }

    /**
     * A schedule configuration for a one-time execution after a specified delay.
     *
     * @param delay the duration to wait before execution
     */
    record OneTime(Duration delay) implements Schedule {}

    /**
     * A schedule configuration for periodic execution at a fixed rate.
     *
     * @param initialDelay the duration to wait before the first execution
     * @param period the interval between the starts of successive executions
     */
    record FixedRate(Duration initialDelay, Duration period) implements Schedule {}

    /**
     * A schedule configuration for periodic execution with a fixed delay between completion and next start.
     *
     * @param initialDelay the duration to wait before the first execution
     * @param delay the duration to wait between executions
     */
    record FixedDelay(Duration initialDelay, Duration delay) implements Schedule {}
}
