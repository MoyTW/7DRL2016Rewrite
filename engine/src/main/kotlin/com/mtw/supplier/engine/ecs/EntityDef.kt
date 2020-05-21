package com.mtw.supplier.engine.ecs

import com.mtw.supplier.engine.ecs.components.CollisionComponent
import com.mtw.supplier.engine.ecs.components.DefenderComponent
import com.mtw.supplier.engine.ecs.components.DisplayComponent
import com.mtw.supplier.engine.ecs.components.DisplayType
import com.mtw.supplier.engine.utils.SeededRand
import java.util.*

// TODO: json data or something less hard-coded
enum class EntityDef(val build: (SeededRand) -> Entity) {
    // #################################################################################################################
    // # ENEMIES                                                                                                       #
    // #################################################################################################################
    SCOUT({ TODO() }),
    FIGHTER({ TODO() }),
    GUNSHIP({ TODO() }),
    FRIGATE({ TODO() }),
    DESTROYER({ TODO() }),
    CRUISER({ TODO() }),
    CARRIER({ TODO() }),

    // #################################################################################################################
    // # MAP ENTITIES                                                                                                  #
    // #################################################################################################################
    SATELLITE(fun(seededRand: SeededRand): Entity {
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
                    )) }),
    // TODO: Implement jump point functionality
    JUMP_POINT(fun(seededRand: SeededRand): Entity {
        return Entity("Jump Point", seededRand)
            .addComponent(DisplayComponent(DisplayType.JUMP_POINT, true))
    }),
    // TODO: Implement intel founctionality
    INTEL(fun(seededRand: SeededRand): Entity {
        return Entity("Intel", seededRand)
            .addComponent(DisplayComponent(DisplayType.INTEL, true))
    })
}