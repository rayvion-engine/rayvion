package com.rayvion.game.combat;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.equipment.EquipmentSystem;
import com.rayvion.engine.event.EventManager;
import com.rayvion.engine.inventory.InventoryItem;
import com.rayvion.engine.inventory.ItemInteractEvent;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.dependency.SystemDependency;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.trait.SystemTraitRequirement;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.Set;

@Slf4j
public class ShieldSystem implements System {

    private final SystemDescriptor descriptor;
    private EquipmentSystem equipmentSystem;
    private EventManager eventManager;

    public ShieldSystem() {
        this.descriptor = new SystemDescriptor(
                new SystemCoordinate("com.rayvion.game", "shield-system", Version.parse("1.0.0")),
                Set.of(
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "equipment", v -> true), SystemDependency.RequirementLevel.REQUIRED),
                        new SystemDependency(new SystemTraitRequirement("com.rayvion.engine", "event", v -> true), SystemDependency.RequirementLevel.REQUIRED)
                ),
                Set.of()
        );
    }

    @Override
    public SystemDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public void onDependencyAdded(System dependency) {
        if (dependency instanceof EquipmentSystem es) equipmentSystem = es;
        else if (dependency instanceof EventManager em) eventManager = em;
    }

    @Override
    public void init() {
        if (eventManager != null) {
            eventManager.subscribe(ItemInteractEvent.class, this::handleItemInteract);
        }
    }

    private void handleItemInteract(ItemInteractEvent event) {
        if (isShield(event.item())) {
            long entityId = event.entity().id();
            Optional<InventoryItem> currentlyEquipped = equipmentSystem.getEquippedItem(entityId);

            if (currentlyEquipped.isPresent() && currentlyEquipped.get().equals(event.item())) {
                equipmentSystem.unequip(entityId);
            } else {
                equipmentSystem.equip(entityId, event.item());
            }
        }
    }

    private boolean isShield(InventoryItem item) {
        return "shield".equals(item.type()) || item.id().contains("shield");
    }
}
