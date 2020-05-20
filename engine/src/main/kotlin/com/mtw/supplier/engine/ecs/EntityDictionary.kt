package com.mtw.supplier.engine.ecs

import com.mtw.supplier.engine.ecs.components.CollisionComponent
import com.mtw.supplier.engine.ecs.components.DefenderComponent
import com.mtw.supplier.engine.ecs.components.DisplayComponent
import com.mtw.supplier.engine.ecs.components.DisplayType
import com.mtw.supplier.engine.utils.SeededRand
import java.util.*

// TODO: Move this out of Literally Hard-Coded
object EntityDictionary {
    fun buildSatelliteEntity(seededRand: SeededRand): Entity {
        return Entity("Satellite", seededRand)
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

    fun buildJumpPointEntity(seededRand: SeededRand): Entity {
        // TODO: Implement jump point functionality
        return Entity("Jump Point", seededRand)
            .addComponent(DisplayComponent(DisplayType.JUMP_POINT, true))
    }

    fun buildIntelEntity(seededRand: SeededRand): Entity {
        // TODO: Implement intel founctionality
        return Entity("Intel", seededRand)
            .addComponent(DisplayComponent(DisplayType.INTEL, true))
    }
}