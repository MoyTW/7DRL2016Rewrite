package com.mtw.supplier.engine.encounter.state

import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.ecs.components.ActionTimeComponent
import com.mtw.supplier.engine.ecs.components.CollisionComponent
import com.mtw.supplier.engine.ecs.components.EncounterLocationComponent
import com.mtw.supplier.engine.ecs.components.PlayerComponent
import com.mtw.supplier.engine.ecs.components.ai.PathAIComponent
import com.mtw.supplier.engine.utils.XYCoordinates
import kotlinx.serialization.Serializable


interface EncounterTileView {
    val explored: Boolean
}

interface EncounterTileMapView {
    val width: Int
    val height: Int
    fun isExplored(x: Int, y: Int): Boolean
    fun blocksVision(x: Int, y: Int): Boolean
}

@Serializable
private class EncounterNode(
    // Whether or not the node itself is passable
    private var _explored: Boolean = false,
    var terrainBlocksMovement: Boolean = false,
    var terrainBlocksVision: Boolean = false
): EncounterTileView {

    override val explored: Boolean
        get() = _explored

    fun markExplored() {
        this._explored = true
    }
}

@Serializable
internal class EncounterMap(
    override val width: Int,
    override val height: Int
): EncounterTileMapView {
    // Tracks the ordering of entities being placed on the map, so if there are multiple entities going on the same tick
    // we have a non-arbitrary ordering. This is...kind of crude
    private var placementTracker: Int = 0
    private val nodes: Array<Array<EncounterNode>> = Array(width) { Array(height) { EncounterNode() } }
    private val entitiesByPosition: MutableMap<XYCoordinates, MutableList<Entity>> = mutableMapOf()

    override fun isExplored(x: Int, y: Int): Boolean {
        return isInBounds(x, y) && nodes[x][y].explored
    }

    override fun blocksVision(x: Int, y: Int): Boolean {
        if (!isInBounds(x, y)) { return true }
        return nodes[x][y].terrainBlocksVision ||
            getEntitiesAtPosition(XYCoordinates(x, y)).any{ it.getComponentOrNull(CollisionComponent::class)?.blocksVision ?: false }
    }

    internal fun isInBounds(x: Int, y: Int): Boolean {
        return x in 0 until width && y in 0 until height
    }

    internal fun markBlockStatus(pos: XYCoordinates, terrainBlocksMovement: Boolean, terrainBlocksVision: Boolean) {
        nodes[pos.x][pos.y].terrainBlocksMovement = terrainBlocksMovement
        nodes[pos.x][pos.y].terrainBlocksVision = terrainBlocksVision
    }

    internal fun markExplored(pos: XYCoordinates) {
        nodes[pos.x][pos.y].markExplored()
    }

    internal fun positionBlocked(pos: XYCoordinates): Boolean {
        if (!isInBounds(pos.x, pos.y)) { return true }
        return nodes[pos.x][pos.y].terrainBlocksMovement ||
            getEntitiesAtPosition(pos).any{ it.getComponentOrNull(CollisionComponent::class)?.blocksMovement ?: false }
    }

    internal fun arePositionsAdjacent(pos1: XYCoordinates, pos2: XYCoordinates): Boolean {
        val dx = kotlin.math.abs(pos1.x - pos2.x)
        val dy = kotlin.math.abs(pos1.y - pos2.y)
        val adjacent = dx < 2 && dy < 2 && (dx + dy != 0)
        return adjacent
    }

    internal fun adjacentUnblockedPositions(pos: XYCoordinates): List<XYCoordinates> {
        val adjacentUnblockedPositions = mutableListOf<XYCoordinates>()
        for(x in (pos.x - 1..pos.x + 1)) {
            for (y in (pos.y - 1..pos.y + 1)) {
                if (x != y && isInBounds(x, y) && !positionBlocked(XYCoordinates(x, y))) {
                    adjacentUnblockedPositions.add(XYCoordinates(x, y))
                }
            }
        }
        return adjacentUnblockedPositions
    }

    internal fun entitiesByPlacementOrder(): List<Entity> {
        val entities = this.entitiesByPosition.values.flatten().sortedBy {
            it.getComponent(EncounterLocationComponent::class).placementOrder
        }
        return entities
    }

    internal fun getEntitiesAtPosition(pos: XYCoordinates): List<Entity> {
        if (!isInBounds(pos.x, pos.y)) { return emptyList() }
        return this.entitiesByPosition[pos] ?: emptyList()
    }

    /**
     * @throws EntityAlreadyHasLocation when a node already has a location
     * @throws NodeHasInsufficientSpaceException when node cannot find space for the entity
     */
    internal fun placeEntity(entity: Entity, targetPosition: XYCoordinates, ignoreCollision: Boolean) {
        if (entity.hasComponent(EncounterLocationComponent::class)) {
            throw EntityAlreadyHasLocation("Specified entity ${entity.name} already has a location, cannot be placed!")
        } else if (!ignoreCollision && this.positionBlocked(targetPosition)) {
            throw NodeHasInsufficientSpaceException("Node $targetPosition is full, cannot place ${entity.name}")
        }

        if(this.entitiesByPosition.containsKey(targetPosition)) {
            this.entitiesByPosition[targetPosition]!!.add(entity)
        } else {
            this.entitiesByPosition[targetPosition] = mutableListOf(entity)
        }
        entity.addComponent(EncounterLocationComponent(targetPosition, placementTracker))
        placementTracker++
    }
    class EntityAlreadyHasLocation(message: String): Exception(message)
    class NodeHasInsufficientSpaceException(message: String): Exception(message)

    internal fun removeEntity(entity: Entity) {
        if (!entity.hasComponent(EncounterLocationComponent::class)) {
            throw EntityHasNoLocation("Specified entity ${entity.name} has no location, cannot remove!")
        }

        val locationComponent = entity.getComponent(EncounterLocationComponent::class)
        this.entitiesByPosition[locationComponent.position]!!.remove(entity)
        entity.removeComponent(locationComponent)
    }
    class EntityHasNoLocation(message: String): Exception(message)

    internal fun teleportEntity(entity: Entity, targetPosition: XYCoordinates, ignoreCollision: Boolean) {
        if (!entity.hasComponent(EncounterLocationComponent::class)) {
            throw EntityHasNoLocation("Specified entity ${entity.name} has no location, cannot remove!")
        } else if (!ignoreCollision && this.positionBlocked(targetPosition)) {
            throw NodeHasInsufficientSpaceException("Node $targetPosition is full, cannot place ${entity.name}")
        }

        val locationComponent = entity.getComponent(EncounterLocationComponent::class)

        // Modify EncounterMap linkages
        this.entitiesByPosition[locationComponent.position]!!.remove(entity)
        if(this.entitiesByPosition.containsKey(targetPosition)) {
            this.entitiesByPosition[targetPosition]!!.add(entity)
        } else {
            this.entitiesByPosition[targetPosition] = mutableListOf(entity)
        }

        // Update locationComponent
        locationComponent.position = targetPosition
    }

}