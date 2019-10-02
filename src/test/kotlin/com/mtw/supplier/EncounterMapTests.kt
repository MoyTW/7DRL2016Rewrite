package com.mtw.supplier

import com.mtw.supplier.ecs.Entity
import com.mtw.supplier.ecs.components.AIComponent
import com.mtw.supplier.ecs.components.FighterComponent
import com.mtw.supplier.ecs.components.HpComponent
import com.mtw.supplier.encounter.map.EncounterMap
import com.mtw.supplier.encounter.map.EncounterNode
import com.mtw.supplier.encounter.EncounterRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest
class EncounterMapTests {

    @Test
    fun doesStuff() {
        val fighterOne = Entity(UUID.randomUUID())
            .addComponent(AIComponent())
            .addComponent(HpComponent(5, 5))
            .addComponent(FighterComponent(5, 5))
        val fighterTwo = Entity(UUID.randomUUID())
            .addComponent(AIComponent())
            .addComponent(HpComponent(5, 5))
            .addComponent(FighterComponent(100, 100))

        // Build nodes
        val temple = EncounterNode(111, "temple", 3)
        val templeBridge = EncounterNode(222, "templeBridge", 2)
        val centerBridge = EncounterNode(333, "centerBridge", 2)
        val plainsBridge = EncounterNode(444, "plainsBridge", 2)
        val plains = EncounterNode(555, "plains", 8)

        // Link nodes
        // TODO: private exits
        temple.exits.add(templeBridge)
        templeBridge.exits.add(temple)

        templeBridge.exits.add(centerBridge)
        centerBridge.exits.add(templeBridge)

        centerBridge.exits.add(plainsBridge)
        plainsBridge.exits.add(centerBridge)

        plains.exits.add(plainsBridge)
        plainsBridge.exits.add(plains)

        val encounterMap = EncounterMap()
            .addNode(temple)
            .addNode(templeBridge)
            .addNode(centerBridge)
            .addNode(plainsBridge)
            .addNode(plains)
            .placeEntity(fighterOne, temple.id)
            .placeEntity(fighterTwo, plains.id)
        val encounterRunner = EncounterRunner(encounterMap)
        encounterRunner.runTurn()
        System.out.println("#####")
    }

}
