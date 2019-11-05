package com.mtw.supplier.region

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.hexworks.mixite.core.api.HexagonOrientation
import org.hexworks.mixite.core.api.HexagonalGridLayout
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
class RegionTests {
    private val json = Json(JsonConfiguration.Stable.copy(prettyPrint = true))
    private val coordinates00 = CubeCoordinates(0, 0)
    private val coordinates11 = CubeCoordinates(1, 1)

    @Test
    fun testEmptyRegionSerializationToJson() {
        val region = Region(1, 1, RegionGridLayout.RECTANGULAR, RegionGridOrientation.FLAT_TOP)
        val jsonData = json.stringify(Region.serializer(), region)
        val expected = """
            {
                "gridHeight": 1,
                "gridWidth": 1,
                "gridLayout": "RECTANGULAR",
                "gridOrientation": "FLAT_TOP",
                "encounterRegistry": {
                    "regionalEncounters": [
                    ]
                },
                "factionRegistry": {
                    "factions": {
                    }
                },
                "cubeCoordinatesToRegionHexes": [
                ]
            }
            """.trimIndent()
        Assert.assertEquals(
            expected,
            jsonData)
    }

    @Test
    fun testRegionAndRegionHexSerializationToJson() {
        val expected = """
            {
                "gridHeight": 2,
                "gridWidth": 2,
                "gridLayout": "RECTANGULAR",
                "gridOrientation": "FLAT_TOP",
                "encounterRegistry": {
                    "regionalEncounters": [
                    ]
                },
                "factionRegistry": {
                    "factions": {
                    }
                },
                "cubeCoordinatesToRegionHexes": [
                    {
                        "gridX": 0,
                        "gridZ": 0
                    },
                    {
                        "coordinates": {
                            "gridX": 0,
                            "gridZ": 0
                        },
                        "vegetationPercentage": 50,
                        "elevation": 69,
                        "hexEffects": [
                        ],
                        "possibleEncounterIdsToProbabilities": {
                        },
                        "passable": true,
                        "opaque": false,
                        "movementCost": 0.0
                    },
                    {
                        "gridX": 1,
                        "gridZ": 1
                    },
                    {
                        "coordinates": {
                            "gridX": 1,
                            "gridZ": 1
                        },
                        "vegetationPercentage": 1,
                        "elevation": 1,
                        "hexEffects": [
                        ],
                        "possibleEncounterIdsToProbabilities": {
                        },
                        "passable": true,
                        "opaque": false,
                        "movementCost": 0.0
                    }
                ]
            }
        """.trimIndent()

        val region = Region(2, 2, RegionGridLayout.RECTANGULAR, RegionGridOrientation.FLAT_TOP)
        region.setHex(coordinates00, RegionHex(coordinates00,50, 69))
        region.setHex(coordinates11, RegionHex(coordinates11,1, 1))
        val jsonData = json.stringify(Region.serializer(), region)
        Assert.assertEquals(
            expected,
            jsonData)
    }

    @Test
    fun testRegionAndRegionHexSerializationFromJson() {
        val jsonString = """
            {
                "gridHeight": 2,
                "gridWidth": 2,
                "gridLayout": "RECTANGULAR",
                "gridOrientation": "FLAT_TOP",
                "cubeCoordinatesToRegionHexes": [
                    {
                        "gridX": 0,
                        "gridZ": 0
                    },
                    {
                        "coordinates": {
                            "gridX": 0,
                            "gridZ": 0
                        },
                        "vegetationPercentage": 50,
                        "elevation": 69,
                        "hexEffects": [
                        ],
                        "possibleEncounterIdsToProbabilities": {
                        },
                        "passable": true,
                        "opaque": false,
                        "movementCost": 0.0
                    },
                    {
                        "gridX": 1,
                        "gridZ": 1
                    },
                    {
                        "coordinates": {
                            "gridX": 1,
                            "gridZ": 1
                        },
                        "vegetationPercentage": 1,
                        "elevation": 1,
                        "hexEffects": [
                        ],
                        "possibleEncounterIdsToProbabilities": {
                        },
                        "passable": true,
                        "opaque": false,
                        "movementCost": 0.0
                    }
                ]
            }
        """.trimIndent()

        val region = json.parse(Region.serializer(), jsonString)
        Assert.assertEquals(2, region.gridHeight)
        Assert.assertEquals(2, region.gridWidth)
        Assert.assertEquals(RegionGridLayout.RECTANGULAR, region.gridLayout)
        Assert.assertEquals(RegionGridOrientation.FLAT_TOP, region.gridOrientation)

        val hex00: RegionHex = region.getHex(CubeCoordinates(0, 0))!!
        Assert.assertEquals(50, hex00.vegetationPercentage)
        Assert.assertEquals(69, hex00.elevation)
        Assert.assertEquals(true, hex00.passable)

        val hex11: RegionHex = region.getHex(CubeCoordinates(1, 1))!!
        Assert.assertEquals(1, hex11.vegetationPercentage)
        Assert.assertEquals(1, hex11.elevation)
        Assert.assertEquals(true, hex11.passable)
    }
}
