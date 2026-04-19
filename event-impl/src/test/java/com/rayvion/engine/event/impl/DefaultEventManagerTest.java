package com.rayvion.engine.event.impl;

import com.rayvion.engine.event.Event;
import com.rayvion.engine.event.EventQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DefaultEventManagerTest {

    private DefaultEventManager eventManager;

    interface UserEvent extends Event { }
    static class UserCreatedEvent implements UserEvent { }
    static class OtherEvent implements Event { }

    @BeforeEach
    void setUp() {
        eventManager = new DefaultEventManager();
    }

    @Test
    void testUnregisteredEventTypeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> eventManager.publish(new OtherEvent()));
    }

    @Test
    void testBasicPushSubscription() {
        eventManager.registerEventType(OtherEvent.class);

        AtomicInteger count = new AtomicInteger(0);
        eventManager.subscribe(OtherEvent.class, e -> count.incrementAndGet());

        eventManager.publish(new OtherEvent());
        eventManager.publish(new OtherEvent());

        assertEquals(2, count.get());
    }

    @Test
    void testBasicPullSubscription() {
        eventManager.registerEventType(OtherEvent.class);

        EventQueue<OtherEvent> queue = eventManager.subscribePull(OtherEvent.class);
        
        eventManager.publish(new OtherEvent());
        eventManager.publish(new OtherEvent());

        assertTrue(queue.hasEvents());
        List<OtherEvent> events = queue.pollAll();
        assertEquals(2, events.size());
        assertFalse(queue.hasEvents());
    }

    @Test
    void testMultiplePullSubscribersGetSeparateQueues() {
        eventManager.registerEventType(OtherEvent.class);

        EventQueue<OtherEvent> queue1 = eventManager.subscribePull(OtherEvent.class);
        EventQueue<OtherEvent> queue2 = eventManager.subscribePull(OtherEvent.class);

        eventManager.publish(new OtherEvent());

        assertEquals(1, queue1.pollAll().size());
        assertEquals(1, queue2.pollAll().size());
    }

    @Test
    void testPolymorphicSubscription() {
        eventManager.registerEventType(UserCreatedEvent.class);

        AtomicInteger baseEventCount = new AtomicInteger(0);
        AtomicInteger userEventCount = new AtomicInteger(0);
        AtomicInteger specificEventCount = new AtomicInteger(0);

        eventManager.subscribe(Event.class, e -> baseEventCount.incrementAndGet());
        eventManager.subscribe(UserEvent.class, e -> userEventCount.incrementAndGet());
        eventManager.subscribe(UserCreatedEvent.class, e -> specificEventCount.incrementAndGet());

        eventManager.publish(new UserCreatedEvent());

        assertEquals(1, baseEventCount.get());
        assertEquals(1, userEventCount.get());
        assertEquals(1, specificEventCount.get());
    }

    @Test
    void testExceptionInPushSubscriberDoesNotHaltOthers() {
        eventManager.registerEventType(OtherEvent.class);

        AtomicInteger count = new AtomicInteger(0);

        eventManager.subscribe(OtherEvent.class, e -> { throw new RuntimeException("Test Exception"); });
        eventManager.subscribe(OtherEvent.class, e -> count.incrementAndGet());

        eventManager.publish(new OtherEvent());

        // The second subscriber should still be called despite the first one throwing an exception
        assertEquals(1, count.get());
    }
}
