package com.mtw.supplier.encounter.state

import com.mtw.supplier.ecs.Entity
import com.mtw.supplier.ecs.components.EncounterLocationComponent
import kotlinx.serialization.Serializable

@Serializable
private class EncounterNode(
    val entities: MutableList<Entity> = mutableListOf()
) {
    // TODO: Collision!
    fun passable(): Boolean {
        return true
    }
}

@Serializable
internal class EncounterMap(
    private val width: Int,
    private val height: Int
) {
    private val nodes: Array<Array<EncounterNode>> = Array(width) { Array(height) { EncounterNode() } }

    internal fun positionBlocked(pos: EncounterPosition): Boolean {
        return nodes[pos.x][pos.y].passable()
    }

    internal fun positionsAdjacent(pos1: EncounterPosition, pos2: EncounterPosition): Boolean {
        val dx = kotlin.math.abs(pos1.x - pos1.y)
        val dy = kotlin.math.abs(pos1.y - pos2.y)
        return dx < 2 && dy < 2 && dx + dy != 0
    }

    internal fun entities(): List<Entity> {
        return this.nodes.flatten().flatMap { it.entities }
    }

    /**
     * @throws EntityAlreadyHasLocation when a node already has a location
     * @throws NodeHasInsufficientSpaceException when node cannot find space for the entity
     */
    internal fun placeEntity(entity: Entity, targetPosition: EncounterPosition) {
        if (entity.hasComponent(EncounterLocationComponent::class)) {
            throw EntityAlreadyHasLocation("Specified entity ${entity.name} already has a location, cannot be placed!")
        } else if (this.positionBlocked(targetPosition)) {
            throw NodeHasInsufficientSpaceException("Node [{$targetPosition.x}][${targetPosition.y}] is full, cannot place ${entity.name}")
        }

        this.nodes[targetPosition.x][targetPosition.y].entities.add(entity)
        entity.addComponent(EncounterLocationComponent(targetPosition))
    }
    class EntityAlreadyHasLocation(message: String): Exception(message)
    class NodeHasInsufficientSpaceException(message: String): Exception(message)

    internal fun removeEntity(entity: Entity) {
        if (!entity.hasComponent(EncounterLocationComponent::class)) {
            throw EntityHasNoLocation("Specified entity ${entity.name} has no location, cannot remove!")
        }

        val locationComponent = entity.getComponent(EncounterLocationComponent::class)
        val (x, y) = locationComponent.position
        this.nodes[x][y].entities.remove(entity)
        entity.removeComponent(locationComponent)
    }
    class EntityHasNoLocation(message: String): Exception(message)

    internal fun teleportEntity(entity: Entity, targetPosition: EncounterPosition) {
        this.removeEntity(entity)
        this.placeEntity(entity, targetPosition)
    }

}