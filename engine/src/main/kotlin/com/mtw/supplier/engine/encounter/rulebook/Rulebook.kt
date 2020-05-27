package com.mtw.supplier.engine.encounter.rulebook

import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.ecs.components.*
import com.mtw.supplier.engine.ecs.components.ai.PathAIComponent
import com.mtw.supplier.engine.ecs.components.item.CarryableComponent
import com.mtw.supplier.engine.ecs.components.item.InventoryComponent
import com.mtw.supplier.engine.encounter.EncounterRunner
import com.mtw.supplier.engine.encounter.rulebook.actions.*
import com.mtw.supplier.engine.encounter.state.EncounterState
import com.mtw.supplier.engine.encounter.state.EncounterMessageLog
import com.mtw.supplier.engine.encounter.state.EncounterStateUtils
import com.mtw.supplier.engine.utils.XYCoordinates

object Rulebook {

    fun resolveActions(actions: List<Action>, encounterState: EncounterState) {
        actions.forEach { resolveAction(it, encounterState) }
    }

    fun resolveAction(action: Action, encounterState: EncounterState) {
        when (action.actionType) {
            ActionType.ATTACK -> resolveAttackAction(action as AttackAction, encounterState)
            ActionType.AUTOPILOT -> resolveAutopilotAction(action as AutopilotAction, encounterState)
            ActionType.FIRE_PROJECTILE -> resolveFireProjectileAction(action as FireProjectileAction, encounterState)
            ActionType.PICK_UP_ITEM -> resolvePickUpItemAction(action as PickUpItemAction, encounterState)
            ActionType.MOVE -> resolveMoveAction(action as MoveAction, encounterState)
            ActionType.WAIT -> resolveWaitAction(action as WaitAction, encounterState)
            ActionType.SELF_DESTRUCT -> resolveSelfDestructionAction(action as SelfDestructAction, encounterState)
        }
    }

    /**
     * Resolves attack against defense with a straight damage = power - defense formula.
     */
    private fun resolveAttackAction(action: AttackAction, encounterState: EncounterState) {
        val attacker = encounterState.getEntity(action.actorId)
        val defender = action.target

        if (!encounterState.arePositionsAdjacent(
                attacker.getComponent(EncounterLocationComponent::class).position,
                defender.getComponent(EncounterLocationComponent::class).position)) {
            encounterState.messageLog.logAction(action, "INVALID", "[${attacker.name}] cannot reach [${defender.name}]")
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

    private fun resolveAutopilotAction(action: AutopilotAction, encounterState: EncounterState) {
        val player = encounterState.getEntity(action.actorId)
        val encounterLocationComponent = player.getComponent(EncounterLocationComponent::class)

        val path = EncounterStateUtils.aStarWithNewGrid(
            startPos = encounterLocationComponent.position,
            endPos = XYCoordinates(5, 5), // TODO: Zones!
            encounterState = encounterState
        ) ?: throw TODO("Gracefully handle obstructed destination with a tasteful user prompt or something")

        var idx = 0

        while (idx < path.size) {
            val nextMoveAction = MoveAction(player.id, path[idx])
            EncounterRunner.runPlayerTurnAndUntilReady(encounterState, nextMoveAction)
            // TODO: Autopilot cessation checks
            idx++
        }
    }

    private fun resolveFireProjectileAction(action: FireProjectileAction, encounterState: EncounterState) {
        val actor = encounterState.getEntity(action.actorId)
        val shooterPos = actor.getComponent(EncounterLocationComponent::class).position
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
                "${actor.name} at $shooterPos fired ${action.projectileType} from ${path.currentPosition()}")
        }
    }

    private fun resolvePickUpItemAction(action: PickUpItemAction, encounterState: EncounterState) {
        val actor = encounterState.getEntity(action.actorId)
        val actorInventory = actor.getComponentOrNull(InventoryComponent::class)
            ?: throw CannotPickUpItemException("${actor.name} cannot pick up items as it has no inventory!")
        val actorLocation = actor.getComponentOrNull(EncounterLocationComponent::class)
            ?: throw CannotPickUpItemException("${actor.name} cannot pick up items as it's not on a map!")

        val targetEntity = encounterState.getEntitiesAtPosition(actorLocation.position).filter {
            it.hasComponent(CarryableComponent::class)
        }.firstOrNull() // NOTE: If we ever have multiple entities on a square we'll want to allow the user to choose!
        if (targetEntity != null) {
            encounterState.removeEntity(targetEntity)
            actorInventory.addItem(targetEntity)
            encounterState.messageLog.logAction(action, "SUCCESS", "${actor.name} picked up ${targetEntity.name}!")
        }
    }
    class CannotPickUpItemException(message: String): Exception(message)

    private fun resolveMoveAction(action: MoveAction, encounterState: EncounterState) {
        val actor = encounterState.getEntity(action.actorId)
        val currentPosition = actor
            .getComponent(EncounterLocationComponent::class)
            .position

        val targetNodeSameAsCurrentNode = currentPosition == action.targetPosition
        val targetNodeBlocked = encounterState.positionBlocked(action.targetPosition)
        val targetNodeAdjacent = encounterState.arePositionsAdjacent(currentPosition, action.targetPosition)

        if (targetNodeSameAsCurrentNode) {
            encounterState.messageLog.logAction(action, "INVALID", "Target node ${action.targetPosition} and source node are identical!")
        } else if (targetNodeBlocked) {
            val collisionComponent = actor.getComponent(CollisionComponent::class)
            if (collisionComponent.attackOnHit) {
                val blockingEntity = encounterState.getBlockingEntityAtPosition(action.targetPosition)
                if (blockingEntity != null) {
                    resolveAction(AttackAction(actor.id, blockingEntity), encounterState)
                }
            }
            if (collisionComponent.selfDestructOnHit) {
                resolveAction(SelfDestructAction(actor.id), encounterState)
            } else {
                encounterState.messageLog.logAction(action, "INVALID", "Target node ${action.targetPosition} blocked!")
            }
        } else if (!targetNodeAdjacent) {
            encounterState.messageLog.logAction(action, "INVALID", "Current node $currentPosition is not adjacent to target node ${action.targetPosition}!")
        } else {
            encounterState.teleportEntity(actor, action.targetPosition)
            encounterState.messageLog.logAction(action, "SUCCESS", "${actor.name} $currentPosition to ${action.targetPosition}")
        }
    }

    private fun resolveWaitAction(action: WaitAction, encounterState: EncounterState) {
        val actor = encounterState.getEntity(action.actorId)
        encounterState.messageLog.logAction(action, "SUCCESS", "[${actor.name}] is waiting!")
    }

    private fun resolveSelfDestructionAction(action: SelfDestructAction, encounterState: EncounterState) {
        val actor = encounterState.getEntity(action.actorId)
        encounterState.removeEntity(actor)
        encounterState.messageLog.logAction(action, "SUCCESS", "[${actor.name}] self-destructed!")
    }
}
