package com.mtw.supplier.encounter.state

import com.mtw.supplier.ecs.Entity
import com.mtw.supplier.ecs.components.CollisionComponent
import com.mtw.supplier.ecs.components.EncounterLocationComponent
import kotlinx.serialization.Serializable

interface EncounterTileView {
    val occupied: Boolean
    val blocked: Boolean
}

interface EncounterTileMapView {
    val width: Int
    val height: Int
    fun getTileView(x: Int, y: Int): EncounterTileView
}

@Serializable
private class EncounterNodeView(
    // Whether or not the node itself is passable
    val terrainBlocked: Boolean = false,
    val entities: MutableList<Entity> = mutableListOf()
): EncounterTileView {
    // Whether or not the node itself is occupied
    override val occupied: Boolean
        get() = entities.any{ it.hasComponent(CollisionComponent::class) && it.getComponent(CollisionComponent::class).collidable }

    override val blocked: Boolean
        get() = terrainBlocked || occupied
}

@Serializable
internal class EncounterMap(
    override val width: Int,
    override val height: Int
): EncounterTileMapView {
    private val nodes: Array<Array<EncounterNodeView>> = Array(width) { Array(height) { EncounterNodeView() } }

    override fun getTileView(x: Int, y: Int): EncounterTileView {
        return nodes[x][y]
    }

    internal fun isInBounds(x: Int, y: Int): Boolean {
        return x in 0 until width && y in 0 until height
    }

    internal fun positionBlocked(pos: EncounterPosition): Boolean {
        return nodes[pos.x][pos.y].blocked
    }

    internal fun arePositionsAdjacent(pos1: EncounterPosition, pos2: EncounterPosition): Boolean {
        val dx = kotlin.math.abs(pos1.x - pos2.x)
        val dy = kotlin.math.abs(pos1.y - pos2.y)
        val adjacent = dx < 2 && dy < 2 && (dx + dy != 0)
        return adjacent
    }

    internal fun adjacentUnblockedPositions(pos: EncounterPosition): List<EncounterPosition> {
        val adjacentUnblockedPositions = mutableListOf<EncounterPosition>()
        for(x in (pos.x - 1..pos.x + 1)) {
            for (y in (pos.y - 1..pos.y + 1)) {
                if (x != y && isInBounds(x, y) && !nodes[x][y].blocked) {
                    adjacentUnblockedPositions.add(EncounterPosition(x, y))
                }
            }
        }
        return adjacentUnblockedPositions
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
            throw NodeHasInsufficientSpaceException("Node $targetPosition is full, cannot place ${entity.name}")
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