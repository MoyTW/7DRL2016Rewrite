package com.mtw.supplier.ecs.components

import com.mtw.supplier.ecs.Component
import kotlinx.serialization.Serializable

@Serializable
class CollisionComponent(
    var collidable: Boolean,
    var blocksVision: Boolean,
    var attackOnHit: Boolean,
    var selfDestructOnHit: Boolean,
    override var _parentId: Int? = null
): Component() {
    companion object {
        fun defaultProjectile(): CollisionComponent = CollisionComponent(
            collidable = false,
            blocksVision = false,
            attackOnHit = true,
            selfDestructOnHit = true
        )
        fun defaultFighter(): CollisionComponent = CollisionComponent(
            collidable = true,
            blocksVision = false,
            attackOnHit = false,
            selfDestructOnHit = false
        )
    }
}