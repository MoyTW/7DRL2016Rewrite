package com.mtw.supplier.engine.ecs.components.item

import com.mtw.supplier.engine.ecs.Component
import kotlinx.serialization.Serializable


@Serializable
class UsableComponent: Component() { override var _parentId: Int? = null }