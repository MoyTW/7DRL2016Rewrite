package com.mtw.supplier.engine.encounter.rulebook.actions

import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.encounter.rulebook.Action
import com.mtw.supplier.engine.encounter.rulebook.ActionType
import com.mtw.supplier.engine.utils.XYCoordinates

class MoveAction(actor: Entity, val targetPosition: XYCoordinates): Action(actor, ActionType.MOVE)