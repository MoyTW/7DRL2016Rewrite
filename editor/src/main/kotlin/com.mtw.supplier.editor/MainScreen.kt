package com.mtw.supplier.editor

import com.mtw.supplier.region.Region
import com.mtw.supplier.region.RegionGridLayout
import com.mtw.supplier.region.RegionGridOrientation
import javafx.scene.control.ScrollPane
import javafx.scene.layout.StackPane
import tornadofx.*

class MainScreen : View() {

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
        center = regionRender(Region(20, 20, RegionGridLayout.RECTANGULAR, RegionGridOrientation.FLAT_TOP, gridHexRadius = 50.0))
    }

    init {
        with (root) {
            prefWidth = 800.0
            prefHeight = 600.0
        }
    }

    private fun regionRender(region: Region): ScrollPane {
        return scrollpane {
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
