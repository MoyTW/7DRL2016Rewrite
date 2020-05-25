package com.mtw.supplier.engine.encounter.rulebook.actions

import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.encounter.rulebook.Action
import com.mtw.supplier.engine.encounter.rulebook.ActionType

class SelfDestructAction(override val actorId: String): Action() {
    override val actionType: ActionType = ActionType.SELF_DESTRUCT
    override val freeAction: Boolean = false
}