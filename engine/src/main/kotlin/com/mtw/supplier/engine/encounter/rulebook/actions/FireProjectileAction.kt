package com.mtw.supplier.engine.encounter.rulebook.actions

import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.encounter.rulebook.Action
import com.mtw.supplier.engine.encounter.rulebook.ActionType
import com.mtw.supplier.engine.utils.PathBuilder


enum class ProjectileType(val displayName: String) {
    LASER("laser beam"),
    SHOTGUN_PELLET("shotgun pellet")
}

class FireProjectileAction(
    actor: Entity,
    val power: Int,
    val pathBuilder: PathBuilder,
    val speed: Int,
    val projectileType: ProjectileType,
    val numProjectiles: Int = 1
): Action(actor, actionType = ActionType.FIRE_PROJECTILE)