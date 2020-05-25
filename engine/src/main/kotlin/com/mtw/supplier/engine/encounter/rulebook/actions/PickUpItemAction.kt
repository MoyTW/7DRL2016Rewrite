package com.mtw.supplier.engine.encounter.rulebook.actions

import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.encounter.rulebook.Action
import com.mtw.supplier.engine.encounter.rulebook.ActionType
import kotlinx.serialization.Serializable

@Serializable
class PickUpItemAction(override val actorId: String): Action() {
    override val actionType: ActionType = ActionType.PICK_UP_ITEM
    override val freeAction: Boolean = true
}