package com.mtw.supplier.engine.ecs

import com.mtw.supplier.engine.ecs.components.CollisionComponent
import com.mtw.supplier.engine.ecs.components.DefenderComponent
import com.mtw.supplier.engine.ecs.components.DisplayComponent
import com.mtw.supplier.engine.ecs.components.DisplayType

// TODO: Move this out of Literally Hard-Coded
object EntityDictionary {
    fun buildSatelliteEntity(entityId: Int): Entity {
        return Entity(entityId, "Satellite")
            .addComponent(CollisionComponent(
                blocksMovement = true,
                blocksVision = true,
                attackOnHit = false,
                selfDestructOnHit = false
            )).addComponent(DefenderComponent(
                defense = Integer.MAX_VALUE,
                maxHp = Integer.MAX_VALUE,
                currentHp = Integer.MAX_VALUE
            )).addComponent(DisplayComponent(
                DisplayType.SATELLITE,
                true
            ))
    }

    fun buildJumpPointEntity(entityId: Int): Entity {
        // TODO: Implement jump point functionality
        return Entity(entityId, "Jump Point")
            .addComponent(DisplayComponent(DisplayType.JUMP_POINT, true))
    }

    fun buildIntelEntity(entityId: Int): Entity {
        // TODO: Implement intel founctionality
        return Entity(entityId, "Intel")
            .addComponent(DisplayComponent(DisplayType.INTEL, true))
    }
}