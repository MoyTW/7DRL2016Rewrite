package com.mtw.supplier.editor

import com.mtw.supplier.region.Region
import com.mtw.supplier.region.RegionGridLayout
import com.mtw.supplier.region.RegionGridOrientation
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.stage.FileChooser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import tornadofx.*
import java.io.File
import java.nio.file.Paths
import javafx.scene.image.WritableImage
import kotlin.math.roundToInt


class MainScreen : View() {
    data class RegionFile(var name: String, var path: File?, var region: Region)
    data class BackgroundFile(var path: File?)

    private val json = Json(JsonConfiguration.Stable.copy(prettyPrint = true))

    private val runPath = Paths.get("").toAbsolutePath().toString()
    private val pngFilters = arrayOf(FileChooser.ExtensionFilter("pngs!!!", "*png"))
    private val regionFileFilters = arrayOf(FileChooser.ExtensionFilter("REGION FILE", "*region"))

    private val gridHeight = SimpleIntegerProperty(this, "gridHeight", 20)
    private val gridWidth = SimpleIntegerProperty(this, "gridWidth", 20)
    private val gridRadius = SimpleDoubleProperty(this, "gridRadius", 10.0)
    private var regionFile: RegionFile = RegionFile(
        "Unnamed Region File",
        null,
        Region(gridHeight.value, gridWidth.value, RegionGridLayout.RECTANGULAR, RegionGridOrientation.FLAT_TOP, gridHexRadius = gridRadius.value)
    )
    private var backgroundFile: BackgroundFile = BackgroundFile(null)

    override val root = borderpane {
        top = menubar {
            menu("File") {
                item("Load", "Shortcut+L").action { doLoadRegionFile() }
                item("Save", "Shortcut+S").action { doSaveRegionFile() }
                item("Save As", "F12").action { doSaveAsRegionFile() }
                item("Quit", "Shortcut+Q").action {
                    println("QUIT")
                }
            }
        }
        left = form {
            fieldset("Grid Controls") {
                field("Grid Height") { textfield { bind(gridHeight) } }
                field("Grid Width") { textfield { bind(gridWidth) } }
                field("Grid Radius") { textfield { bind(gridRadius) } }
                button("Regenerate Hexes").action { doRegenerate() }
            }
            fieldset("Background Controls") {
                button("Load Background").action { doLoadBackground() }
            }
        }
        center = centerRender(backgroundFile, regionFile.region)
    }

    init {
        with (root) {
            prefWidth = 800.0
            prefHeight = 600.0
        }
    }

    private fun doLoadRegionFile() {
        with (root) {
            val file = chooseFile("Select the region file", regionFileFilters, op = { initialDirectory = File(runPath) }).firstOrNull()
            if (file != null) {
                val region = json.parse(Region.serializer(), file.readText())
                regionFile.name = file.name
                regionFile.path = file
                regionFile.region = region
                gridHeight.set(region.gridHeight)
                gridWidth.set(region.gridWidth)
                center = centerRender(backgroundFile, region)
            }
        }
    }

    private fun doSaveAsRegionFile() {
        var outFile = chooseFile("Name the save file", regionFileFilters, FileChooserMode.Save, op = { initialDirectory = File(runPath) }).firstOrNull()
        if (outFile != null) {
            if (!outFile.absolutePath.endsWith(".region")) {
                outFile = File(outFile.absolutePath + ".region")
            }
            regionFile.path = outFile
            val regionJson = json.stringify(Region.serializer(), regionFile.region)
            outFile.writeText(regionJson)
        }
    }

    private fun doSaveRegionFile() {
        if (regionFile.path == null) {
            doSaveAsRegionFile()
        } else {
            val regionJson = json.stringify(Region.serializer(), regionFile.region)
            regionFile.path!!.writeText(regionJson)
        }
    }

    private fun doLoadBackground() {
        with (root) {
            val file = chooseFile("FILE", pngFilters, op = { initialDirectory = File(runPath) }).firstOrNull()
            if (file != null) {
                backgroundFile.path = file
                center = centerRender(backgroundFile, regionFile.region)
            }
        }
    }

    private fun doRegenerate() {
        with(root) {
            regionFile.region = Region(gridHeight.value, gridWidth.value, RegionGridLayout.RECTANGULAR, RegionGridOrientation.FLAT_TOP, gridHexRadius = gridRadius.value)
            center = centerRender(backgroundFile, regionFile.region)
        }
    }

    private fun maxXY(region: Region): Pair<Double,Double> {
        var maxX = 0.0
        var maxY = 0.0
        for (points in region.getAllPoints()) {
            for (point in points) {
                if (point.first > maxX) {
                    maxX = point.first
                }
                if (point.second > maxY) {
                    maxY = point.second
                }
            }
        }
        return Pair(maxX, maxY)
    }

    // I can't figure out how to make these things work in functions so I'm doing this now because wait it's 12:30
    // already what the i got work tomorrow. wtf. ok technically i got work today. rip me
    private fun centerRender(backgroundFile: BackgroundFile, region: Region): ScrollPane {
        val (maxX, maxY) = maxXY(regionFile.region)
        if (backgroundFile.path != null) {
            return scrollpane {
                stackpane {
                    group {
                        imageview {
                            image=Image("file:\\${backgroundFile.path!!.canonicalPath}", maxX, maxY, false, false)
                        }
                        region.getAllPoints().map {
                            polyline(
                                it[0].first, it[0].second,
                                it[1].first, it[1].second,
                                it[2].first, it[2].second,
                                it[3].first, it[3].second,
                                it[4].first, it[4].second,
                                it[5].first, it[5].second,
                                it[0].first, it[0].second
                            )
                        }
                    }
                }
            }
        } else {
            return scrollpane{
                group {
                    region.getAllPoints().map {
                        polyline(
                            it[0].first, it[0].second,
                            it[1].first, it[1].second,
                            it[2].first, it[2].second,
                            it[3].first, it[3].second,
                            it[4].first, it[4].second,
                            it[5].first, it[5].second,
                            it[0].first, it[0].second
                        )
                    }
                }
            }
        }
    }
}
