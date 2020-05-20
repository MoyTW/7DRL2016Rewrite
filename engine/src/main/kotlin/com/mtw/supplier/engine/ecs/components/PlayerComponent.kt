package com.mtw.supplier.engine.ecs.components

import com.mtw.supplier.engine.ecs.Component
import kotlinx.serialization.Serializable

@Serializable
class PlayerComponent(): Component() {
    override var _parentId: String? = null
}