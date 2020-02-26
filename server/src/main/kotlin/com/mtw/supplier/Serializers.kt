package com.mtw.supplier

import com.mtw.supplier.ecs.Component
import com.mtw.supplier.ecs.components.*
import com.mtw.supplier.ecs.components.ai.AIComponent
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule

object Serializers {
    fun componentSerializersModuleBuilder(): SerialModule {
        return SerializersModule {
            polymorphic(Component::class) {
                ActionTimeComponent::class with ActionTimeComponent.serializer()
                AIComponent::class with AIComponent.serializer()
                CollisionComponent::class with CollisionComponent.serializer()
                EncounterLocationComponent::class with EncounterLocationComponent.serializer()
                FactionComponent::class with FactionComponent.serializer()
                HpComponent::class with HpComponent.serializer()
                FighterComponent::class with FighterComponent.serializer()
                PlayerComponent::class with PlayerComponent.serializer()
                SpeedComponent::class with SpeedComponent.serializer()
            }
        }
    }
}