package com.mtw.supplier.engine.encounter.rulebook.actions

import com.mtw.supplier.engine.encounter.rulebook.Action
import com.mtw.supplier.engine.encounter.rulebook.ActionType
import kotlinx.serialization.Serializable

@Serializable
class AutopilotAction(override val actorId: String, val zoneId: String): Action() {
    override val actionType: ActionType = ActionType.AUTOPILOT
    override val freeAction: Boolean = true
}