package com.mtw.supplier.engine.ecs.components.item

import com.mtw.supplier.engine.ecs.Component
import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.ecs.components.EncounterLocationComponent
import kotlinx.serialization.Serializable

@Serializable
class InventoryComponent(
    val size: Int = 26
): Component() {
    override var _parentId: String? = null

    private val contents: MutableList<Entity> = mutableListOf()

    /**
     * @throws InventoryFullException when the inventory is already full
     * @throws NotCarryableException when the entity has no carryable component
     */
    fun addItem(carryable: Entity) {
        if (carryable.hasComponent(EncounterLocationComponent::class)) {
            throw ItemHasEncounterLocation("Cannot carry ${carryable.name} because it is on the map!")
        }else if (contents.size >= size) {
            throw InventoryFullException("Cannot carry ${carryable.name} because inventory is already full!")
        }

        val carryableComponent = carryable.getComponentOrNull(CarryableComponent::class)
        if (carryableComponent == null) {
            throw NotCarryableException("Cannot carry entity ${carryable.name} id=${carryable.id}")
        } else {
            this.contents.add(carryable)
        }
    }
    class ItemHasEncounterLocation(message: String): Exception(message)
    class InventoryFullException(message: String): Exception(message)
    class NotCarryableException(message: String): Exception(message)

    fun removeItem(carryable: Entity) {
        contents.remove(carryable)
    }
}
