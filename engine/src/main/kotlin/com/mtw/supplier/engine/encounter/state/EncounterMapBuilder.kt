package com.mtw.supplier.engine.encounter.state

import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.ecs.EntityDictionary
import com.mtw.supplier.engine.utils.SeededRand
import com.mtw.supplier.engine.utils.XYCoordinates

class Zone(
    private val bottomLeft: XYCoordinates,
    private val width: Int,
    private val height: Int,
    val name: String,
    private val seededRand: SeededRand
) {
    val x1 = bottomLeft.x
    val y1 = bottomLeft.y
    val x2 = bottomLeft.x + width
    val y2 = bottomLeft.y + height
    val rand = seededRand.getRandom()

    fun intersects(other: Zone): Boolean {
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

class LevelBlueprint(
    val satellitesPerZone: Int
) {

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
    internal fun fillZone(zone: Zone, encounterMap: EncounterMap, blueprint: LevelBlueprint, safe: Boolean = false) {
        // Distribute satellites within zone
        for (i in 0 until blueprint.satellitesPerZone) {
            val pos = zone.randomCoordinates()
            if (!encounterMap.positionBlocked(pos)) {
                encounterMap.placeEntity(EntityDictionary.buildSatelliteEntity(seededRand), pos, true)
            }
        }

        // Place enemies within zone
        // TODO: Place enemies

        // Place items within zone
        // TODO: Place items
    }

    internal fun build(): EncounterMap {
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
        val zones: MutableList<Zone> = mutableListOf()
        var zoneGenAttempts = 0
        while (zoneGenAttempts < maxZoneGenAttempts && zones.size < maxZones) {
            // Make the zone & place it
            val zoneWidth = (zoneMinSize..zoneMaxSize).random(seededRand.getRandom())
            val zoneHeight = (zoneMinSize..zoneMaxSize).random(seededRand.getRandom())
            val bottomLeft = XYCoordinates((0 until mapWidth - zoneWidth).random(seededRand.getRandom()),
                (0 until mapHeight - zoneHeight).random(seededRand.getRandom()))
            val newZone = Zone(bottomLeft, zoneWidth, zoneHeight, "Zone ${zones.size}", seededRand)

            // Compare the zones
            var failed = false
            for (otherZone in zones) {
                if (newZone.intersects(otherZone)) {
                    failed = true
                    break
                }
            }

            if (!failed) {
                zones.add(newZone)
            }
            zoneGenAttempts += 1
        }

        // Place entities in the zones
        // Place the player. If on depth 9, player is placed in a hazardous zone; otherwise starting zone is safe.
        if (levelDepth == 9) {
            encounterMap.placeEntity(player, zones[0].randomUnblockedCoordinates(encounterMap), true)
            fillZone(zones[0], encounterMap, LevelBlueprint(20), safe = false)
        } else {
            encounterMap.placeEntity(player, zones[0].randomUnblockedCoordinates(encounterMap), true)
            fillZone(zones[0], encounterMap, LevelBlueprint(20), safe = true)
        }
        // We add objects to every other zone
        for (i in 1 until zones.size) {
            fillZone(zones[i], encounterMap, LevelBlueprint(20), safe = false)
        }
        // Generate the diplomat if you're on the last level
        if (levelDepth == 10) {
            TODO("Generate the diplomat!")
        }

        // Generate the jump point
        val jumpPointZone = zones[(1 until zones.size).random(seededRand.getRandom())]
        val jumpPointPos = jumpPointZone.randomUnblockedCoordinates(encounterMap)
        encounterMap.placeEntity(EntityDictionary.buildJumpPointEntity(seededRand), jumpPointPos, true)

        // Generate the intel
        val intelZone = zones[(1 until zones.size).random(seededRand.getRandom())]
        val intelPos = jumpPointZone.randomUnblockedCoordinates(encounterMap)
        encounterMap.placeEntity(EntityDictionary.buildIntelEntity(seededRand), jumpPointPos, true)

        // TODO: Finalize the zone strings

        return encounterMap
    }

}