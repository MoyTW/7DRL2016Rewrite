package com.mtw.supplier.engine.encounter.rulebook.actions

import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.encounter.rulebook.Action
import com.mtw.supplier.engine.encounter.rulebook.ActionType
import com.mtw.supplier.engine.utils.XYCoordinates
import kotlinx.serialization.Serializable

@Serializable
class MoveAction(override val actorId: String, val targetPosition: XYCoordinates): Action() {
    override val actionType: ActionType = ActionType.MOVE
    override val freeAction: Boolean = false
}