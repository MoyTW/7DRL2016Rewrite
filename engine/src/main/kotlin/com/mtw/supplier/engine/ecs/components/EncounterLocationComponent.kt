package com.mtw.supplier.engine.ecs.components

import com.mtw.supplier.engine.ecs.Component
import com.mtw.supplier.engine.utils.XYCoordinates
import kotlinx.serialization.Serializable

@Serializable
class EncounterLocationComponent(var position: XYCoordinates, override var _parentId: Int? = null): Component()