package com.mtw.supplier.engine.encounter.rulebook.actions

import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.encounter.rulebook.Action
import com.mtw.supplier.engine.encounter.rulebook.ActionType
import kotlinx.serialization.Serializable

@Serializable
class WaitAction(override val actorId: String): Action(ActionType.WAIT)