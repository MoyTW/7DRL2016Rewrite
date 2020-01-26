package com.mtw.supplier

import com.mtw.supplier.ecs.Entity
import com.mtw.supplier.ecs.components.AIComponent
import com.mtw.supplier.ecs.components.FactionComponent
import com.mtw.supplier.ecs.components.FighterComponent
import com.mtw.supplier.ecs.components.HpComponent
import com.mtw.supplier.encounter.state.EncounterState
import com.mtw.supplier.encounter.EncounterRunner
import com.mtw.supplier.encounter.state.EncounterPosition
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class EncounterStateTests {

    @Test
    fun doesStuff() {
        val fighterOne = Entity(1, "wolf")
            .addComponent(AIComponent())
            .addComponent(HpComponent(5, 5))
            .addComponent(FighterComponent(5, 5, 5))
            .addComponent(FactionComponent(0))
        val fighterTwo = Entity(2, "strongMercenary")
            .addComponent(AIComponent())
            .addComponent(HpComponent(5, 5))
            .addComponent(FighterComponent(5, 100, 100))
            .addComponent(FactionComponent(2))

        val encounterState = EncounterState(5, 1)
            .placeEntity(fighterOne, EncounterPosition(1, 1))
            .placeEntity(fighterTwo, EncounterPosition(5, 1))
        val encounterRunner = EncounterRunner()
        encounterRunner.runEncounter(encounterState)
    }
}
