package com.mtw.supplier.encounter.rulebook

import com.mtw.supplier.ecs.Entity
import com.mtw.supplier.ecs.components.AIComponent
import com.mtw.supplier.ecs.components.EncounterLocationComponent
import com.mtw.supplier.ecs.components.FighterComponent
import com.mtw.supplier.ecs.components.HpComponent
import com.mtw.supplier.encounter.state.EncounterState
import com.mtw.supplier.encounter.rulebook.actions.AttackAction
import com.mtw.supplier.encounter.rulebook.actions.MoveAction
import com.mtw.supplier.encounter.rulebook.actions.WaitAction
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt

object Rulebook {
    private val logger = LoggerFactory.getLogger(Rulebook::class.java)

    fun resolveAction(action: Action, encounterState: EncounterState) {
        when (action.actionType) {
            ActionType.MOVE -> resolveMoveAction(action as MoveAction, encounterState)
            ActionType.ATTACK -> resolveAttackAction(action as AttackAction, encounterState)
            ActionType.USE_ITEM -> TODO()
            ActionType.WAIT -> resolveWaitAction(action as WaitAction)
        }
    }

    private fun resolveMoveAction(action: MoveAction, encounterState: EncounterState) {
        val currentPosition = action.actor
            .getComponent(EncounterLocationComponent::class)
            .position

        val targetNodeSameAsCurrentNode = currentPosition == action.targetPosition
        val targetNodeBlocked = encounterState.positionBlocked(action.targetPosition)
        val targetNodeAdjacent = encounterState.arePositionsAdjacent(currentPosition, action.targetPosition)

        if (targetNodeSameAsCurrentNode) {
            logger.info("[MOVE]:[INVALID] Target node ${action.targetPosition} and source node are identical!")
        } else if (targetNodeBlocked) {
            logger.info("[MOVE]:[INVALID] Target node ${action.targetPosition} full!")
        } else if (!targetNodeAdjacent) {
            logger.info("[MOVE]:[INVALID] Current node $currentPosition is not adjacent to target node ${action.targetPosition}!")
        } else {
            encounterState.teleportEntity(action.actor, action.targetPosition)
            logger.info("[MOVE]:[SUCCESS] ${action.actor.name} $currentPosition to ${action.targetPosition}")
        }
    }

    private fun resolveAttackAction(action: AttackAction, encounterState: EncounterState) {
        val attacker = action.actor
        val attackerPos = attacker.getComponent(EncounterLocationComponent::class).position

        val defender = action.target
        val defenderPos = defender.getComponent(EncounterLocationComponent::class).position

        // TODO: Range & visibility & such
        if (!encounterState.arePositionsAdjacent(attackerPos, defenderPos)) {
            logger.info("[ATTACK]:[INVALID] [${action.actor.name}] cannot reach [${action.target.name}]")
        } else {
            val attackerFighter = attacker.getComponent(FighterComponent::class)
            val defenderFighter = defender.getComponent(FighterComponent::class)

            // TODO: Properly controlled randomness
            val r = Random(4)
            val d100Roll = r.nextInt(100) + 1

            // TODO: Shamelessly stealing POE because why not but maybe actually consider mechanics
            val modifiedAttackRoll = d100Roll + attackerFighter.toHit - defenderFighter.toDodge
            when {
                modifiedAttackRoll < 30 -> {
                    logger.info("[ATTACK]:[MISS] (raw=$d100Roll,final=$modifiedAttackRoll) [${action.actor.name}] missed [${action.target.name}]")
                }
                modifiedAttackRoll in 31..50 -> {
                    val damage = ceil(attackerFighter.hitDamage * .5).roundToInt()
                    logger.info("[ATTACK]:[GRAZE] (raw=$d100Roll,final=$modifiedAttackRoll) [${action.actor.name}] grazed [${action.target.name}] for $damage damage!")
                    applyDamage(damage, defender)
                }
                modifiedAttackRoll in 51..100 -> {
                    val damage = attackerFighter.hitDamage
                    logger.info("[ATTACK]:[HIT] (raw=$d100Roll,final=$modifiedAttackRoll) [${action.actor.name}] hit [${action.target.name}] for $damage damage!")
                    applyDamage(damage, defender)
                }
                modifiedAttackRoll > 100 -> {
                    val damage = ceil(attackerFighter.hitDamage * 1.25).roundToInt()
                    logger.info("[ATTACK]:[CRIT] (raw=$d100Roll,final=$modifiedAttackRoll) [${action.actor.name}] critically hit [${action.target.name}] for $damage damage!")
                    applyDamage(damage, defender)
                }
            }
        }
    }

    // TODO: Better rules
    private fun applyDamage(damage: Int, entity: Entity) {
        val hpComponent = entity.getComponent(HpComponent::class)
        hpComponent.removeHp(damage)
        if (hpComponent.currentHp < 0) {
            // TODO: "No AI == dead" is a sketchy definition of dead!
            entity.removeComponent(AIComponent::class)
            logger.info("<EVENT>:<DEATH> [${entity.name}] is dead!")
        }
    }

    private fun resolveWaitAction(action: WaitAction) {
        logger.info("[WAIT]:[SUCCESS] [${action.actor.name}] is waiting!")
    }
}
