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
        val currentNodeId = action.actor
            .getComponent(EncounterLocationComponent::class)
            .locationNodeId

        val targetNodeSameAsCurrentNode = currentNodeId == action.targetNodeId
        val targetNodeHasRoom = encounterState.getNodeHasRoom(action.actor, action.targetNodeId)
        val targetNodeReachable = encounterState.getNodeDirectlyConnected(currentNodeId, action.targetNodeId)

        if (targetNodeSameAsCurrentNode) {
            logger.info("[MOVE]:[INVALID] Target node [${encounterState.getNodeName(action.targetNodeId)}, ${action.targetNodeId}] and source node are identical!")
        } else if (!targetNodeHasRoom) {
            logger.info("[MOVE]:[INVALID] Target node [${encounterState.getNodeName(action.targetNodeId)}, ${action.targetNodeId}] full!")
        } else if (!targetNodeReachable) {
            logger.info("[MOVE]:[INVALID] Target node [${encounterState.getNodeName(action.targetNodeId)}, ${action.targetNodeId}] not adjacent!")
        } else {
            encounterState.relocateEntity(action.actor, action.targetNodeId)
            logger.info("[MOVE]:[SUCCESS] [${encounterState.getNodeName(currentNodeId)}, $currentNodeId] to [${encounterState.getNodeName(action.targetNodeId)}, ${action.targetNodeId}]")
        }
    }

    private fun resolveAttackAction(action: AttackAction, encounterState: EncounterState) {
        val attacker = action.actor
        val attackerNodeId = attacker.getComponent(EncounterLocationComponent::class).locationNodeId

        val defender = action.target
        val defenderNodeId = defender.getComponent(EncounterLocationComponent::class).locationNodeId

        // TODO: Range & visibility & such
        if (!encounterState.getNodeDirectlyConnected(attackerNodeId, defenderNodeId)) {
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
