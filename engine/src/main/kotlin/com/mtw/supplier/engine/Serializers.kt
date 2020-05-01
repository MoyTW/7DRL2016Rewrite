package com.mtw.supplier.engine

import com.mtw.supplier.engine.ecs.Component
import com.mtw.supplier.engine.ecs.components.*
import com.mtw.supplier.engine.ecs.components.ai.AIComponent
import com.mtw.supplier.engine.ecs.components.ai.EnemyScoutAIComponent
import com.mtw.supplier.engine.ecs.components.ai.PathAIComponent
import com.mtw.supplier.engine.ecs.components.item.CarryableComponent
import com.mtw.supplier.engine.ecs.components.item.InventoryComponent
import com.mtw.supplier.engine.ecs.components.item.UsableComponent
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule

object Serializers {
    fun componentSerializersModuleBuilder(): SerialModule {
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
}