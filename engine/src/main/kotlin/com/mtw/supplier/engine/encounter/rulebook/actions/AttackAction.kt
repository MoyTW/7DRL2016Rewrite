package com.mtw.supplier.engine.encounter.rulebook.actions

import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.encounter.rulebook.Action
import com.mtw.supplier.engine.encounter.rulebook.ActionType

// TODO: Switch target to ID
class AttackAction(override val actorId: String, val target: Entity): Action() {
    override val actionType: ActionType = ActionType.PICK_UP_ITEM
    // Kind of irrelevant, since the only things that use AttackAction are projectiles, which then self-destruct!
    override val freeAction: Boolean = false
}