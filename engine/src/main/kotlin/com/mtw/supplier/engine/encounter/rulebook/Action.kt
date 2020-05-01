package com.mtw.supplier.engine.encounter.rulebook

import com.mtw.supplier.engine.ecs.Entity

abstract class Action(val actor: Entity, val actionType: ActionType)