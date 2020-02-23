package com.mtw.supplier

import com.mtw.supplier.ecs.Component
import com.mtw.supplier.ecs.components.*
import kotlinx.serialization.modules.SerialModule
import kotlinx.serialization.modules.SerializersModule

object Serializers {
    fun componentSerializersModuleBuilder(): SerialModule {
        return SerializersModule {
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
    }
}