package com.mtw.supplier.engine.ecs.components

import com.mtw.supplier.engine.ecs.Component
import kotlinx.serialization.Serializable

@Serializable
class PlayerComponent(override var _parentId: Int? = null): Component()