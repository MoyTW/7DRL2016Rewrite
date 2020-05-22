package com.mtw.supplier.engine.encounter

import com.mtw.supplier.engine.ecs.EntityDef
import com.mtw.supplier.engine.utils.SeededRand

object WeightedChoicePicker {
    fun <T: Any> pick(choicesWithWeights: Map<T, Int>, seededRand: SeededRand): T {
        val max = choicesWithWeights.values.sum()
        val choice = seededRand.getRandom().nextInt(max)

        var choiceSum = 0
        val asList = choicesWithWeights.entries.toList()
        for (choiceWithWeight in asList) {
            choiceSum += choiceWithWeight.value
            if (choice <= choiceSum) {
                return choiceWithWeight.key
            }
        }
        // The above code should always return, but Kotlin requres an assured return.
        return asList.last().key
    }
}

enum class EncounterDef(val encounterName: String, val shipList: List<EntityDef>) {
    EMPTY_ENCOUNTER ( "none", listOf()),
    SCOUT_ENCOUNTER ("single scout", listOf(EntityDef.SCOUT)),
    SCOUT_PAIR_ENCOUNTER ("scout pair", listOf(EntityDef.SCOUT, EntityDef.SCOUT)),
    SCOUT_TRIO_ENCOUNTER ("scout element", listOf(EntityDef.SCOUT, EntityDef.SCOUT, EntityDef.SCOUT)),
    FIGHTER_ENCOUNTER ("single fighter", listOf(EntityDef.FIGHTER)),
    FIGHTER_RECON_ENCOUNTER ("recon flight", listOf(EntityDef.FIGHTER, EntityDef.SCOUT, EntityDef.SCOUT)),
    FIGHTER_PAIR_ENCOUNTER ("fighter element", listOf(EntityDef.FIGHTER, EntityDef.FIGHTER)),
    FIGHTER_FLIGHT_ENCOUNTER ("fighter flight", listOf(EntityDef.FIGHTER, EntityDef.FIGHTER, EntityDef.FIGHTER,
        EntityDef.FIGHTER)),
    GUNSHIP_ENCOUNTER ("single gunship", listOf(EntityDef.GUNSHIP)),
    GUNSHIP_RECON_ENCOUNTER ("gunship and escorts", listOf(EntityDef.GUNSHIP, EntityDef.SCOUT, EntityDef.SCOUT)),
    // In the original game, GUNSHIP_PAIR_ENCOUNTER is never used. That, uh, appears to be a bug.
    GUNSHIP_PAIR_ENCOUNTER ("gunship element", listOf(EntityDef.GUNSHIP, EntityDef.GUNSHIP)),
    FRIGATE_ENCOUNTER ("single frigate", listOf(EntityDef.FRIGATE)),
    DESTROYER_ENCOUNTER ("single destroyer", listOf(EntityDef.DESTROYER)),
    CRUISER_ENCOUNTER ("single cruiser", listOf(EntityDef.CRUISER)),
    CARRIER_ENCOUNTER ("single carrier", listOf(EntityDef.CARRIER)),
    FRIGATE_PAIR_ENCOUNTER ("pair of frigates", listOf(EntityDef.FRIGATE, EntityDef.FRIGATE)),
    FRIGATE_GUNSHIP_ENCOUNTER ("frigate and gunship", listOf(EntityDef.FRIGATE, EntityDef.GUNSHIP, EntityDef.GUNSHIP)),
    DESTROYER_FIGHTER_ENCOUNTER ("destroyer and escorts", listOf(EntityDef.DESTROYER, EntityDef.FIGHTER, EntityDef.FIGHTER,
        EntityDef.FIGHTER, EntityDef.FIGHTER, EntityDef.FIGHTER, EntityDef.FIGHTER)),
    DESTROYER_FRIGATE_ENCOUNTER ("destroyer and frigate", listOf(EntityDef.DESTROYER, EntityDef.FRIGATE)),
    CRUISER_PAIR_ENCOUNTER ("cruiser pair", listOf(EntityDef.CRUISER, EntityDef.CRUISER)),
    CRUISER_FIGHTER_ENCOUNTER ("cruiser and escorts", listOf(EntityDef.CRUISER, EntityDef.FIGHTER, EntityDef.FIGHTER,
        EntityDef.FIGHTER, EntityDef.FIGHTER, EntityDef.FIGHTER, EntityDef.FIGHTER)),
    CARRIER_SCREEN_ENCOUNTER ("carrier and screening force", listOf(EntityDef.CARRIER, EntityDef.SCOUT, EntityDef.SCOUT,
        EntityDef.SCOUT, EntityDef.GUNSHIP, EntityDef.GUNSHIP, EntityDef.GUNSHIP)),
    CARRIER_DESTROYER_ENCOUNTER ("carrier and destroyer", listOf(EntityDef.CARRIER, EntityDef.DESTROYER, EntityDef.GUNSHIP)),
    CARRIER_TASK_FORCE_ENCOUNTER ("carrier task force", listOf(EntityDef.CARRIER, EntityDef.CRUISER, EntityDef.DESTROYER,
        EntityDef.DESTROYER, EntityDef.FRIGATE, EntityDef.FRIGATE, EntityDef.FRIGATE)),
    FAST_RESPONSE_FLEET_ENCOUNTER ("fast response fleet", listOf(EntityDef.DESTROYER, EntityDef.DESTROYER, EntityDef.DESTROYER,
        EntityDef.DESTROYER, EntityDef.DESTROYER, EntityDef.DESTROYER, EntityDef.FRIGATE, EntityDef.FRIGATE, EntityDef.GUNSHIP,
        EntityDef.GUNSHIP, EntityDef.GUNSHIP, EntityDef.GUNSHIP, EntityDef.GUNSHIP, EntityDef.GUNSHIP, EntityDef.GUNSHIP,
        EntityDef.GUNSHIP, EntityDef.GUNSHIP, EntityDef.GUNSHIP, EntityDef.GUNSHIP, EntityDef.GUNSHIP)),
    HEAVY_STRIKE_FORCE_ENCOUNTER ("heavy strike force", listOf(EntityDef.CRUISER, EntityDef.CRUISER, EntityDef.CRUISER,
        EntityDef.CRUISER, EntityDef.CARRIER, EntityDef.CARRIER, EntityDef.DESTROYER, EntityDef.DESTROYER)),
    EVER_VICTORIOUS_FLEET_ENCOUNTER ("Ever Victorious Fleet", listOf(EntityDef.CRUISER, EntityDef.CRUISER, EntityDef.CRUISER,
        EntityDef.CRUISER, EntityDef.CARRIER, EntityDef.CARRIER, EntityDef.DESTROYER, EntityDef.DESTROYER, EntityDef.CARRIER,
        EntityDef.CRUISER, EntityDef.FRIGATE, EntityDef.FRIGATE, EntityDef.FRIGATE, EntityDef.FRIGATE, EntityDef.FRIGATE))
}

