package com.mtw.supplier

import com.mtw.supplier.ecs.Entity
import com.mtw.supplier.ecs.components.*
import com.mtw.supplier.encounter.state.EncounterPosition
import com.mtw.supplier.encounter.state.EncounterState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class SupplierApplication

@RestController
class RootController {
	private final val gameModule = Serializers.componentSerializersModuleBuilder()
	private val json = Json(JsonConfiguration.Stable.copy(prettyPrint = true), context = gameModule)

	private var gameState: EncounterState = generateNewGameState()

	@PostMapping("/game/reset")
	fun gameReset(): String {
		gameState = generateNewGameState()
		return json.stringify(EncounterState.serializer(), gameState)
	}

	@GetMapping("/game/state")
	fun gameState(): String {
		return json.stringify(EncounterState.serializer(), gameState)
	}

	// TODO: Proppa level gen & not literally in controller lol
	private final fun generateNewGameState(): EncounterState {
		val wolf = Entity(1, "wolf")
			.addComponent(AIComponent())
			.addComponent(HpComponent(20, 20))
			.addComponent(FighterComponent(5, 5, 5))
			.addComponent(FactionComponent(0))
			.addComponent(CollisionComponent(true))
			.addComponent(ActionTimeComponent(5))
			.addComponent(SpeedComponent(5))
		val mercenary = Entity(2, "strongMercenary")
			.addComponent(AIComponent())
			.addComponent(HpComponent(50, 50))
			.addComponent(FighterComponent(5, 100, 100))
			.addComponent(FactionComponent(2))
			.addComponent(CollisionComponent(true))
			.addComponent(ActionTimeComponent(30))
			.addComponent(SpeedComponent(30))

		return EncounterState(15, 7)
			.placeEntity(wolf, EncounterPosition(0, 1))
			.placeEntity(mercenary, EncounterPosition(12, 5))
	}
}

fun main(args: Array<String>) {
	runApplication<SupplierApplication>(*args)
}
