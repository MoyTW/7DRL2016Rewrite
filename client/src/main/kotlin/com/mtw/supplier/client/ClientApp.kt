package com.mtw.supplier.client

import com.mtw.supplier.engine.Serializers
import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.ecs.components.*
import com.mtw.supplier.engine.ecs.components.ai.EnemyScoutAIComponent
import com.mtw.supplier.engine.encounter.EncounterRunner
import com.mtw.supplier.engine.encounter.rulebook.actions.MoveAction
import com.mtw.supplier.engine.encounter.state.EncounterState
import com.mtw.supplier.engine.utils.Constants
import com.mtw.supplier.engine.utils.SeededRand
import com.mtw.supplier.engine.utils.XYCoordinates
import kotlinx.coroutines.*
import org.hexworks.cobalt.events.api.Event
import org.hexworks.cobalt.events.api.KeepSubscription
import org.hexworks.zircon.api.CP437TilesetResources
import org.hexworks.zircon.api.DrawSurfaces
import org.hexworks.zircon.api.SwingApplications
import org.hexworks.zircon.api.application.AppConfig
import org.hexworks.zircon.api.builder.graphics.LayerBuilder
import org.hexworks.zircon.api.color.ANSITileColor
import org.hexworks.zircon.api.data.Position
import org.hexworks.zircon.api.data.Size
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.extensions.toScreen
import org.hexworks.zircon.api.graphics.TileGraphics
import org.hexworks.zircon.api.uievent.*
import org.hexworks.zircon.internal.Zircon
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import kotlin.concurrent.thread

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        ClientApp()
    }
}

/**
 * Run with `./gradlew clean client:run`
 *
 * Comment/uncomment version lines in build.gradle.kts for client to see version diffs
 */
class ClientApp {
    init {
        // Create Zircon app
        val tileGrid = SwingApplications.startTileGrid(
            AppConfig.newBuilder()
                .withSize(ClientAppConfig.CLIENT_WIDTH, ClientAppConfig.CLIENT_HEIGHT)
                .withDefaultTileset(CP437TilesetResources.rexPaint16x16())
                .build())
        val screen = tileGrid.toScreen()
        screen.display()

        // Create FoW, Entity layers & attach to screen
        val someTileGraphics = DrawSurfaces.tileGraphicsBuilder()
            .withSize(Size.create(ClientAppConfig.MAP_WIDTH, ClientAppConfig.MAP_HEIGHT))
            .build()
        screen.addLayer(LayerBuilder.newBuilder().withTileGraphics(someTileGraphics).build())
        val redTile = Tile.newBuilder()
            .withBackgroundColor(ANSITileColor.RED)
            .build()

        for (x in 5 until someTileGraphics.width - 5) {
            for (y in 5 until someTileGraphics.height - 5) {
                someTileGraphics.draw(redTile, Position.create(x, y))
            }
        }
    }
}
