package com.mtw.supplier.engine.encounter.state

import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.ecs.components.CollisionComponent
import com.mtw.supplier.engine.ecs.components.EncounterLocationComponent
import com.mtw.supplier.engine.ecs.components.PlayerComponent
import com.mtw.supplier.engine.encounter.rulebook.Action
import com.mtw.supplier.engine.utils.Constants
import com.mtw.supplier.engine.utils.SeededRand
import com.mtw.supplier.engine.utils.XYCoordinates
import kotlinx.serialization.Serializable

@Serializable
class EncounterMessageLog {
    private val LOG_LENGTH = 100
    private val eventLog: MutableList<String> = mutableListOf()

    private fun addEntry(text: String) {
        if (eventLog.size >= LOG_LENGTH) {
            eventLog.removeAt(LOG_LENGTH - 1)
        }
        eventLog.add(0, text)
    }

    fun logAction(action: Action, status: String, text: String) {
        val actionString = "[${action.actor.name}]:[${action.actionType.name}]:[$status] $text"
        addEntry(actionString)
    }

    fun logEvent(eventType: String, text: String) {
        val eventString = "<EVENT>:<$eventType> $text"
        addEntry(eventString)
    }

    fun getMessages(): List<String> {
        return eventLog
    }
}

@Serializable
class EncounterState(
    val seededRand: SeededRand,
    private val width: Int,
    private val height: Int,
    private var _currentTime: Int = 0,
    private var _completed: Boolean = false,
    private var entityIdIdx: Int = 0 // TODO: uh this be dumb tho
) {
    // I don't think this is elegant, or like...good. in case you're wondering.
    private var _encounterMap: EncounterMap? = null
    private val encounterMap: EncounterMap
        get() = _encounterMap!!

    val messageLog: EncounterMessageLog = EncounterMessageLog()
    var fovCache: FoVCache? = null

    /**
     * Wouldn't normally do this, but serialization library discourages taking in constructor parameters that aren't var
     * or vals, and this (while not...elegant) is quick. rip.
     */
    fun initialize(player: Entity) {
        this._encounterMap = EncounterMapBuilder(1, player, seededRand).build()
    }

    val currentTime: Int
        get() = this._currentTime

    val completed: Boolean
        get() = this._completed

    fun calculatePlayerFoVAndMarkExploration() {
        this.fovCache = FoVCache.computeFoV(this.encounterMap,
            this.playerEntity().getComponent(EncounterLocationComponent::class).position,
            Constants.VISION_RADIUS
        )
        for (pos in this.fovCache!!.visiblePositions) {
            encounterMap.markExplored(pos)
        }
    }

    fun getNextEntityId(): Int {
        entityIdIdx += 1
        return entityIdIdx
    }


    fun getEncounterTileMap(): EncounterTileMapView {
        return encounterMap
    }

    fun advanceTime(timeDiff: Int) {
        this._currentTime += timeDiff
    }

    fun completeEncounter() {
        if (this._completed) {
            throw EncounterCannotBeCompletedTwiceException()
        }
        this._completed = true
    }
    class EncounterCannotBeCompletedTwiceException: Exception("Encounter cannot be completed twice!")


    // TODO: Assure ordering!
    fun entities(): List<Entity> {
        return this.encounterMap.entitiesByPlacementOrder()
    }

    fun playerEntity(): Entity {
        return this.entities().first { it.hasComponent(PlayerComponent::class) }
    }

    fun getEntity(entityId: String): Entity {
        return entities().firstOrNull { it.id == entityId } ?: throw EntityIdNotFoundException(entityId)
    }
    class EntityIdNotFoundException(entityId: String): Exception("Entity id $entityId could not be found!")

    fun getBlockingEntityAtPosition(pos: XYCoordinates): Entity? {
        return this.encounterMap.getEntitiesAtPosition(pos).firstOrNull { it.getComponentOrNull(CollisionComponent::class)?.blocksMovement ?: false }
    }

    fun getEntitiesAtPosition(pos: XYCoordinates): List<Entity> {
        return this.encounterMap.getEntitiesAtPosition(pos)
    }

    fun positionBlocked(pos: XYCoordinates): Boolean {
        return this.encounterMap.positionBlocked(pos)
    }

    fun arePositionsAdjacent(pos1: XYCoordinates, pos2: XYCoordinates): Boolean {
        return this.encounterMap.arePositionsAdjacent(pos1, pos2)
    }

    fun adjacentUnblockedPositions(pos: XYCoordinates): List<XYCoordinates> {
        return this.encounterMap.adjacentUnblockedPositions(pos)
    }

    /**
     * @throws EntityAlreadyHasLocation when a node already has a location
     * @throws NodeHasInsufficientSpaceException when node cannot find space for the entity
     */
    fun placeEntity(entity: Entity, targetPosition: XYCoordinates, ignoreCollision: Boolean = false): EncounterState {
        this.encounterMap.placeEntity(entity, targetPosition, ignoreCollision)
        return this
    }

    fun removeEntity(entity: Entity): EncounterState {
        this.encounterMap.removeEntity(entity)
        return this
    }

    fun teleportEntity(entity: Entity, targetPosition: XYCoordinates, ignoreCollision: Boolean = false) {
        this.encounterMap.teleportEntity(entity, targetPosition, ignoreCollision)
    }
}

