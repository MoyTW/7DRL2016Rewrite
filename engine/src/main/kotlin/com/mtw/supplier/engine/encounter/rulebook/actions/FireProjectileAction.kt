package com.mtw.supplier.engine.encounter.rulebook.actions

import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.encounter.rulebook.Action
import com.mtw.supplier.engine.encounter.rulebook.ActionType
import com.mtw.supplier.engine.utils.LinePathBuilder
import com.mtw.supplier.engine.utils.PathBuilder
import com.mtw.supplier.engine.utils.SeededRand
import com.mtw.supplier.engine.utils.XYCoordinates


enum class ProjectileType(val displayName: String) {
    LASER("laser beam"),
    SMALL_SHOTGUN_PELLET("shotgun pellet"),
    SMALL_GATLING_SHELL("gatling shell"),
    SMALL_CANNON_SHELL("cannon shell")
}

class FireProjectileAction(
    override val actorId: String,
    val power: Int,
    val pathBuilder: PathBuilder,
    val speed: Int,
    val projectileType: ProjectileType,
    val numProjectiles: Int = 1
): Action(actionType = ActionType.FIRE_PROJECTILE)

object WeaponList {
    fun createFireSmallShotgunAction(parent: Entity, targetPos: XYCoordinates, seededRand: SeededRand): FireProjectileAction {
        return FireProjectileAction(
            actorId = parent.id,
            power = 1,
            pathBuilder = LinePathBuilder(targetPos = targetPos, seededRand = seededRand, spread = 5),
            speed = 25,
            projectileType = ProjectileType.SMALL_SHOTGUN_PELLET,
            numProjectiles = 5)
    }

    fun createFireSmallGatlingAction(parent: Entity, targetPos: XYCoordinates): FireProjectileAction {
        return FireProjectileAction(
            actorId = parent.id,
            power = 2,
            pathBuilder = LinePathBuilder(targetPos = targetPos),
            speed = 50,
            projectileType = ProjectileType.SMALL_GATLING_SHELL)
    }

    fun createFireSmallCannonAction(parent: Entity, targetPos: XYCoordinates): FireProjectileAction {
        return FireProjectileAction(
            actorId = parent.id,
            power = 5,
            pathBuilder = LinePathBuilder(targetPos = targetPos),
            speed = 50,
            projectileType = ProjectileType.SMALL_CANNON_SHELL)
    }
}