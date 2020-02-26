package com.mtw.supplier

import com.mtw.supplier.ecs.Entity
import com.mtw.supplier.ecs.components.*
import com.mtw.supplier.ecs.components.ai.TestAIComponent
import com.mtw.supplier.encounter.EncounterRunner
import com.mtw.supplier.encounter.rulebook.actions.MoveAction
import com.mtw.supplier.encounter.rulebook.actions.WaitAction
import com.mtw.supplier.utils.XYCoordinates
import com.mtw.supplier.encounter.state.EncounterState
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
	private final val gameModule = Serializers.componentSerializersModuleBuilder()
	private val json = Json(JsonConfiguration.Stable.copy(prettyPrint = true), context = gameModule)

	private var gameState: EncounterState = generateNewGameState()

	@PostMapping("/game/reset")
	fun gameReset(): String {
		gameState = generateNewGameState()
		EncounterRunner.runUntilPlayerReady(gameState)
		return json.stringify(EncounterState.serializer(), gameState)
	}

	@GetMapping("/game/state")
	fun gameState(): String {
		return json.stringify(EncounterState.serializer(), gameState)
	}

	@PostMapping("/game/player/action/move")
	fun gamePlayerActionMove(@RequestBody request: ActionMoveRequest): String {
		val oldPlayerPos = gameState.playerEntity().getComponent(EncounterLocationComponent::class).position
		val newPlayerPos = oldPlayerPos.copy(
			x = oldPlayerPos.x + request.direction.dx, y = oldPlayerPos.y + request.direction.dy)

		val action = MoveAction(gameState.playerEntity(), newPlayerPos)
		EncounterRunner.runPlayerTurn(gameState, action)
		EncounterRunner.runUntilPlayerReady(gameState)

		return json.stringify(EncounterState.serializer(), gameState)
	}

	@PostMapping("/game/player/action/wait")
	fun gamePlayerActionWait(): String {
		val action = WaitAction(gameState.playerEntity())
		EncounterRunner.runPlayerTurn(gameState, action)
		EncounterRunner.runUntilPlayerReady(gameState)

		return json.stringify(EncounterState.serializer(), gameState)
	}

	// TODO: Proppa level gen & not literally in controller lol
	private final fun generateNewGameState(): EncounterState {
		val state = EncounterState(15, 7)

		val wolf = Entity(state.getNextEntityId(), "wolf")
			.addComponent(TestAIComponent())
			.addComponent(HpComponent(20, 20))
			.addComponent(FighterComponent(5, 5, 5))
			.addComponent(FactionComponent(0))
			.addComponent(CollisionComponent(true))
			.addComponent(ActionTimeComponent(5))
			.addComponent(SpeedComponent(5))
		val mercenary = Entity(state.getNextEntityId(), "strongMercenary")
			.addComponent(TestAIComponent())
			.addComponent(HpComponent(50, 50))
			.addComponent(FighterComponent(5, 100, 100))
			.addComponent(FactionComponent(2))
			.addComponent(CollisionComponent(true))
			.addComponent(ActionTimeComponent(30))
			.addComponent(SpeedComponent(30))
		val player = Entity(state.getNextEntityId(), "player")
			.addComponent(PlayerComponent())
			.addComponent(HpComponent(50, 50))
			.addComponent(FighterComponent(5, 100, 100))
			.addComponent(FactionComponent(2))
			.addComponent(CollisionComponent(true))
			.addComponent(ActionTimeComponent(5))
			.addComponent(SpeedComponent(20))

		state.placeEntity(wolf, XYCoordinates(0, 1))
			 .placeEntity(mercenary, XYCoordinates(12, 5))
			 .placeEntity(player, XYCoordinates(5, 3))
		EncounterRunner.runNextActiveTick(state)
		return state
	}
}

fun main(args: Array<String>) {
	runApplication<SupplierApplication>(*args)
}
