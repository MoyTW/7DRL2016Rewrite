package com.mtw.supplier.encounter.rulebook.actions

import com.mtw.supplier.ecs.Entity
import com.mtw.supplier.encounter.rulebook.Action
import com.mtw.supplier.encounter.rulebook.ActionType
import com.mtw.supplier.utils.Path


enum class ProjectileType(val displayName: String) {
    LASER("laser beam")
}

class FireProjectileAction(
    actor: Entity,
    val damage: Int,
    val path: Path,
    val speed: Int,
    val projectileType: ProjectileType
): Action(actor, actionType = ActionType.FIRE_PROJECTILE)