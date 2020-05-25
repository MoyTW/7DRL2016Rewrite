package com.mtw.supplier.engine.ecs.components.ai

import com.mtw.supplier.engine.ecs.components.EncounterLocationComponent
import com.mtw.supplier.engine.encounter.rulebook.Action
import com.mtw.supplier.engine.encounter.rulebook.actions.MoveAction
import com.mtw.supplier.engine.encounter.rulebook.actions.WeaponList
import com.mtw.supplier.engine.encounter.state.EncounterState
import com.mtw.supplier.engine.encounter.state.EncounterStateUtils
import kotlinx.serialization.Serializable

@Serializable
class EnemyScoutAIComponent: AIComponent() {
    override var _parentId: String? = null
    override var isActive: Boolean = false

    override fun decideNextActions(encounterState: EncounterState): List<Action> {
        if (!isActive) { return listOf() }

        val actions = mutableListOf<Action>()

        val parent = encounterState.getEntity(this.parentId)
        val parentPos = parent.getComponent(EncounterLocationComponent::class).position
        val playerPos = encounterState.playerEntity().getComponent(EncounterLocationComponent::class).position

        // Close distance
        if (EncounterStateUtils.distanceBetween(parentPos, playerPos) >= 5f) {
            val path = EncounterStateUtils.aStarWithNewGrid(parentPos, playerPos, encounterState)
            if (path != null) {
                actions.add(MoveAction(actorId = parent.id, targetPosition = path[0]))
            }
        }
        // Fire
        actions.add(WeaponList.createFireSmallShotgunAction(parent, playerPos, encounterState.seededRand))

        return actions
    }
}