package com.mtw.supplier.engine.ecs

import com.mtw.supplier.engine.ecs.components.ai.*
import com.mtw.supplier.engine.ecs.components.*
import com.mtw.supplier.engine.utils.SeededRand

// TODO: json data or something less hard-coded
enum class EntityDef(val build: (SeededRand) -> Entity) {
    // #################################################################################################################
    // # ENEMIES                                                                                                       #
    // #################################################################################################################
    SCOUT({
        // TODO: Add in XP values
        Entity("Scout", it)
            .addComponent(EnemyScoutAIComponent())
            .addComponent(DefenderComponent(defense = 0, maxHp = 10, currentHp = 10))
            .addComponent(FactionComponent(0))
            .addComponent(CollisionComponent.defaultFighter())
            .addComponent(ActionTimeComponent(75))
            .addComponent(SpeedComponent(75))
            .addComponent(DisplayComponent(DisplayType.ENEMY_SCOUT, false))
    }),
    FIGHTER({
        Entity("Fighter", it)
            .addComponent(EnemyFighterAIComponent())
            .addComponent(DefenderComponent(defense = 0, maxHp = 30, currentHp = 30))
            .addComponent(FactionComponent(0))
            .addComponent(CollisionComponent.defaultFighter())
            .addComponent(ActionTimeComponent(125))
            .addComponent(SpeedComponent(125))
            .addComponent(DisplayComponent(DisplayType.ENEMY_FIGHTER, false))
    }),
    GUNSHIP({
        Entity("Gunship", it)
            .addComponent(EnemyGunshipAIComponent())
            .addComponent(DefenderComponent(defense = 4, maxHp = 50, currentHp = 50))
            .addComponent(FactionComponent(0))
            .addComponent(CollisionComponent.defaultFighter())
            .addComponent(ActionTimeComponent(100))
            .addComponent(SpeedComponent(100))
            .addComponent(DisplayComponent(DisplayType.ENEMY_GUNSHIP, false))
    }),
    FRIGATE({ TODO() }),
    DESTROYER({ TODO() }),
    CRUISER({ TODO() }),
    CARRIER({ TODO() }),

    // #################################################################################################################
    // # ITEM ENTITIES                                                                                                 #
    // #################################################################################################################
    ITEM_DUCT_TAPE({
        // TODO: Implement duct tape!
        Entity("duct tape", it)
            .addComponent(DisplayComponent(DisplayType.ITEM_DUCT_TAPE, false))
    }),
    ITEM_EMP({
        // TODO: Implement EMP!
        Entity("EMP device", it)
            .addComponent(DisplayComponent(DisplayType.ITEM_EMP, false))
    }),
    ITEM_EXTRA_BATTERY({
        // TODO: Implement battery!
        Entity("battery", it)
            .addComponent(DisplayComponent(DisplayType.ITEM_EXTRA_BATTERY, false))
    }),
    ITEM_RED_PAINT({
        // TODO: Implement red paint!
        Entity("red paint", it)
            .addComponent(DisplayComponent(DisplayType.ITEM_RED_PAINT, false))
    }),

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