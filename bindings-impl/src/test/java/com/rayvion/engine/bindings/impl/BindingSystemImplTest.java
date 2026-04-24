package com.rayvion.engine.bindings.impl;

import com.rayvion.engine.bindings.BindingEvent;
import com.rayvion.engine.bindings.BindingGroup;
import com.rayvion.engine.bindings.BindingParameter;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.event.impl.DefaultEventManager;
import com.rayvion.engine.input.KeyEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BindingSystemImplTest {
    private EventManager eventManager;
    private BindingSystemImpl bindingSystem;

    @BeforeEach
    void setUp() {
        eventManager = new DefaultEventManager();
        bindingSystem = new BindingSystemImpl(eventManager);
        bindingSystem.init();
    }

    @Test
    void testBindingEventEmission() {
        BindingGroup movement = bindingSystem.createGroup("Movement");
        BindingParameter forward = bindingSystem.createParameter(movement, "Forward");
        bindingSystem.addBinding(forward, 10); // Dummy keycode

        List<BindingEvent> events = new ArrayList<>();
        eventManager.subscribe(BindingEvent.class, events::add);

        // Publish raw key event
        eventManager.registerEventType(KeyEvent.class);
        eventManager.publish(new KeyEvent(10, KeyEvent.Type.KEY_DOWN));

        assertEquals(1, events.size());
        assertEquals(forward, events.get(0).parameter());
        assertEquals(KeyEvent.Type.KEY_DOWN, events.get(0).type());
    }

    @Test
    void testMultipleBindings() {
        BindingGroup movement = bindingSystem.createGroup("Movement");
        BindingParameter forward = bindingSystem.createParameter(movement, "Forward");
        bindingSystem.addBinding(forward, 10);
        bindingSystem.addBinding(forward, 11);

        List<BindingEvent> events = new ArrayList<>();
        eventManager.subscribe(BindingEvent.class, events::add);

        eventManager.registerEventType(KeyEvent.class);
        
        // First key down -> ACTIVE
        eventManager.publish(new KeyEvent(10, KeyEvent.Type.KEY_DOWN));
        assertEquals(1, events.size());
        assertEquals(KeyEvent.Type.KEY_DOWN, events.get(0).type());

        // Second key down -> Still ACTIVE, no new event
        eventManager.publish(new KeyEvent(11, KeyEvent.Type.KEY_DOWN));
        assertEquals(1, events.size());

        // First key up -> Still ACTIVE (one key left)
        eventManager.publish(new KeyEvent(10, KeyEvent.Type.KEY_UP));
        assertEquals(1, events.size());

        // Second key up -> INACTIVE
        eventManager.publish(new KeyEvent(11, KeyEvent.Type.KEY_UP));
        assertEquals(2, events.size());
        assertEquals(KeyEvent.Type.KEY_UP, events.get(1).type());

        assertTrue(events.stream().allMatch(e -> e.parameter().equals(forward)));
    }

    @Test
    void testRemoval() {
        BindingGroup movement = bindingSystem.createGroup("Movement");
        BindingParameter forward = bindingSystem.createParameter(movement, "Forward");
        bindingSystem.addBinding(forward, 10);
        bindingSystem.removeBinding(forward, 10);

        List<BindingEvent> events = new ArrayList<>();
        eventManager.subscribe(BindingEvent.class, events::add);

        eventManager.registerEventType(KeyEvent.class);
        eventManager.publish(new KeyEvent(10, KeyEvent.Type.KEY_DOWN));

        assertEquals(0, events.size());
    }

    @Test
    void testIdempotency() {
        BindingGroup movement1 = bindingSystem.createGroup("Movement");
        BindingGroup movement2 = bindingSystem.createGroup("Movement");
        assertSame(movement1, movement2);

        BindingParameter forward1 = bindingSystem.createParameter(movement1, "Forward");
        BindingParameter forward2 = bindingSystem.createParameter(movement1, "Forward");
        assertSame(forward1, forward2);
    }

    @Test
    void testGetters() {
        BindingGroup movement = bindingSystem.createGroup("Movement");
        BindingParameter forward = bindingSystem.createParameter(movement, "Forward");
        bindingSystem.addBinding(forward, 10);

        assertTrue(bindingSystem.getGroup("Movement").isPresent());
        assertEquals(movement, bindingSystem.getGroup("Movement").get());
        assertFalse(bindingSystem.getGroup("NonExistent").isPresent());

        assertTrue(bindingSystem.getParameter(movement, "Forward").isPresent());
        assertEquals(forward, bindingSystem.getParameter(movement, "Forward").get());
        assertFalse(bindingSystem.getParameter(movement, "NonExistent").isPresent());
        
        BindingGroup otherGroup = new BindingGroup("Other");
        assertFalse(bindingSystem.getParameter(otherGroup, "Forward").isPresent());

        assertTrue(bindingSystem.getBindings(forward).contains(10));
        assertTrue(bindingSystem.getBindings(new BindingParameter("Other", movement)).isEmpty());
    }

    @Test
    void testRemoveBindingEdgeCases() {
        BindingGroup movement = bindingSystem.createGroup("Movement");
        BindingParameter forward = bindingSystem.createParameter(movement, "Forward");
        
        // Remove from non-existent parameter
        assertDoesNotThrow(() -> bindingSystem.removeBinding(forward, 10));
        
        bindingSystem.addBinding(forward, 10);
        // Remove non-existent key from existing parameter
        assertDoesNotThrow(() -> bindingSystem.removeBinding(forward, 11));
        
        // Key with no parameters
        assertDoesNotThrow(() -> bindingSystem.removeBinding(new BindingParameter("None", movement), 10));
    }

    @Test
    void testHandleKeyEventEdgeCases() {
        List<BindingEvent> events = new ArrayList<>();
        eventManager.subscribe(BindingEvent.class, events::add);
        eventManager.registerEventType(KeyEvent.class);

        // Key with no bindngs
        eventManager.publish(new KeyEvent(999, KeyEvent.Type.KEY_DOWN));
        assertEquals(0, events.size());

        // Multiple parameters for same key
        BindingGroup movement = bindingSystem.createGroup("Movement");
        BindingParameter forward = bindingSystem.createParameter(movement, "Forward");
        BindingParameter jump = bindingSystem.createParameter(movement, "Jump");
        bindingSystem.addBinding(forward, 20);
        bindingSystem.addBinding(jump, 20);

        eventManager.publish(new KeyEvent(20, KeyEvent.Type.KEY_DOWN));
        assertEquals(2, events.size());
        
        // KEY_TYPED (shoud be ignord by curnt implemntation)
        int currentSize = events.size();
        eventManager.publish(new KeyEvent(20, KeyEvent.Type.KEY_TYPED));
        assertEquals(currentSize, events.size());
    }

    @Test
    void testGetDescriptor() {
        assertNotNull(bindingSystem.getDescriptor());
        assertEquals("bindings", bindingSystem.getDescriptor().coordinate().id());
    }
}
