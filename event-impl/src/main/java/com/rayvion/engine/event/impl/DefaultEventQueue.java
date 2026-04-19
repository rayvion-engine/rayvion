package com.rayvion.engine.event.impl;

import com.rayvion.engine.event.Event;
import com.rayvion.engine.event.EventQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

class DefaultEventQueue<T extends Event> implements EventQueue<T> {

    private final ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();

    void offer(T event) {
        queue.offer(event);
    }

    @Override
    public List<T> pollAll() {
        List<T> events = new ArrayList<>();
        T event;
        while ((event = queue.poll()) != null) {
            events.add(event);
        }
        return events;
    }

    @Override
    public T poll() {
        return queue.poll();
    }

    @Override
    public boolean hasEvents() {
        return !queue.isEmpty();
    }
}
