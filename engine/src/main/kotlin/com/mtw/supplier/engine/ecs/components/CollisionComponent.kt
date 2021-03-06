package com.mtw.supplier.engine.ecs.components

import com.mtw.supplier.engine.ecs.Component
import kotlinx.serialization.Serializable

@Serializable
class CollisionComponent(
    var blocksMovement: Boolean,
    var blocksVision: Boolean,
    var attackOnHit: Boolean,
    var selfDestructOnHit: Boolean,
    override var _parentId: String? = null
): Component() {
    companion object {
        fun defaultProjectile(): CollisionComponent = CollisionComponent(
            blocksMovement = false,
            blocksVision = false,
            attackOnHit = true,
            selfDestructOnHit = true
        )
        fun defaultFighter(): CollisionComponent = CollisionComponent(
            blocksMovement = true,
            blocksVision = false,
            attackOnHit = false,
            selfDestructOnHit = false
        )
    }
}