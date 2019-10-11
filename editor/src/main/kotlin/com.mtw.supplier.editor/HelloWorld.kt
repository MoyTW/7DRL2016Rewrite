package com.mtw.supplier.editor

import com.mtw.supplier.region.Region
import com.mtw.supplier.region.RegionGridLayout
import com.mtw.supplier.region.RegionGridOrientation
import javafx.scene.Group
import javafx.scene.layout.StackPane
import tornadofx.*

class HelloWorld : View() {
    // every view has a root component
    override val root = borderpane {
        top = menubar {
            menu("File") {
                item("Load", "Shortcut+L").action {
                    println("LOAD")
                }
                item("Save", "Shortcut+S").action {
                    println("SAVE")
                }
                item("Quit", "Shortcut+Q").action {
                    println("QUIT")
                }
            }
        }
        bottom = hilarity()
    }

    fun hilarity(): Group {
        val region = Region(5, 5, RegionGridLayout.HEXAGONAL, RegionGridOrientation.FLAT_TOP, gridHexRadius = 50.0)
        return group {
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

    fun horizontalStreetPane(): StackPane {
        return stackpane {
            rectangle {
                fill = c("4E9830")
                width = 100.0
                height = 100.0
            }
            rectangle {
                fill = c("919191")
                width = 100.0
                height= 40.0
            }
        }
    }
}
