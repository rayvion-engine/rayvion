package com.rayvion.game.consumable;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.inventory.ConsumableItemUseEvent;
import com.rayvion.engine.inventory.InventorySystem;
import com.rayvion.engine.inventory.ItemInteractEvent;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * System that handles the generic logic of using consumable items.
 */
@Slf4j
public class ConsumableSystem implements System {
    private final SystemDescriptor descriptor;
    private InventorySystem inventorySystem;
    private EventManager eventManager;

    public ConsumableSystem() {
        this.descriptor = new SystemDescriptor(
                new SystemCoordinate("com.rayvion.game", "consumable-system", Version.parse("1.0.0")),
                Set.of(),
                Set.of()
        );
    }

    @Override
    public SystemDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void onDependencyAdded(System dependency) {
        if (dependency instanceof InventorySystem is) this.inventorySystem = is;
        if (dependency instanceof EventManager em) this.eventManager = em;
    }

    @Override
    public void init() {
        if (eventManager != null) {
            eventManager.subscribe(ItemInteractEvent.class, this::handleItemInteract);
            log.info("ConsumableSystem initialized and subscribed to ItemInteractEvent");
        }
    }

    private void handleItemInteract(ItemInteractEvent event) {
        if (!"consumable".equals(event.item().type())) {
            return;
        }

        if (inventorySystem == null) return;

        inventorySystem.getInventory(event.entity()).ifPresent(inventory -> {
            if (inventory.removeItem(event.item())) {
                log.info("ConsumableSystem: Consuming '{}' for entity {}", 
                        event.item().name(), event.entity().id());
                eventManager.publish(new ConsumableItemUseEvent(event.entity(), event.item()));
            }
        });
    }

}
