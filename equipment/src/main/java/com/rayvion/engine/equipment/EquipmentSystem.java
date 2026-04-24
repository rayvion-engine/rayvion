package com.rayvion.engine.equipment;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.inventory.InventoryItem;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.dependency.SystemDependency;
import com.rayvion.engine.system.trait.SystemTraitRequirement;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;
import java.util.Optional;
import java.util.Set;

/**
 * System that manages equipment for entities.
 * Allows equipping a single item that can potentially override the entity's graphics.
 */
public interface EquipmentSystem extends System {
    @Override
    default SystemDescriptor getDescriptor() {
        return new SystemDescriptor(
                new SystemCoordinate("com.rayvion.engine", "equipment", Version.parse("0.1.0")),
                Set.of(
                        new SystemDependency(
                                new SystemTraitRequirement("com.rayvion.engine", "graphics", version -> version.getMajorVersion() == 0),
                                SystemDependency.RequirementLevel.REQUIRED
                        ),
                        new SystemDependency(
                                new SystemTraitRequirement("com.rayvion.engine", "event", version -> version.getMajorVersion() == 0),
                                SystemDependency.RequirementLevel.REQUIRED
                        )
                ),
                Set.of(new SystemTraitCoordinate("com.rayvion.engine", "equipment", Version.parse("0.1.0")))
        );
    }

    /**
     * Equips an item to an entity.
     * If the entity already has an item equipped, it will be unequipped first.
     *
     * @param entityId The ID of the entity.
     * @param item The item to equip.
     */
    void equip(long entityId, InventoryItem item);

    /**
     * Unequips the currently equipped item from an entity.
     *
     * @param entityId The ID of the entity.
     */
    void unequip(long entityId);

    /**
     * Gets the currently equipped item for an entity.
     *
     * @param entityId The ID of the entity.
     * @return An Optional containing the equipped item, or empty if none.
     */
    Optional<InventoryItem> getEquippedItem(long entityId);

    /**
     * Checks if an entity has an item equipped.
     *
     * @param entityId The ID of the entity.
     * @return True if an item is equipped, false otherwise.
     */
    boolean hasEquippedItem(long entityId);
}
