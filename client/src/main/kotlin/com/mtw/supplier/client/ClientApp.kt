package com.mtw.supplier.client

import com.mtw.supplier.engine.encounter.state.EncounterState
import org.hexworks.zircon.api.CP437TilesetResources
import org.hexworks.zircon.api.DrawSurfaces
import org.hexworks.zircon.api.SwingApplications
import org.hexworks.zircon.api.application.AppConfig
import org.hexworks.zircon.api.builder.graphics.LayerBuilder
import org.hexworks.zircon.api.data.Size
import org.hexworks.zircon.api.extensions.toScreen
import org.hexworks.zircon.api.graphics.TileGraphics
import org.hexworks.zircon.api.uievent.*
import org.slf4j.LoggerFactory

enum class Direction(val dx: Int, val dy: Int) {
    N(0, 1),
    NE(1, 1),
    E(1, 0),
    SE(1, -1),
    S(0, -1),
    SW(-1, -1),
    W(-1, 0),
    NW(-1, 1)
}

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        // Create Zircon app
        val tileGrid = SwingApplications.startTileGrid(
            AppConfig.newBuilder()
                .withSize(ClientAppConfig.CLIENT_WIDTH, ClientAppConfig.CLIENT_HEIGHT)
                .withDefaultTileset(CP437TilesetResources.rexPaint16x16())
                .build())
        val screen = tileGrid.toScreen()
        screen.display()

        // Network stuff
        val networkClient = NetworkClient()

        // Create FoW, Entity layers & attach to screen
        val mapFoWTileGraphics: TileGraphics = DrawSurfaces.tileGraphicsBuilder()
            .withSize(Size.create(ClientAppConfig.MAP_WIDTH, ClientAppConfig.MAP_HEIGHT))
            .build()
        screen.addLayer(LayerBuilder.newBuilder().withTileGraphics(mapFoWTileGraphics).build())
        val mapProjectilePathTileGraphics: TileGraphics = DrawSurfaces.tileGraphicsBuilder()
            .withSize(Size.create(ClientAppConfig.MAP_WIDTH, ClientAppConfig.MAP_HEIGHT))
            .build()
        screen.addLayer(LayerBuilder.newBuilder().withTileGraphics(mapProjectilePathTileGraphics).build())
        val mapEntityTileGraphics: TileGraphics = DrawSurfaces.tileGraphicsBuilder()
            .withSize(Size.create(ClientAppConfig.MAP_WIDTH, ClientAppConfig.MAP_HEIGHT))
            .build()
        screen.addLayer(LayerBuilder.newBuilder().withTileGraphics(mapEntityTileGraphics).build())

        // Add input handler
        tileGrid.processKeyboardEvents(KeyboardEventType.KEY_PRESSED) { keyboardEvent: KeyboardEvent, uiEventPhase: UIEventPhase ->
            val newEncounterState = ClientApp.handleKeyPress(keyboardEvent, networkClient)
            ClientDrawer.drawGameState(mapFoWTileGraphics, mapProjectilePathTileGraphics, mapEntityTileGraphics, newEncounterState)
            UIEventResponse.pass()
        }

        ClientDrawer.drawGameState(mapFoWTileGraphics, mapProjectilePathTileGraphics, mapEntityTileGraphics, networkClient.refreshEncounterState())
    }
}

object ClientApp {
    val logger = LoggerFactory.getLogger(this::class.java)

    fun handleKeyPress(event: KeyboardEvent, client: NetworkClient): EncounterState? {
        return when (event.code) {
            KeyCode.NUMPAD_1 ->  client.postMoveAction(Direction.SW) 
            KeyCode.KEY_B ->  client.postMoveAction(Direction.SW) 
            KeyCode.NUMPAD_2 ->  client.postMoveAction(Direction.S) 
            KeyCode.KEY_J ->  client.postMoveAction(Direction.S) 
            KeyCode.NUMPAD_3 ->  client.postMoveAction(Direction.SE) 
            KeyCode.KEY_N ->  client.postMoveAction(Direction.SE) 
            KeyCode.NUMPAD_4 ->  client.postMoveAction(Direction.W) 
            KeyCode.KEY_H ->  client.postMoveAction(Direction.W) 
            KeyCode.NUMPAD_5 ->  client.postWaitAction() 
            KeyCode.PERIOD ->  client.postWaitAction() 
            KeyCode.NUMPAD_6 ->  client.postMoveAction(Direction.E) 
            KeyCode.KEY_L ->  client.postMoveAction(Direction.E) 
            KeyCode.NUMPAD_7 ->  client.postMoveAction(Direction.NW) 
            KeyCode.KEY_Y ->  client.postMoveAction(Direction.NW) 
            KeyCode.NUMPAD_8 ->  client.postMoveAction(Direction.N) 
            KeyCode.KEY_K ->  client.postMoveAction(Direction.N) 
            KeyCode.NUMPAD_9 ->  client.postMoveAction(Direction.NE) 
            KeyCode.KEY_U ->  client.postMoveAction(Direction.NE) 
            else -> null
        }
    }
}
