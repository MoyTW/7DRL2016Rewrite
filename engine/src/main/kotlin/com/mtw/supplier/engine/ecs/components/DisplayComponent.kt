package com.mtw.supplier.engine.ecs.components

import com.mtw.supplier.engine.ecs.Component
import kotlinx.serialization.Serializable

/**
 * Priority rules:
 * 0 -> Blocking entity (Actor, Satellite)
 * 1 -> Projectile entity
 * 2 -> Item entity
 */
enum class DisplayType(val priority: Int) {
    PLAYER(0),
    ENEMY_SCOUT(0),
    PROJECTILE_SMALL_SHOTGUN(1)
}

@Serializable
class DisplayComponent (
    val displayType: DisplayType,
    val seeInFoW: Boolean
) : Component() {
    override var _parentId: Int? = null
}