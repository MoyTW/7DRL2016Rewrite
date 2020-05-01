package com.mtw.supplier.engine.ecs.components

import com.mtw.supplier.engine.ecs.Component
import kotlinx.serialization.Serializable

@Serializable
class SpeedComponent(
    var _baseSpeed: Int
): Component() {
    override var _parentId: Int? = null

    val baseSpeed: Int
        get() = _baseSpeed

    // TODO: Buffs
    val speed: Int
        get() = _baseSpeed
}