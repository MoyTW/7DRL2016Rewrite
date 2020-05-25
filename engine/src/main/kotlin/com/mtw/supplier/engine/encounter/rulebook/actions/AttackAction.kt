package com.mtw.supplier.engine.encounter.rulebook.actions

import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.encounter.rulebook.Action
import com.mtw.supplier.engine.encounter.rulebook.ActionType

// TODO: Switch target to ID
class AttackAction(override val actorId: String, val target: Entity): Action(ActionType.ATTACK)