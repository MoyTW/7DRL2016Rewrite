package com.mtw.supplier.engine

import com.mtw.supplier.engine.ecs.Component
import com.mtw.supplier.engine.ecs.components.*
import com.mtw.supplier.engine.ecs.components.ai.AIComponent
import com.mtw.supplier.engine.ecs.components.ai.EnemyScoutAIComponent
import com.mtw.supplier.engine.ecs.components.ai.PathAIComponent
import com.mtw.supplier.engine.ecs.components.item.CarryableComponent
import com.mtw.supplier.engine.ecs.components.item.InventoryComponent
import com.mtw.supplier.engine.ecs.components.item.UsableComponent
import com.mtw.supplier.engine.encounter.state.EncounterState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule
import org.slf4j.LoggerFactory
import kotlin.system.measureTimeMillis

object Serializers {
    private val logger = LoggerFactory.getLogger(Serializers::class.java)
    private val json = Json(JsonConfiguration.Stable.copy(prettyPrint = false),
        context = componentSerializersModuleBuilder())

    private fun componentSerializersModuleBuilder(): SerialModule {
        return SerializersModule {
            polymorphic(Component::class) {
                // ai
                AIComponent::class with AIComponent.serializer()
                EnemyScoutAIComponent::class with EnemyScoutAIComponent.serializer()
                PathAIComponent::class with PathAIComponent.serializer()

                // item
                CarryableComponent::class with CarryableComponent.serializer()
                InventoryComponent::class with InventoryComponent.serializer()
                UsableComponent::class with UsableComponent.serializer()

                // other
                ActionTimeComponent::class with ActionTimeComponent.serializer()
                CollisionComponent::class with CollisionComponent.serializer()
                DisplayComponent::class with DisplayComponent.serializer()
                EncounterLocationComponent::class with EncounterLocationComponent.serializer()
                FactionComponent::class with FactionComponent.serializer()
                DefenderComponent::class with DefenderComponent.serializer()
                AttackerComponent::class with AttackerComponent.serializer()
                PlayerComponent::class with PlayerComponent.serializer()
                SpeedComponent::class with SpeedComponent.serializer()
            }
        }
    }

    fun stringify(encounterState: EncounterState): String {
        // This is kind of a janky but deterministic way to force consistent randomness.
        encounterState.seededRand.reseed()

        var s: String? = null
        val timeTaken = measureTimeMillis {
            s = json.stringify(EncounterState.serializer(), encounterState)
        }
        logger.info("Stringify ms: $timeTaken")
        return s!!.replace(",", ",\n")
    }

    fun parse(body: String): EncounterState {
        var p: EncounterState? = null
        val timeTaken = measureTimeMillis {
            p = json.parse(EncounterState.serializer(), body)
        }
        logger.info("Parse ms: $timeTaken")
        return p!!
    }
}