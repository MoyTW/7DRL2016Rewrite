package com.mtw.supplier.engine.encounter.state

import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.ecs.EntityDef
import com.mtw.supplier.engine.encounter.EncounterDef
import com.mtw.supplier.engine.encounter.LevelBlueprint
import com.mtw.supplier.engine.encounter.LevelData
import com.mtw.supplier.engine.utils.SeededRand
import com.mtw.supplier.engine.utils.XYCoordinates

class ZoneBuilder(
    private val bottomLeft: XYCoordinates,
    private val width: Int,
    private val height: Int,
    val name: String,
    private val seededRand: SeededRand,
    var encounterDef: EncounterDef? = null
) {
    private val x1 = bottomLeft.x
    private val y1 = bottomLeft.y
    private val x2 = bottomLeft.x + width
    private val y2 = bottomLeft.y + height
    private val rand = seededRand.getRandom()

    fun intersects(other: ZoneBuilder): Boolean {
        return this.x1 <= other.x2 && this.x2 >= other.x1 && this.y1 <= other.y2 && this.y2 >= other.y1
    }

    fun randomCoordinates(): XYCoordinates {
        return XYCoordinates(x = rand.nextInt(x1, x2), y = rand.nextInt(y1, y2))
    }

    internal fun randomUnblockedCoordinates(encounterMap: EncounterMap): XYCoordinates {
        val maxTries = 1000
        var tries = 0
        var pos = randomCoordinates()
        while (encounterMap.positionBlocked(pos)) {
            if (tries > maxTries) {
                throw IllegalArgumentException("Can't find random unblocked coordinates in zone! " +
                    "Something's gone wrong with your minimum/satellite ratio probably.")
            }
            pos = randomCoordinates()
            tries++
        }
        return pos
    }
}

class Zone(
    private val x1: Int,
    private val y1: Int,
    private val x2: Int,
    private val y2: Int,
    val name: String,
    val encounterDef: EncounterDef,
    val informedByIntel: Boolean
) {
    val center: XYCoordinates = XYCoordinates(x1 + x2 / 2, (y1 + y2) / 2)
}

class EncounterMapBuilder(
    val levelDepth: Int,
    val player: Entity,
    val seededRand: SeededRand,
    val mapWidth: Int = 300,
    val mapHeight: Int = 300,
    val maxZoneGenAttempts: Int = 100,
    val maxZones: Int = 9,
    val zoneMinSize: Int = 20,
    val zoneMaxSize: Int = 40
) {
    internal fun fillZone(zoneBuilder: ZoneBuilder, encounterMap: EncounterMap, blueprint: LevelBlueprint, safe: Boolean = false) {
        // Distribute satellites within zone
        for (i in 0 until blueprint.satellitesPerZone) {
            val pos = zoneBuilder.randomCoordinates()
            if (!encounterMap.positionBlocked(pos)) {
                encounterMap.placeEntity(EntityDef.SATELLITE.build(seededRand), pos, true)
            }
        }

        // Place enemies within zone
        if (safe) {
            zoneBuilder.encounterDef = EncounterDef.EMPTY_ENCOUNTER
        } else {
            val chosenEncounter = blueprint.chooseEncounter(seededRand)
            zoneBuilder.encounterDef = chosenEncounter

            chosenEncounter.shipList.map {
                val enemy = it.build(seededRand)
                encounterMap.placeEntity(enemy, zoneBuilder.randomUnblockedCoordinates(encounterMap), false)
            }
        }

        // Place items within zone
        // TODO: Place items
    }

    internal fun build(): EncounterMap {
        val blueprint = LevelData.depthsToBlueprints[levelDepth]!!

        // Generate the map with a border
        val encounterMap = EncounterMap(mapWidth, mapHeight)
        for (x in 0 until mapWidth) {
            for (y in 0 until mapHeight) {
                if (x == 0 || x == mapWidth - 1 || y == 0 || y == mapHeight - 1) {
                    encounterMap.markBlockStatus(XYCoordinates(x, y),true, true)
                }
            }
        }

        // Generate the zones layouts. No entities are added here.
        val zoneBuilders: MutableList<ZoneBuilder> = mutableListOf()
        var zoneGenAttempts = 0
        while (zoneGenAttempts < maxZoneGenAttempts && zoneBuilders.size < maxZones) {
            // Make the zone & place it
            val zoneWidth = (zoneMinSize..zoneMaxSize).random(seededRand.getRandom())
            val zoneHeight = (zoneMinSize..zoneMaxSize).random(seededRand.getRandom())
            val bottomLeft = XYCoordinates((0 until mapWidth - zoneWidth).random(seededRand.getRandom()),
                (0 until mapHeight - zoneHeight).random(seededRand.getRandom()))
            val newZone = ZoneBuilder(bottomLeft, zoneWidth, zoneHeight, "Zone ${zoneBuilders.size}", seededRand)

            // Compare the zones
            var failed = false
            for (otherZone in zoneBuilders) {
                if (newZone.intersects(otherZone)) {
                    failed = true
                    break
                }
            }

            if (!failed) {
                zoneBuilders.add(newZone)
            }
            zoneGenAttempts += 1
        }

        // Place entities in the zones
        // Place the player. If on depth 9, player is placed in a hazardous zone; otherwise starting zone is safe.
        if (levelDepth == 9) {
            encounterMap.placeEntity(player, zoneBuilders[0].randomUnblockedCoordinates(encounterMap), true)
            fillZone(zoneBuilders[0], encounterMap, blueprint, safe = false)
        } else {
            encounterMap.placeEntity(player, zoneBuilders[0].randomUnblockedCoordinates(encounterMap), true)
            fillZone(zoneBuilders[0], encounterMap, blueprint, safe = false)
            // TODO Move "starting zone is dangerous" into the level blueprint
            //fillZone(zoneBuilders[0], encounterMap, blueprint, safe = true)
        }
        // We add objects to every other zone
        for (i in 1 until zoneBuilders.size) {
            fillZone(zoneBuilders[i], encounterMap, blueprint, safe = false)
        }
        // Generate the diplomat if you're on the last level
        if (levelDepth == 10) {
            TODO("Generate the diplomat!")
        }

        // Generate the jump point
        val jumpPointZone = zoneBuilders[(1 until zoneBuilders.size).random(seededRand.getRandom())]
        val jumpPointPos = jumpPointZone.randomUnblockedCoordinates(encounterMap)
        encounterMap.placeEntity(EntityDef.JUMP_POINT.build(seededRand), jumpPointPos, true)

        // Generate the intel
        val intelZone = zoneBuilders[(1 until zoneBuilders.size).random(seededRand.getRandom())]
        val intelPos = jumpPointZone.randomUnblockedCoordinates(encounterMap)
        encounterMap.placeEntity(EntityDef.INTEL.build(seededRand), jumpPointPos, true)

        // TODO: Finalize the zone strings

        return encounterMap
    }

}