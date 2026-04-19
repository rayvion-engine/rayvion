package com.rayvion.engine.event;

import java.util.function.Consumer;

/**
 * Central event manager or bus system.
 * Supports event type registration, event publishing, and subscription handling.
 */
public interface EventManager {

    /**
     * Registers an event type.
     * Only registered types can be published. 
     * Subscribers to super-types (for polymorphism) don't strictly need their super-type registered,
     * but the exact leaf event type being published must be registered.
     *
     * @param eventType the event class
     * @param <T>       the event type
     */
    <T extends Event> void registerEventType(Class<T> eventType);

    /**
     * Publishes an event to all push subscribers and push it into all pull queues.
     * This supports polymorphic delivery (subscribers to superclasses/interfaces of T will also receive T).
     * Catching exceptions from push subscribers is highly recommended internally so 
     * one failing subscriber does not halt the delivery to remaining subscribers.
     *
     * @param event the event to publish
     * @param <T>   the event type
     * @throws IllegalArgumentException if the event's class was not registered
     */
    <T extends Event> void publish(T event);

    /**
     * Subscribes using the push model.
     * The consumer will be invoked whenever an event matching `eventType` (or its subtypes) is published.
     *
     * @param eventType  the event type to subscribe to
     * @param subscriber the callback to invoke
     * @param <T>        the event type
     */
    <T extends Event> void subscribe(Class<T> eventType, Consumer<T> subscriber);

    /**
     * Subscribes using the pull model.
     * A unique, independent queue is returned to the caller, which will accumulate events matching `eventType`
     * (or its subtypes) until polled.
     *
     * @param eventType the event type to subscribe to
     * @param <T>       the event type
     * @return a new queue accumulating events for this subscriber
     */
    <T extends Event> EventQueue<T> subscribePull(Class<T> eventType);
}
