package com.mtw.supplier.ecs.components

import com.mtw.supplier.ecs.Component
import com.mtw.supplier.ecs.Entity
import com.mtw.supplier.encounter.state.EncounterState
import com.mtw.supplier.encounter.rulebook.Action
import com.mtw.supplier.encounter.rulebook.actions.AttackAction
import com.mtw.supplier.encounter.rulebook.actions.MoveAction
import com.mtw.supplier.encounter.rulebook.actions.WaitAction
import com.mtw.supplier.encounter.state.EncounterPosition
import kotlinx.serialization.Serializable

@Serializable
class AIComponent : Component() {
    override var _parentId: Int? = null

    private fun parentIsHostileTo(parentEntity: Entity, otherEntity: Entity, encounterState: EncounterState): Boolean {
        return parentEntity.getComponent(FactionComponent::class).isHostileTo(otherEntity.id, encounterState)
    }

    fun decideNextAction(encounterState: EncounterState): Action {
        val parentEntity = encounterState.getEntity(this.parentId)
        // TODO: All of this is a placeholder
        val firstOtherAliveEnemy = encounterState.entities()
            .firstOrNull {
                it != parentEntity &&
                it.hasComponent(AIComponent::class) &&
                it.hasComponent(FactionComponent::class) &&
                this.parentIsHostileTo(parentEntity, it, encounterState)
            }
            ?: return WaitAction(parentEntity)


        val parentLocation = parentEntity.getComponent(EncounterLocationComponent::class).position
        val firstOtherEntityLocation = firstOtherAliveEnemy.getComponent(EncounterLocationComponent::class).position

        // wow ugly!
        return if (encounterState.arePositionsAdjacent(parentLocation, firstOtherEntityLocation)) {
            AttackAction(parentEntity, firstOtherAliveEnemy)
        } else  {
            val pathToFirstOtherEntity = lowEffortBfs(parentLocation, firstOtherEntityLocation, encounterState)
            if (pathToFirstOtherEntity != null) {
                MoveAction(parentEntity, pathToFirstOtherEntity[0])
            } else {
                WaitAction(parentEntity)
            }
        }
    }

    fun lowEffortBfs(startNode: EncounterPosition,
                     endNode: EncounterPosition,
                     encounterState: EncounterState): List<EncounterPosition>? {
        val nextToVisit: MutableList<EncounterPosition> = mutableListOf(startNode)
        val visited: MutableSet<EncounterPosition> = mutableSetOf(startNode)
        val pathTracker: MutableMap<EncounterPosition, EncounterPosition> = mutableMapOf()

        while (nextToVisit.isNotEmpty()) {
            val current = nextToVisit[0]
            nextToVisit.removeAt(0)

            if (encounterState.arePositionsAdjacent(current, endNode)) {
                val path = mutableListOf(endNode, current)
                while (pathTracker.containsKey(path.last())) {
                    path.add(pathTracker[path.last()]!!)
                }
                path.remove(startNode)
                return path.reversed()
            }

            visited.add(current)

            encounterState.adjacentUnblockedPositions(current).map {
                if (!visited.contains(it)) {
                    nextToVisit.add(it)
                    pathTracker[it] = current
                }
            }
        }
        return null
    }
}
