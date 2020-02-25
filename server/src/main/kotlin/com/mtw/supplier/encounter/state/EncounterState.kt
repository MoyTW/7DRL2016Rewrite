package com.mtw.supplier.encounter.state

import com.mtw.supplier.ecs.Entity
import com.mtw.supplier.ecs.components.PlayerComponent
import com.mtw.supplier.encounter.rulebook.Action
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import java.util.*

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
    private val width: Int = 10,
    private val height: Int = 10,
    private var _currentTime: Int = 0,
    private var _completed: Boolean = false
) {
    val messageLog: EncounterMessageLog = EncounterMessageLog()

    val currentTime: Int
        get() = this._currentTime

    val completed: Boolean
        get() = this._completed

    // TODO: Map sizing
    private val encounterMap: EncounterMap = EncounterMap(width, height)

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


    // TODO: Possibly maintain internal list
    fun entities(): List<Entity> {
        return this.encounterMap.entities()
    }

    fun playerEntity(): Entity {
        return this.entities().first { it.hasComponent(PlayerComponent::class) }
    }

    fun getEntity(entityId: Int): Entity {
        return entities().firstOrNull { it.id == entityId } ?: throw EntityIdNotFoundException(entityId)
    }
    class EntityIdNotFoundException(entityId: Int): Exception("Entity id $entityId could not be found!")

    fun positionBlocked(pos: EncounterPosition): Boolean {
        return this.encounterMap.positionBlocked(pos)
    }

    fun arePositionsAdjacent(pos1: EncounterPosition, pos2: EncounterPosition): Boolean {
        return this.encounterMap.arePositionsAdjacent(pos1, pos2)
    }

    fun adjacentUnblockedPositions(pos: EncounterPosition): List<EncounterPosition> {
        return this.encounterMap.adjacentUnblockedPositions(pos)
    }

    /**
     * @throws EntityAlreadyHasLocation when a node already has a location
     * @throws NodeHasInsufficientSpaceException when node cannot find space for the entity
     */
    fun placeEntity(entity: Entity, targetPosition: EncounterPosition): EncounterState {
        this.encounterMap.placeEntity(entity, targetPosition)
        return this
    }

    fun removeEntity(entity: Entity): EncounterState {
        this.encounterMap.removeEntity(entity)
        return this
    }

    fun teleportEntity(entity: Entity, targetPosition: EncounterPosition) {
        this.encounterMap.teleportEntity(entity, targetPosition)
    }
}

