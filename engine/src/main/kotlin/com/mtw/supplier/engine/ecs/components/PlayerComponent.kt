package com.mtw.supplier.engine.ecs.components

import com.mtw.supplier.engine.ecs.Component
import com.mtw.supplier.engine.utils.Constants
import kotlinx.serialization.Serializable

@Serializable
class PlayerComponent(): Component() {
    override var _parentId: String? = null

    val visionRadius = Constants.VISION_RADIUS
}