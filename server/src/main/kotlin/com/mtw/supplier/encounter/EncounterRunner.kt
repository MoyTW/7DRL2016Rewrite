package com.mtw.supplier.encounter

import com.mtw.supplier.ecs.components.AIComponent
import com.mtw.supplier.ecs.components.FactionComponent
import com.mtw.supplier.encounter.state.EncounterState
import com.mtw.supplier.encounter.rulebook.Rulebook
import org.slf4j.LoggerFactory

class EncounterRunner {
    fun runTurn(encounterState: EncounterState) {
        logger.info("========== START OF TURN ${encounterState.currentTime} ==========")
        // TODO: Caching of various iterables, if crawling nodes is slow?
        for(entity in encounterState.entities()) {
            if (entity.hasComponent(AIComponent::class)) {
                val nextAction = entity.getComponent(AIComponent::class).decideNextAction(encounterState)
                logger.debug("Action: $nextAction")
                Rulebook.resolveAction(nextAction, encounterState)
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
        encounterState.advanceTime()
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
