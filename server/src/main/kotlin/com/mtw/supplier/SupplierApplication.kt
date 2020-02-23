package com.mtw.supplier

import com.mtw.supplier.ecs.Component
import com.mtw.supplier.ecs.Entity
import com.mtw.supplier.ecs.components.*
import com.mtw.supplier.encounter.EncounterRunner
import com.mtw.supplier.encounter.state.EncounterPosition
import com.mtw.supplier.encounter.state.EncounterState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerializersModule
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class SupplierApplication

@RestController
class RootController {
	private final val gameModule = SerializersModule {
		polymorphic(Component::class) {
			AIComponent::class with AIComponent.serializer()
			EncounterLocationComponent::class with EncounterLocationComponent.serializer()
			HpComponent::class with HpComponent.serializer()
			FighterComponent::class with FighterComponent.serializer()
			FactionComponent::class with FactionComponent.serializer()
			CollisionComponent::class with CollisionComponent.serializer()
			ActionTimeComponent::class with ActionTimeComponent.serializer()
			SpeedComponent::class with SpeedComponent.serializer()
		}
	}
	private val json = Json(JsonConfiguration.Stable.copy(prettyPrint = true), context = gameModule)

	@GetMapping("/health")
	fun health(): Boolean{
		return true
	}

	@GetMapping("/game")
	fun game(): String {
		val fighterOne = Entity(1, "wolf")
			.addComponent(AIComponent())
			.addComponent(HpComponent(20, 20))
			.addComponent(FighterComponent(5, 5, 5))
			.addComponent(FactionComponent(0))
			.addComponent(CollisionComponent(true))
			.addComponent(ActionTimeComponent(5))
			.addComponent(SpeedComponent(5))
		val fighterTwo = Entity(2, "strongMercenary")
			.addComponent(AIComponent())
			.addComponent(HpComponent(50, 50))
			.addComponent(FighterComponent(5, 100, 100))
			.addComponent(FactionComponent(2))
			.addComponent(CollisionComponent(true))
			.addComponent(ActionTimeComponent(30))
			.addComponent(SpeedComponent(30))

		val encounterState = EncounterState(5, 1)
			.placeEntity(fighterOne, EncounterPosition(0, 0))
			.placeEntity(fighterTwo, EncounterPosition(4, 0))

		return json.stringify(EncounterState.serializer(), encounterState)
	}
}

fun main(args: Array<String>) {
	runApplication<SupplierApplication>(*args)
}
