package com.mtw.supplier.server

import com.mtw.supplier.engine.Serializers
import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.ecs.EntityDef
import com.mtw.supplier.engine.ecs.components.*
import com.mtw.supplier.engine.ecs.components.ai.AIComponent
import com.mtw.supplier.engine.ecs.components.ai.EnemyScoutAIComponent
import com.mtw.supplier.engine.ecs.components.item.InventoryComponent
import com.mtw.supplier.engine.encounter.EncounterRunner
import com.mtw.supplier.engine.encounter.rulebook.Action
import com.mtw.supplier.engine.encounter.rulebook.actions.MoveAction
import com.mtw.supplier.engine.encounter.rulebook.actions.WaitAction
import com.mtw.supplier.engine.utils.XYCoordinates
import com.mtw.supplier.engine.encounter.state.EncounterState
import com.mtw.supplier.engine.utils.Constants
import com.mtw.supplier.engine.utils.SeededRand
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import kotlin.random.Random
import kotlin.system.measureTimeMillis

@SpringBootApplication
class SupplierApplication


@RestController
class RootController {
	private val logger = LoggerFactory.getLogger(RootController::class.java)
	private var gameState: EncounterState = generateNewGameState()
	private val hackMetrics: MutableList<Pair<String, Long>> = mutableListOf()

	@PostMapping("/game/reset")
	fun gameReset(): String {
		gameState = generateNewGameState()
		EncounterRunner.runUntilPlayerReady(gameState)
		return Serializers.stringify(gameState)
	}

	@GetMapping("/game/state")
	fun gameState(): String {
		return Serializers.stringify(gameState)
	}

	@GetMapping("/metrics/hack")
	fun hackMetrics(): MutableList<Pair<String, Long>> {
		return hackMetrics
	}

	@PostMapping("game/player/action")
	fun gamePlayerAction(@RequestBody action: String): String {
		val turnTime = measureTimeMillis {
			EncounterRunner.runPlayerTurnAndUntilReady(gameState, Serializers.parseAction(action))
		}
		hackMetrics.add(Pair("", turnTime))

		return Serializers.stringify(gameState)
	}

	private fun buildActivateAndPlace(entityDef: EntityDef, state: EncounterState, x: Int, y: Int) {
		val entity = entityDef.build(state.seededRand)
		entity.getComponentOrNull(AIComponent::class)?.isActive = true
		state.placeEntity(entity, XYCoordinates(x, y))
	}

	// TODO: Proppa level gen & not literally in controller lol
	private final fun generateNewGameState(): EncounterState {
		val consistentRand = SeededRand(10)

		val player = Entity("player", seededRand = consistentRand)
			.addComponent(PlayerComponent())
			.addComponent(InventoryComponent())
			.addComponent(DefenderComponent(9999, 50, 50))
			.addComponent(FactionComponent(2))
			.addComponent(CollisionComponent.defaultFighter())
			.addComponent(ActionTimeComponent(100))
			.addComponent(SpeedComponent(100))
			.addComponent(DisplayComponent(DisplayType.PLAYER, false))
		val state = EncounterState(consistentRand, Constants.MAP_WIDTH, Constants.MAP_HEIGHT)
		state.initialize(player)

		/*
		state.removeEntity(player)
		state.placeEntity(player, XYCoordinates(25, 25))

		buildActivateAndPlace(EntityDef.SCOUT, state, 10, 10)
		buildActivateAndPlace(EntityDef.FIGHTER, state, 10, 20)
		buildActivateAndPlace(EntityDef.GUNSHIP, state, 10, 30)
		*/

		EncounterRunner.runUntilPlayerReady(state)

		return state
	}
}

fun main(args: Array<String>) {
	runApplication<SupplierApplication>(*args)
}
