package com.mtw.supplier.server

import com.mtw.supplier.engine.Serializers
import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.ecs.components.*
import com.mtw.supplier.engine.ecs.components.ai.EnemyScoutAIComponent
import com.mtw.supplier.engine.encounter.EncounterRunner
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
	private var gameState: EncounterState = generateNewGameState()

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

	@PostMapping("/game/player/action/move")
	fun gamePlayerActionMove(@RequestBody request: ActionMoveRequest): String {
		val oldPlayerPos = gameState.playerEntity().getComponent(EncounterLocationComponent::class).position
		val newPlayerPos = oldPlayerPos.copy(
			x = oldPlayerPos.x + request.direction.dx, y = oldPlayerPos.y + request.direction.dy)

		val action = MoveAction(gameState.playerEntity(), newPlayerPos)
		EncounterRunner.runPlayerTurn(gameState, action)
		EncounterRunner.runUntilPlayerReady(gameState)

		return Serializers.stringify(gameState)
	}

	@PostMapping("/game/player/action/wait")
	fun gamePlayerActionWait(): String {
		val action = WaitAction(gameState.playerEntity())
		EncounterRunner.runPlayerTurn(gameState, action)
		EncounterRunner.runUntilPlayerReady(gameState)

		return Serializers.stringify(gameState)
	}

	// TODO: Proppa level gen & not literally in controller lol
	private final fun generateNewGameState(): EncounterState {
		val state = EncounterState(40, 40)

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
		val player = Entity(state.getNextEntityId(), "player")
			.addComponent(PlayerComponent())
			.addComponent(DefenderComponent(0, 50, 50))
			.addComponent(FactionComponent(2))
			.addComponent(CollisionComponent.defaultFighter())
			.addComponent(ActionTimeComponent(100))
			.addComponent(SpeedComponent(100))
			.addComponent(DisplayComponent(DisplayType.PLAYER, false))

		state.placeEntity(scout, XYCoordinates(10, 10))
			 .placeEntity(player, XYCoordinates(25, 25))
		EncounterRunner.runUntilPlayerReady(state)
		return state
	}
}

fun main(args: Array<String>) {
	runApplication<SupplierApplication>(*args)
}
