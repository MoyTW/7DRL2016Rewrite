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
class EnemyGunshipAIComponent: AIComponent() {
    override var _parentId: String? = null
    override var isActive: Boolean = false

    private val gunCooldown = 4
    private var currentGunCooldown = 0
    private var canMove = true

    override fun decideNextActions(encounterState: EncounterState): List<Action> {
        if (!isActive) { return listOf() }

        val actions = mutableListOf<Action>()

        val parent = encounterState.getEntity(this.parentId)
        val parentPos = parent.getComponent(EncounterLocationComponent::class).position
        val playerPos = encounterState.playerEntity().getComponent(EncounterLocationComponent::class).position

        // Close distance if further than 5 every other turn
        if (this.canMove && EncounterStateUtils.distanceBetween(parentPos, playerPos) >= 5f) {
            val path = EncounterStateUtils.aStarWithNewGrid(parentPos, playerPos, encounterState)
            if (path != null) {
                actions.add(MoveAction(actor = parent, targetPosition = path[0]))
            }
            this.canMove = false
        } else {
            this.canMove = true
        }

        // Fire shotgun, then cannon 3 times; I'm not entirely sure I didn't mean for it to be opposite, lol
        if (currentGunCooldown == 0) {
            actions.add(WeaponList.createFireSmallShotgunAction(parent, playerPos, encounterState.seededRand))
            currentGunCooldown += gunCooldown
        } else {
            actions.add(WeaponList.createFireSmallCannonAction(parent, playerPos))
        }
        if (currentGunCooldown > 0) {
            currentGunCooldown--
        }

        return actions
    }
}