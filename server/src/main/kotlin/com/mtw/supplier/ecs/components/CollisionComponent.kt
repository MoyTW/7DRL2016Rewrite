package com.mtw.supplier.ecs.components

import com.mtw.supplier.ecs.Component
import kotlinx.serialization.Serializable

@Serializable
class CollisionComponent(var collidable: Boolean, override var _parentId: Int? = null): Component()