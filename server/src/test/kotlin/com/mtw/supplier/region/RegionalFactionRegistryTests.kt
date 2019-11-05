package com.mtw.supplier.region

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
class EntityTests {
    @Test
    fun testSerializationToJson() {
        val json = Json(JsonConfiguration.Stable)

        val beastFaction = RegionalFaction(0, "beasts", mutableMapOf(0 to 100.0, 1 to -10.0))
        val expeditionFaction = RegionalFaction(1, "expedition", mutableMapOf(0 to -25.0, 1 to 10.0))
        val registry = RegionalFactionRegistry()
            .addFaction(beastFaction)
            .addFaction(expeditionFaction)

        val jsonData = json.stringify(RegionalFactionRegistry.serializer(), registry)
        Assert.assertEquals(
            "{\"factions\":{\"0\":{\"id\":0,\"name\":\"beasts\",\"factionIdsToRelationshipScores\":{\"0\":100.0,\"1\":-10.0}},\"1\":{\"id\":1,\"name\":\"expedition\",\"factionIdsToRelationshipScores\":{\"0\":-25.0,\"1\":10.0}}}}",
            jsonData)
    }

    @Test
    fun testSerializationFromJson() {
        val jsonString = "{\"factions\":{\"0\":{\"id\":0,\"name\":\"beasts\",\"factionIdsToRelationshipScores\":{\"0\":100.0,\"1\":-10.0}},\"1\":{\"id\":1,\"name\":\"expedition\",\"factionIdsToRelationshipScores\":{\"0\":-25.0,\"1\":10.0}}}}"
        val json = Json(JsonConfiguration.Stable)
        val registry = json.parse(RegionalFactionRegistry.serializer(), jsonString)

        Assert.assertEquals("beasts", registry.getFactionName(0))
        Assert.assertEquals("expedition", registry.getFactionName(1))
        Assert.assertEquals(100.0, registry.getRelationshipScore(0, 0), 0.0)
        Assert.assertEquals(-10.0, registry.getRelationshipScore(0, 1), 0.0)
        Assert.assertEquals(-25.0, registry.getRelationshipScore(1, 0), 0.0)
        Assert.assertEquals(10.0, registry.getRelationshipScore(1, 1), 0.0)
    }

    @Test(expected = RegionalFaction.UndefinedRelationshipException::class)
    fun testThrowsExceptionWhenMissingRating() {
        val beastFaction = RegionalFaction(0, "beasts", mutableMapOf(0 to 0.0))
        val expeditionFaction = RegionalFaction(1, "expedition", mutableMapOf(0 to 0.0, 1 to 10.0))
        RegionalFactionRegistry()
            .addFaction(beastFaction)
            .addFaction(expeditionFaction)
    }

    @Test(expected = RegionalFactionRegistry.FactionAlreadyExistsException::class)
    fun testThrowsWhenDoubleAdding() {
        val beastFaction = RegionalFaction(0, "beasts", mutableMapOf(0 to 0.0, 1 to 0.0))
        val expeditionFaction = RegionalFaction(0, "expedition", mutableMapOf(0 to 5.0, 1 to 0.0))
        RegionalFactionRegistry()
            .addFaction(beastFaction)
            .addFaction(expeditionFaction)
    }
}
