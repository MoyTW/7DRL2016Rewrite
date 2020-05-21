package com.mtw.supplier.engine.ecs.components.ai

import com.mtw.supplier.engine.ecs.components.EncounterLocationComponent
import com.mtw.supplier.engine.encounter.rulebook.Action
import com.mtw.supplier.engine.encounter.rulebook.actions.FireProjectileAction
import com.mtw.supplier.engine.encounter.rulebook.actions.MoveAction
import com.mtw.supplier.engine.encounter.rulebook.actions.ProjectileType
import com.mtw.supplier.engine.encounter.rulebook.actions.WeaponList
import com.mtw.supplier.engine.encounter.state.EncounterState
import com.mtw.supplier.engine.encounter.state.EncounterStateUtils
import com.mtw.supplier.engine.utils.LinePathBuilder
import kotlinx.serialization.Serializable

@Serializable
class EnemyFighterAIComponent: AIComponent() {
    override var _parentId: String? = null
    override var isActive: Boolean = false

    override fun decideNextActions(encounterState: EncounterState): List<Action> {
        if (!isActive) { return listOf() }

        val actions = mutableListOf<Action>()

        val parent = encounterState.getEntity(this.parentId)
        val parentPos = parent.getComponent(EncounterLocationComponent::class).position
        val playerPos = encounterState.playerEntity().getComponent(EncounterLocationComponent::class).position

        // Close distance
        val path = EncounterStateUtils.aStarWithNewGrid(parentPos, playerPos, encounterState)
        if (path != null) {
            actions.add(MoveAction(actor = parent, targetPosition = path[0]))
        }

        // Fire 3 gatling shells (wait, they'll just look like one shell though? did I do that just for style?)
        actions.add(WeaponList.createFireSmallGatlingAction(parent, playerPos))
        actions.add(WeaponList.createFireSmallGatlingAction(parent, playerPos))
        actions.add(WeaponList.createFireSmallGatlingAction(parent, playerPos))

        return actions
    }
}