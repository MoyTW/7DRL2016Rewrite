package com.mtw.supplier.engine.encounter

import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.ecs.components.*
import com.mtw.supplier.engine.ecs.components.ai.AIComponent
import com.mtw.supplier.engine.ecs.components.ai.PathAIComponent
import com.mtw.supplier.engine.encounter.rulebook.Action
import com.mtw.supplier.engine.encounter.state.EncounterState
import com.mtw.supplier.engine.encounter.rulebook.Rulebook
import com.mtw.supplier.engine.utils.LinePathBuilder
import org.slf4j.LoggerFactory

/**
 * ===== ONE TURN =====
 * Formal description of desired turn behaviour:
 * 1. List oll entities ready to move
 * 2. If the player is on the list, the player always goes first
 *   2a. Player gets infinite free actions
 *   2b. Player ends turn on move action
 *   2c. On move turn, fires a laser projectile with TTL 1 and speed 1
 * 3. All other non-projectile entities go in placement order
 *   3a. Projectiles launched by entities *do not* take an immediate turn
 * 4. All projectiles go // TODO: Projectiles & entities interleaved
 *   4a. Yes this does mean that an entity can move in front of a projectile
 * 5. Check for victory conditions // TODO: Victory conditions
 */

object EncounterRunner {
    private val logger = LoggerFactory.getLogger(EncounterRunner::class.java)

    private fun ticksToNextEvent(encounterState: EncounterState): Int {
        var ticksToNext = 99999999
        for (entity in encounterState.entities()) {
            val toNext = entity.getComponentOrNull(ActionTimeComponent::class)?.ticksUntilTurn
            if (toNext != null && toNext < ticksToNext) {
                ticksToNext = toNext
            }
        }
        return ticksToNext
    }

    private fun passTimeAndGetReadyEntities(encounterState: EncounterState, ticks: Int): MutableList<Entity> {
        val readyEntities = mutableListOf<Entity>()

        for (entity in encounterState.entities()) {
            val actionTimeComponent = entity.getComponentOrNull(ActionTimeComponent::class)
            if (actionTimeComponent != null) {
                actionTimeComponent.passTime(ticks)
                if (actionTimeComponent.isReady()) {
                    readyEntities.add(entity)
                }
            }
        }

        return readyEntities
    }

    private fun fireLaser(encounterState: EncounterState, player: Entity) {
        val hostileEntities = encounterState.entities().filter {
            it.hasComponent(AIComponent::class) && it.hasComponent(FactionComponent::class) }
        // TODO: Range and FOV stuff
        if (hostileEntities.isNotEmpty()) {
            val playerPos = player.getComponent(EncounterLocationComponent::class).position

            val target = hostileEntities[0]
            val pathBuilder = LinePathBuilder(target.getComponent(EncounterLocationComponent::class).position,
                encounterState.seededRand)
            // TODO: Do damage
            //Rulebook.resolveAction(FireProjectileAction(player, 0, pathBuilder, 0, ProjectileType.LASER), encounterState)
        }
    }

    fun runPlayerTurnAndUntilReady(encounterState: EncounterState, playerAction: Action) {
        if (encounterState.completed) { return }

        val actor = encounterState.getEntity(playerAction.actorId)

        // Take player action
        Rulebook.resolveAction(playerAction, encounterState)

        // TODO: Allow non-players to use free actions (not necessary for original design though)
        if (!playerAction.freeAction) {
            val speedComponent = actor.getComponent(SpeedComponent::class)
            actor.getComponent(ActionTimeComponent::class).endTurn(speedComponent)

            // Update the FoV for the player
            encounterState.calculatePlayerFoVAndMarkExploration()

            // Shoot the player's laser
            fireLaser(encounterState, actor)

            runUntilPlayerReady(encounterState)
        }
    }

    fun runUntilPlayerReady(encounterState: EncounterState) {
        if (encounterState.completed) { return }

        var isPlayerReady = runNextActiveTick(encounterState)
        while (!isPlayerReady && !encounterState.completed) {
            isPlayerReady = runNextActiveTick(encounterState)
        }
        encounterState.calculatePlayerFoVAndMarkExploration()
        // TODO: Figure out why I wrote the below code - it shouldn't be necessary.
        /**
         * It's looking for path AI components and trying to force them to expire, essentially - so it's cleaning up
         * path components before the player is allowed to make another move. I *think* the principle it's going for is
         * "path entities resolve before other entities always" but runNextActiveTick doesn't hold that to be true, they
         * operate in speed/creation order. So if I want to have "actor phase" and "projectile phase" I should split it
         * there as well.
         */
        /*encounterState.entities().filter { it.hasComponent(PathAIComponent::class) }.map {
            if (it.getComponent(PathAIComponent::class).path.atEnd()
                //|| it.getComponent(ActionTimeComponent::class).isReady()
            ) {
                val nextActions = it.getComponent(AIComponent::class).decideNextActions(encounterState)
                logger.debug("Actions: $nextActions")
                Rulebook.resolveActions(nextActions, encounterState)
                val speedComponent = it.getComponent(SpeedComponent::class)
                it.getComponent(ActionTimeComponent::class).endTurn(speedComponent)
            }
        }*/
    }

    private fun runNextActiveTick(encounterState: EncounterState): Boolean {
        if (encounterState.completed) { return false }

        // Run the clock until the next entity is ready
        val ticksToNext = ticksToNextEvent(encounterState)
        val readyEntities = passTimeAndGetReadyEntities(encounterState, ticksToNext)
        encounterState.advanceTime(ticksToNext)

        // If the player is the next ready entity, abort
        if (readyEntities.first().hasComponent(PlayerComponent::class)) {
            return true
        }

        //logger.info("========== START OF TURN ${encounterState.currentTime} ==========")
        // TODO: Caching of various iterables, if crawling nodes is slow?
        while (readyEntities.isNotEmpty() && !readyEntities.first().hasComponent(PlayerComponent::class)) {
            val entity = readyEntities.first()
            readyEntities.removeAt(0)
            if (entity.hasComponent(AIComponent::class)) {
                val nextActions = entity.getComponent(AIComponent::class).decideNextActions(encounterState)
                logger.debug("Actions: $nextActions")
                Rulebook.resolveActions(nextActions, encounterState)
                val speedComponent = entity.getComponent(SpeedComponent::class)
                entity.getComponent(ActionTimeComponent::class).endTurn(speedComponent)
            }
        }

        // lol
        val remainingAIEntities = encounterState.entities().filter {
            it.hasComponent(AIComponent::class) && it.hasComponent(FactionComponent::class)
        }
        val anyHostileRelationships = remainingAIEntities.any { leftEntity ->
            val faction = leftEntity.getComponent(FactionComponent::class)
            remainingAIEntities.any { rightEntity ->
                faction.isHostileTo(rightEntity.id, encounterState)
            } || faction.isHostileTo(encounterState.playerEntity().id, encounterState)
        }
        if (!anyHostileRelationships) {
            logger.info("!!!!!!!!!! ENCOUNTER HAS NO REMAINING HOSTILES, SHOULD END! !!!!!!!!!!")
            encounterState.completeEncounter()
        }

        //logger.info("========== END OF TURN ${encounterState.currentTime} ==========")
        return false
    }
}
