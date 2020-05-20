package com.mtw.supplier.engine.ecs.components

import com.mtw.supplier.engine.ecs.Component
import kotlinx.serialization.Serializable

@Serializable
class AttackerComponent(
    var power: Int
): Component() {
    override var _parentId: String? = null
}