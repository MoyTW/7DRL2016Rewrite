package com.mtw.supplier.encounter

import com.mtw.supplier.ecs.Entity
import com.mtw.supplier.ecs.components.AIComponent
import com.mtw.supplier.ecs.components.ActionTimeComponent
import com.mtw.supplier.ecs.components.FactionComponent
import com.mtw.supplier.ecs.components.SpeedComponent
import com.mtw.supplier.encounter.state.EncounterState
import com.mtw.supplier.encounter.rulebook.Rulebook
import org.slf4j.LoggerFactory

class EncounterRunner {

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

    private fun passTimeAndGetReadyEntities(encounterState: EncounterState, ticks: Int): List<Entity> {
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

    fun runTurn(encounterState: EncounterState) {
        val ticksToNext = ticksToNextEvent(encounterState)
        val readyEntities = passTimeAndGetReadyEntities(encounterState, ticksToNext)
        encounterState.advanceTime(ticksToNext)

        logger.info("========== START OF TURN ${encounterState.currentTime} ==========")
        // TODO: Caching of various iterables, if crawling nodes is slow?
        for(entity in readyEntities) {
            if (entity.hasComponent(AIComponent::class)) {
                val nextAction = entity.getComponent(AIComponent::class).decideNextAction(encounterState)
                logger.debug("Action: $nextAction")
                Rulebook.resolveAction(nextAction, encounterState)
                val speedComponent = entity.getComponent(SpeedComponent::class)
                entity.getComponent(ActionTimeComponent::class).endTurn(speedComponent)
            }
        }

        // lol
        val remainingAIEntities = encounterState.entities().filter { it.hasComponent(AIComponent::class) }
        val anyHostileRelationships = remainingAIEntities.any { leftEntity ->
            remainingAIEntities.any { rightEntity ->
                leftEntity.getComponent(FactionComponent::class).isHostileTo(rightEntity.id, encounterState)
            }
        }
        if (!anyHostileRelationships) {
            logger.info("!!!!!!!!!! ENCOUNTER HAS NO REMAINING HOSTILES, SHOULD END! !!!!!!!!!!")
            encounterState.completeEncounter()
        }

        logger.info("========== END OF TURN ${encounterState.currentTime} ==========")
    }

    fun runEncounter(encounterState: EncounterState, timeLimit: Int = 1000) {
        when {
            encounterState.completed -> throw CannotRunCompletedEncounterException()
            encounterState.currentTime >= timeLimit -> throw CannotRunTimeLimitedException()
            else -> {
                while (!encounterState.completed && encounterState.currentTime < timeLimit) {
                    this.runTurn(encounterState)
                }
            }
        }
    }

    class CannotRunCompletedEncounterException : Exception("Cannot run next turn on a completed encounter!")
    class CannotRunTimeLimitedException : Exception("Cannot run next turn on an encounter past the time limit!")

    companion object {
        private val logger = LoggerFactory.getLogger(EncounterRunner::class.java)
    }
}
