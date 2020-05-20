package com.mtw.supplier.engine.encounter.rulebook

import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.ecs.components.*
import com.mtw.supplier.engine.ecs.components.ai.PathAIComponent
import com.mtw.supplier.engine.encounter.rulebook.actions.*
import com.mtw.supplier.engine.encounter.state.EncounterState
import com.mtw.supplier.engine.encounter.state.EncounterMessageLog

object Rulebook {

    fun resolveActions(actions: List<Action>, encounterState: EncounterState) {
        actions.forEach { resolveAction(it, encounterState) }
    }

    fun resolveAction(action: Action, encounterState: EncounterState) {
        when (action.actionType) {
            ActionType.ATTACK -> resolveAttackAction(action as AttackAction, encounterState)
            ActionType.FIRE_PROJECTILE -> resolveFireProjectileAction(action as FireProjectileAction, encounterState)
            ActionType.MOVE -> resolveMoveAction(action as MoveAction, encounterState)
            ActionType.USE_ITEM -> TODO()
            ActionType.WAIT -> resolveWaitAction(action as WaitAction, encounterState.messageLog)
            ActionType.SELF_DESTRUCT -> resolveSelfDestructionAction(action as SelfDestructAction, encounterState)
        }
    }

    /**
     * Resolves attack against defense with a straight damage = power - defense formula.
     */
    private fun resolveAttackAction(action: AttackAction, encounterState: EncounterState) {
        val attacker = action.actor
        val defender = action.target

        if (!encounterState.arePositionsAdjacent(
                attacker.getComponent(EncounterLocationComponent::class).position,
                defender.getComponent(EncounterLocationComponent::class).position)) {
            encounterState.messageLog.logAction(action, "INVALID", "[${action.actor.name}] cannot reach [${action.target.name}]")
        } else {
            val damage = attacker.getComponent(AttackerComponent::class).power -
                defender.getComponent(DefenderComponent::class).defense
            applyDamage(damage, defender, encounterState)
        }
    }

    /**
     * Applies damage directly to HP, bypassing any other factors.
     */
    private fun applyDamage(damage: Int, entity: Entity, encounterState: EncounterState) {
        val hpComponent = entity.getComponent(DefenderComponent::class)
        hpComponent.removeHp(damage)
        if (hpComponent.currentHp < 0) {
            encounterState.removeEntity(entity)
            encounterState.messageLog.logEvent("DEATH", "[${entity.name}] is dead!")
        }
    }

    private fun resolveFireProjectileAction(action: FireProjectileAction, encounterState: EncounterState) {
        val shooterPos = action.actor.getComponent(EncounterLocationComponent::class).position
        repeat (action.numProjectiles) {
            val path = action.pathBuilder.build(shooterPos)
            val projectile = Entity(action.projectileType.displayName, seededRand = encounterState.seededRand)
                .addComponent(PathAIComponent(path))
                .addComponent(AttackerComponent(action.power))
                .addComponent(CollisionComponent.defaultProjectile())
                .addComponent(ActionTimeComponent(action.speed))
                .addComponent(SpeedComponent(action.speed))
                // TODO: Differentiate display on projectile type maybe?
                .addComponent(DisplayComponent(DisplayType.PROJECTILE_SMALL_SHOTGUN, false))
            encounterState.placeEntity(projectile, path.currentPosition(), ignoreCollision = true)

            encounterState.messageLog.logAction(action, "SUCCESS",
                "${action.actor.name} at $shooterPos fired ${action.projectileType} from ${path.currentPosition()}")
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
            encounterState.messageLog.logAction(action, "INVALID", "Target node ${action.targetPosition} and source node are identical!")
        } else if (targetNodeBlocked) {
            val collisionComponent = action.actor.getComponent(CollisionComponent::class)
            if (collisionComponent.attackOnHit) {
                val blockingEntity = encounterState.getBlockingEntityAtPosition(action.targetPosition)
                if (blockingEntity != null) {
                    resolveAction(AttackAction(action.actor, blockingEntity), encounterState)
                }
            }
            if (collisionComponent.selfDestructOnHit) {
                resolveAction(SelfDestructAction(action.actor), encounterState)
            } else {
                encounterState.messageLog.logAction(action, "INVALID", "Target node ${action.targetPosition} blocked!")
            }
        } else if (!targetNodeAdjacent) {
            encounterState.messageLog.logAction(action, "INVALID", "Current node $currentPosition is not adjacent to target node ${action.targetPosition}!")
        } else {
            encounterState.teleportEntity(action.actor, action.targetPosition)
            encounterState.messageLog.logAction(action, "SUCCESS", "${action.actor.name} $currentPosition to ${action.targetPosition}")
        }
    }

    private fun resolveWaitAction(action: WaitAction, messageLog: EncounterMessageLog) {
        messageLog.logAction(action, "SUCCESS", "[${action.actor.name}] is waiting!")
    }

    private fun resolveSelfDestructionAction(action: SelfDestructAction, encounterState: EncounterState) {
        encounterState.removeEntity(action.actor)
        encounterState.messageLog.logAction(action, "SUCCESS", "[${action.actor.name}] self-destructed!")
    }
}
