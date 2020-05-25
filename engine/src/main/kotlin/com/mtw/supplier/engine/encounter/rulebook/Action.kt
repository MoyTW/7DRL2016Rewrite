package com.mtw.supplier.engine.encounter.rulebook

import com.mtw.supplier.engine.ecs.Entity
import kotlinx.serialization.Serializable

@Serializable
abstract class Action {
    abstract val actorId: String
    abstract val actionType: ActionType
    abstract val freeAction: Boolean
}