package com.mtw.supplier.ecs.components

import com.mtw.supplier.ecs.Component
import kotlinx.serialization.Serializable

@Serializable
class AttackerComponent(
    var power: Int
): Component() {
    override var _parentId: Int? = null
}