enum class EncounterChallengeRating(val challengeRatingToEncounters: Map<EncounterDef, Int>) {
    ZERO(mapOf(EncounterDef.SCOUT_ENCOUNTER to 50,
        EncounterDef.SCOUT_PAIR_ENCOUNTER to 100,
        EncounterDef.SCOUT_TRIO_ENCOUNTER to 100,
        EncounterDef.FIGHTER_ENCOUNTER to 50)),
    ONE(mapOf(EncounterDef.FIGHTER_ENCOUNTER to 50,
        EncounterDef.FIGHTER_RECON_ENCOUNTER to 100,
        EncounterDef.FIGHTER_PAIR_ENCOUNTER to 100,
        EncounterDef.GUNSHIP_ENCOUNTER to 50)),
    TWO(mapOf(EncounterDef.GUNSHIP_ENCOUNTER to 50,
        EncounterDef.FIGHTER_FLIGHT_ENCOUNTER to 100,
        EncounterDef.GUNSHIP_RECON_ENCOUNTER to 100,
        EncounterDef.FRIGATE_ENCOUNTER to 50)),
    THREE(mapOf(EncounterDef.FRIGATE_ENCOUNTER to 50,
        EncounterDef.FRIGATE_PAIR_ENCOUNTER to 100,
        EncounterDef.FRIGATE_GUNSHIP_ENCOUNTER to 100,
        EncounterDef.DESTROYER_ENCOUNTER to 50)),
    FOUR(mapOf(EncounterDef.DESTROYER_ENCOUNTER to 50,
        EncounterDef.DESTROYER_FIGHTER_ENCOUNTER to 100,
        EncounterDef.DESTROYER_FRIGATE_ENCOUNTER to 100,
        EncounterDef.CRUISER_ENCOUNTER to 50)),
    FIVE(mapOf(EncounterDef.CRUISER_ENCOUNTER to 50,
        EncounterDef.CRUISER_PAIR_ENCOUNTER to 100,
        EncounterDef.CRUISER_FIGHTER_ENCOUNTER to 100,
        EncounterDef.CARRIER_ENCOUNTER to 50)),
    SIX(mapOf(EncounterDef.CARRIER_ENCOUNTER to 50,
        EncounterDef.CARRIER_SCREEN_ENCOUNTER to 100,
        EncounterDef.CARRIER_DESTROYER_ENCOUNTER to 100,
        EncounterDef.CARRIER_TASK_FORCE_ENCOUNTER to 50)),
    SEVEN(mapOf(EncounterDef.CARRIER_TASK_FORCE_ENCOUNTER to 50,
        EncounterDef.FAST_RESPONSE_FLEET_ENCOUNTER to 100,
        EncounterDef.HEAVY_STRIKE_FORCE_ENCOUNTER to 100,
        EncounterDef.EVER_VICTORIOUS_FLEET_ENCOUNTER to 50))
}

