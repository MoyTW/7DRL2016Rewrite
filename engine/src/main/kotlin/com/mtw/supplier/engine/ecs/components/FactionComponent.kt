package com.mtw.supplier.engine.ecs.components

import com.mtw.supplier.engine.ecs.Component
import com.mtw.supplier.engine.encounter.state.EncounterState
import kotlinx.serialization.Serializable

@Serializable
class FactionComponent(var factionId: Int) : Component() {
    override var _parentId: String? = null

    fun isHostileTo(otherEntityId: String, encounterState: EncounterState): Boolean {
        val parentEntity = encounterState.getEntity(this.parentId)
        val otherEntity = encounterState.getEntity(otherEntityId)

        return parentEntity.getComponent(FactionComponent::class).factionId != otherEntity.getComponent(FactionComponent::class).factionId
    }
}
