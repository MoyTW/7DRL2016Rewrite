package com.mtw.supplier.ecs.components

import com.mtw.supplier.ecs.Component
import kotlinx.serialization.Serializable

enum class DisplayType {
    PLAYER,
    ENEMY_SCOUT,
    PROJECTILE_SMALL_SHOTGUN
}

@Serializable
class DisplayComponent (
    val displayType: DisplayType,
    val seeInFoW: Boolean
) : Component() {
    override var _parentId: Int? = null
}