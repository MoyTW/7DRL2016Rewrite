package com.mtw.supplier.ecs.components

import com.mtw.supplier.ecs.Component
import com.mtw.supplier.ecs.Entity
import com.mtw.supplier.encounter.state.EncounterState
import com.mtw.supplier.encounter.rulebook.Action
import com.mtw.supplier.encounter.rulebook.actions.AttackAction
import com.mtw.supplier.encounter.rulebook.actions.MoveAction
import com.mtw.supplier.encounter.rulebook.actions.WaitAction
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
        val firstOtherAliveEnemy = encounterState.getEntities()
            .firstOrNull {
                it != parentEntity &&
                it.hasComponent(AIComponent::class) &&
                it.hasComponent(FactionComponent::class) &&
                this.parentIsHostileTo(parentEntity, it, encounterState)
            }
            ?: return WaitAction(parentEntity)


        val parentLocation = parentEntity.getComponent(EncounterLocationComponent::class).locationNodeId
        val firstOtherEntityLocation = firstOtherAliveEnemy
            .getComponent(EncounterLocationComponent::class)
            .locationNodeId

        // wow ugly!
        return if (encounterState.getNodeDirectlyConnected(parentLocation, firstOtherEntityLocation)) {
            AttackAction(parentEntity, firstOtherAliveEnemy)
        } else  {
            val pathToFirstOtherEntity = badDepthFirstSearch(parentLocation, firstOtherEntityLocation, encounterState)
            if (pathToFirstOtherEntity != null) {
                MoveAction(parentEntity, pathToFirstOtherEntity[pathToFirstOtherEntity.size - 2])
            } else {
                WaitAction(parentEntity)
            }
        }
    }

    /**
     * look this is from memory ok it's not pretty
     * I feel compelled to defend my mediocre-to-bad on-the-spot algorithm skills because it's been so long since I've
     * actually written a classical algorithm, versus business logic & APIs & sequence diagrams & kafka streams lol
     */
    fun badDepthFirstSearch(startNode: Int, endNode: Int, encounterState: EncounterState): List<Int>? {
        return dfsRecurse(startNode, endNode, encounterState, setOf())
    }

    private fun dfsRecurse(startNode: Int, endNode: Int, encounterState: EncounterState, visitedNodes: Set<Int>): MutableList<Int>? {
        val exits = encounterState.getDirectlyConnectedNodes(startNode)

        return when {
            startNode == endNode -> mutableListOf(startNode)
            exits.isEmpty() -> null
            else -> return exits.map { exitId ->
                if (exitId !in visitedNodes) {
                    val newVisitedNodes = visitedNodes.toMutableSet() // this should copy it!
                    newVisitedNodes.add(startNode)
                    val recurseResult = dfsRecurse(exitId, endNode, encounterState, newVisitedNodes)
                    recurseResult?.add(startNode)
                    recurseResult
                } else {
                    null
                }
            }.firstOrNull {
                it != null
            }
        }
    }
}
