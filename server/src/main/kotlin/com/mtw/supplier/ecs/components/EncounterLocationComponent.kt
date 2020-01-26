package com.mtw.supplier.ecs.components

import com.mtw.supplier.ecs.Component
import com.mtw.supplier.encounter.state.EncounterPosition
import kotlinx.serialization.Serializable

@Serializable
class EncounterLocationComponent(var position: EncounterPosition, override var _parentId: Int? = null): Component()