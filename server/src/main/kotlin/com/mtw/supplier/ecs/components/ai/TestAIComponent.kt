package com.mtw.supplier.ecs.components.ai

import com.mtw.supplier.ecs.Entity
import com.mtw.supplier.ecs.components.EncounterLocationComponent
import com.mtw.supplier.ecs.components.FactionComponent
import com.mtw.supplier.encounter.state.EncounterState
import com.mtw.supplier.encounter.rulebook.Action
import com.mtw.supplier.encounter.rulebook.actions.AttackAction
import com.mtw.supplier.encounter.rulebook.actions.MoveAction
import com.mtw.supplier.encounter.rulebook.actions.WaitAction
import com.mtw.supplier.utils.XYCoordinates
import kotlinx.serialization.Serializable
import java.util.*
import kotlin.math.abs


@Serializable
class TestAIComponent : AIComponent() {
    override var _parentId: Int? = null

    private fun parentIsHostileTo(parentEntity: Entity, otherEntity: Entity, encounterState: EncounterState): Boolean {
        return parentEntity.getComponent(FactionComponent::class).isHostileTo(otherEntity.id, encounterState)
    }

    override fun decideNextAction(encounterState: EncounterState): Action {
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
            val pathToFirstOtherEntity = aStarWithNewGrid(parentLocation, firstOtherEntityLocation, encounterState)
            if (pathToFirstOtherEntity != null) {
                MoveAction(parentEntity, pathToFirstOtherEntity[0])
            } else {
                WaitAction(parentEntity)
            }
        }
    }

    fun aStarHeuristic(startPos: XYCoordinates, endPos: XYCoordinates): Double {
        return abs(startPos.x.toDouble() - endPos.x.toDouble()) +
            abs(startPos.y.toDouble() - endPos.y.toDouble())
    }

    companion object {
        val aStarComparator = Comparator<Pair<XYCoordinates, Double>> { o1, o2 -> o1!!.second.compareTo(o2!!.second) }
    }

    fun aStarWithNewGrid(startPos: XYCoordinates,
                         endPos: XYCoordinates,
                         encounterState: EncounterState): List<XYCoordinates>? {
        val frontier = PriorityQueue<Pair<XYCoordinates, Double>>(aStarComparator)
        frontier.add(Pair(startPos, 0.0))

        val cameFrom: MutableMap<XYCoordinates, XYCoordinates> = mutableMapOf()

        val costSoFar: MutableMap<XYCoordinates, Double> = mutableMapOf()
        costSoFar[startPos] = 0.0

        while (frontier.isNotEmpty()) {
            val currentPos = frontier.poll().first

            if (encounterState.arePositionsAdjacent(currentPos, endPos)) {
                val path = mutableListOf(endPos, currentPos)
                while (cameFrom.containsKey(path.last())) {
                    path.add(cameFrom[path.last()]!!)
                }
                path.remove(startPos)
                return path.reversed()
            }

            for (nextPos in encounterState.adjacentUnblockedPositions(currentPos)) {
                val newNextPosCost = costSoFar[currentPos]!!.plus(1.0) // Fixed cost of 1
                if (!costSoFar.containsKey(nextPos) || newNextPosCost < costSoFar[nextPos]!!) {
                    costSoFar[nextPos] = newNextPosCost
                    val priority = newNextPosCost + aStarHeuristic(nextPos, endPos)
                    frontier.add(Pair(nextPos, priority))
                    cameFrom[nextPos] = currentPos
                }
            }
        }
        return null
    }
}