class LevelBlueprint(
    val levelDepth: Int,
    val satellitesPerZone: Int,
    private val maxItemsPerZone: Int,
    private val encounterTable: Map<EncounterChallengeRating, Int>,
    private val itemTable: Map<EntityDef, Int>
) {
    /**
     * Encounters are generated by the following rules:
     * 1. Roll for the challenge rating
     * 2. Roll for the specific encounter at that challenge level
     */
    fun chooseEncounterForZone(seededRand: SeededRand): EncounterDef {
        val encounterLevel = WeightedChoicePicker.pick(encounterTable, seededRand)
        return WeightedChoicePicker.pick(encounterLevel.challengeRatingToEncounters, seededRand)
    }

    fun chooseItemsForZone(seededRand: SeededRand): List<EntityDef> {
        val numItems = seededRand.getRandom().nextInt(maxItemsPerZone + 1)
        return (0 until numItems).map { WeightedChoicePicker.pick(itemTable, seededRand) }
    }
}

object LevelData {
    // In the original game, the item chances were fixed across depths
    val theOneItemChanceTable = mapOf<EntityDef, Int>(
        EntityDef.ITEM_DUCT_TAPE to 45,
        EntityDef.ITEM_EMP to 10,
        EntityDef.ITEM_EXTRA_BATTERY to 25,
        EntityDef.ITEM_RED_PAINT to 10
    )

    val depthsToBlueprints: Map<Int, LevelBlueprint> = mapOf(
        1 to LevelBlueprint(levelDepth = 1,
            satellitesPerZone = 20,
            maxItemsPerZone = 3,
            encounterTable = mapOf(
                EncounterChallengeRating.ZERO to 20,
                EncounterChallengeRating.ONE to 10),
            itemTable = theOneItemChanceTable),
        2 to LevelBlueprint(levelDepth = 2,
            satellitesPerZone = 20,
            maxItemsPerZone = 3,
            encounterTable = mapOf(
                EncounterChallengeRating.ZERO to 10,
                EncounterChallengeRating.ONE to 20,
                EncounterChallengeRating.TWO to 10),
            itemTable = theOneItemChanceTable),
        3 to LevelBlueprint(levelDepth = 3,
            satellitesPerZone = 15,
            maxItemsPerZone = 3,
            encounterTable = mapOf(
                EncounterChallengeRating.ONE to 10,
                EncounterChallengeRating.TWO to 20,
                EncounterChallengeRating.THREE to 10),
            itemTable = theOneItemChanceTable),
        4 to LevelBlueprint(levelDepth = 4,
            satellitesPerZone = 15,
            maxItemsPerZone = 2,
            encounterTable = mapOf(
                EncounterChallengeRating.TWO to 10,
                EncounterChallengeRating.THREE to 20,
                EncounterChallengeRating.FOUR to 10),
            itemTable = theOneItemChanceTable),
        5 to LevelBlueprint(levelDepth = 5,
            satellitesPerZone = 10,
            maxItemsPerZone = 2,
            encounterTable = mapOf(
                EncounterChallengeRating.THREE to 10,
                EncounterChallengeRating.FOUR to 20,
                EncounterChallengeRating.FIVE to 10),
            itemTable = theOneItemChanceTable),
        6 to LevelBlueprint(levelDepth = 6,
            satellitesPerZone = 10,
            maxItemsPerZone = 1,
            encounterTable = mapOf(
                EncounterChallengeRating.FOUR to 10,
                EncounterChallengeRating.FIVE to 20,
                EncounterChallengeRating.SIX to 10),
            itemTable = theOneItemChanceTable),
        7 to LevelBlueprint(levelDepth = 7,
            satellitesPerZone = 10,
            maxItemsPerZone = 1,
            encounterTable = mapOf(
                EncounterChallengeRating.FIVE to 10,
                EncounterChallengeRating.SIX to 20),
            itemTable = theOneItemChanceTable),
        8 to LevelBlueprint(levelDepth = 8,
            satellitesPerZone = 10,
            maxItemsPerZone = 1,
            encounterTable = mapOf(EncounterChallengeRating.SIX to 1),
            itemTable = theOneItemChanceTable),
        9 to LevelBlueprint(levelDepth = 9,
            satellitesPerZone = 30,
            maxItemsPerZone = 1,
            encounterTable = mapOf(EncounterChallengeRating.SEVEN to 1),
            itemTable = theOneItemChanceTable)
    )
}