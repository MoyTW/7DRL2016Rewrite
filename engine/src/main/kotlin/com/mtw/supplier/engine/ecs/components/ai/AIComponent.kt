package com.mtw.supplier.engine.ecs.components.ai

import com.mtw.supplier.engine.ecs.Component
import com.mtw.supplier.engine.encounter.rulebook.Action
import com.mtw.supplier.engine.encounter.state.EncounterState
import kotlinx.serialization.Serializable


@Serializable
abstract class AIComponent : Component() {
    abstract var isActive: Boolean
    abstract fun decideNextActions(encounterState: EncounterState): List<Action>
}
