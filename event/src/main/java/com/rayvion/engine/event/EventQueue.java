package com.rayvion.engine.event;

import java.util.List;

/**
 * An event queue for the pull-based subscription model.
 * Accumulates events until they are explicitly polled.
 */
public interface EventQueue<T extends Event> {

    /**
     * Retrieves and removes all accumulated events from this queue.
     *
     * @return a list of polled events, never null.
     */
    List<T> pollAll();

    /**
     * Retrieves and removes the head of this queue, or returns null if this queue is empty.
     *
     * @return the head of this queue, or null if this queue is empty.
     */
    T poll();

    /**
     * Checks if the queue has any events pending.
     *
     * @return true if there is at least one event in the queue.
     */
    boolean hasEvents();
}
