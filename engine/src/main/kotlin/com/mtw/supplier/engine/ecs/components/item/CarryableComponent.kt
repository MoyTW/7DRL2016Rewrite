package com.mtw.supplier.engine.ecs.components.item

import com.mtw.supplier.engine.ecs.Component
import kotlinx.serialization.Serializable

@Serializable
class CarryableComponent: Component() {
    override var _parentId: String? = null
}
