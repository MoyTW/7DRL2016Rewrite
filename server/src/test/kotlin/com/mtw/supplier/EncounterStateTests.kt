package com.mtw.supplier

import com.mtw.supplier.ecs.Entity
import com.mtw.supplier.ecs.components.*
import com.mtw.supplier.encounter.state.EncounterState
import com.mtw.supplier.encounter.EncounterRunner
import com.mtw.supplier.encounter.state.EncounterPosition
import org.junit.Test
import org.junit.internal.runners.JUnit4ClassRunner
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(JUnit4::class)
@SpringBootTest
class EncounterStateTests {

    @Test
    fun doesStuff() {
        val fighterOne = Entity(1, "wolf")
            .addComponent(AIComponent())
            .addComponent(HpComponent(5, 5))
            .addComponent(FighterComponent(5, 5, 5))
            .addComponent(FactionComponent(0))
            .addComponent(CollisionComponent(true))
        val fighterTwo = Entity(2, "strongMercenary")
            .addComponent(AIComponent())
            .addComponent(HpComponent(5, 5))
            .addComponent(FighterComponent(5, 100, 100))
            .addComponent(FactionComponent(2))
            .addComponent(CollisionComponent(true))

        val encounterState = EncounterState(5, 1)
            .placeEntity(fighterOne, EncounterPosition(0, 0))
            .placeEntity(fighterTwo, EncounterPosition(4, 0))
        val encounterRunner = EncounterRunner()
        encounterRunner.runEncounter(encounterState)
    }
}
