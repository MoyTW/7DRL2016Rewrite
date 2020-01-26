package com.mtw.supplier.encounter.state

import com.mtw.supplier.ecs.Entity
import kotlinx.serialization.Serializable


@Serializable
class EncounterState(
    public val width: Int = 10,
    public val height: Int = 10,
    private var _currentTime: Int = 0,
    private var _completed: Boolean = false
) {
    val currentTime: Int
        get() = this._currentTime

    val completed: Boolean
        get() = this._completed

    // TODO: Map sizing
    private val encounterMap: EncounterMap = EncounterMap(50, 50)

    fun advanceTime(timeDiff: Int = 1) {
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

    fun getEntity(entityId: Int): Entity {
        return entities().firstOrNull { it.id == entityId } ?: throw EntityIdNotFoundException(entityId)
    }
    class EntityIdNotFoundException(entityId: Int): Exception("Entity id $entityId could not be found!")

    fun positionBlocked(pos: EncounterPosition): Boolean {
        return this.encounterMap.positionBlocked(pos)
    }

    fun positionsAdjacent(pos1: EncounterPosition, pos2: EncounterPosition): Boolean {
        return this.encounterMap.positionsAdjacent(pos1, pos2)
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

