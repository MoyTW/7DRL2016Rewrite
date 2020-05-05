package com.mtw.supplier.server

import com.mtw.supplier.engine.Serializers
import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.ecs.components.*
import com.mtw.supplier.engine.ecs.components.ai.EnemyScoutAIComponent
import com.mtw.supplier.engine.encounter.EncounterRunner
import com.mtw.supplier.engine.encounter.EncounterStateHistory
import com.mtw.supplier.engine.encounter.rulebook.actions.MoveAction
import com.mtw.supplier.engine.encounter.rulebook.actions.WaitAction
import com.mtw.supplier.engine.utils.XYCoordinates
import com.mtw.supplier.engine.encounter.state.EncounterState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class SupplierApplication


enum class Direction(val dx: Int, val dy: Int) {
	N(0, 1),
	NE(1, 1),
	E(1, 0),
	SE(1, -1),
	S(0, -1),
	SW(-1, -1),
	W(-1, 0),
	NW(-1, 1)
}
data class ActionMoveRequest(val direction: Direction)

@RestController
class RootController {
	private var gameState: EncounterState = generateNewGameState(EncounterStateHistory())

	@PostMapping("/game/reset")
	fun gameReset(): String {
		val history = EncounterStateHistory()
		gameState = generateNewGameState(history)
		return Serializers.stringify(gameState)
	}

	@GetMapping("/game/state")
	fun gameState(): String {
		return Serializers.stringify(gameState)
	}

	@PostMapping("/game/player/action/move")
	fun gamePlayerActionMove(@RequestBody request: ActionMoveRequest): String {
		val oldPlayerPos = gameState.playerEntity().getComponent(EncounterLocationComponent::class).position
		val newPlayerPos = oldPlayerPos.copy(
			x = oldPlayerPos.x + request.direction.dx, y = oldPlayerPos.y + request.direction.dy)

		val action = MoveAction(gameState.playerEntity(), newPlayerPos)
		val history = EncounterStateHistory()
		EncounterRunner.runPlayerTurn(gameState, action, history)
		EncounterRunner.runUntilPlayerReady(gameState, history)

		return Serializers.stringify(gameState)
	}

	@PostMapping("/game/player/action/wait")
	fun gamePlayerActionWait(): String {
		val action = WaitAction(gameState.playerEntity())
		val history = EncounterStateHistory()
		EncounterRunner.runPlayerTurn(gameState, action, history)
		EncounterRunner.runUntilPlayerReady(gameState, history)

		return Serializers.stringify(gameState)
	}

	private fun makeAndPlaceScout(state: EncounterState, x: Int, y: Int) {
		val activatedAi = EnemyScoutAIComponent()
		activatedAi.isActive = true
		val scout = Entity(state.getNextEntityId(), "Scout")
			.addComponent(activatedAi)
			.addComponent(DefenderComponent(0, 10, 10))
			.addComponent(FactionComponent(0))
			.addComponent(CollisionComponent.defaultFighter())
			.addComponent(ActionTimeComponent(75))
			.addComponent(SpeedComponent(75))
			.addComponent(DisplayComponent(DisplayType.ENEMY_SCOUT, false))
		state.placeEntity(scout, XYCoordinates(x, y))
	}

	// TODO: Proppa level gen & not literally in controller lol
	private final fun generateNewGameState(history: EncounterStateHistory): EncounterState {
		val state = EncounterState(40, 40)

		val player = Entity(state.getNextEntityId(), "player")
			.addComponent(PlayerComponent())
			.addComponent(DefenderComponent(0, 50, 50))
			.addComponent(FactionComponent(2))
			.addComponent(CollisionComponent.defaultFighter())
			.addComponent(ActionTimeComponent(100))
			.addComponent(SpeedComponent(100))
			.addComponent(DisplayComponent(DisplayType.PLAYER, false))
		state.placeEntity(player, XYCoordinates(25, 25))

		makeAndPlaceScout(state, 10, 20)
		makeAndPlaceScout(state, 10, 18)
		makeAndPlaceScout(state, 10, 16)
		makeAndPlaceScout(state, 10, 14)
		makeAndPlaceScout(state, 10, 12)
		makeAndPlaceScout(state, 10, 10)
		makeAndPlaceScout(state, 10, 8)
		makeAndPlaceScout(state, 10, 6)
		makeAndPlaceScout(state, 10, 4)
		makeAndPlaceScout(state, 10, 2)

		history.recordState(state)
		EncounterRunner.runUntilPlayerReady(state, history)

		return state
	}
}

fun main(args: Array<String>) {
	runApplication<SupplierApplication>(*args)
}
