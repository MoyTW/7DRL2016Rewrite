package com.mtw.supplier.editor

import com.mtw.supplier.Direction
import com.mtw.supplier.Serializers
import com.mtw.supplier.ecs.Entity
import com.mtw.supplier.ecs.components.DisplayComponent
import com.mtw.supplier.ecs.components.DisplayType
import com.mtw.supplier.ecs.components.EncounterLocationComponent
import com.mtw.supplier.ecs.components.PlayerComponent
import com.mtw.supplier.encounter.state.EncounterState
import com.mtw.supplier.encounter.state.FoVCache
import com.mtw.supplier.utils.XYCoordinates
import io.github.rybalkinsd.kohttp.dsl.httpGet
import io.github.rybalkinsd.kohttp.dsl.httpPost
import io.github.rybalkinsd.kohttp.ext.asString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.Response
import org.hexworks.zircon.api.CP437TilesetResources
import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.DrawSurfaces
import org.hexworks.zircon.api.SwingApplications
import org.hexworks.zircon.api.application.AppConfig
import org.hexworks.zircon.api.builder.graphics.LayerBuilder
import org.hexworks.zircon.api.color.ANSITileColor
import org.hexworks.zircon.api.color.TileColor
import org.hexworks.zircon.api.data.Position
import org.hexworks.zircon.api.data.Size
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.extensions.toScreen
import org.hexworks.zircon.api.graphics.TileGraphics
import org.hexworks.zircon.api.uievent.*

object EditorApp {
    //val gameState = GameState()
    val GAME_WIDTH: Int = 60
    val GAME_HEIGHT: Int = 40
    val MAP_WIDTH: Int = 60
    val MAP_HEIGHT: Int = 40
    // TODO: Log
    // val LOG_WIDTH: Int = GAME_WIDTH
    // val LOG_HEIGHT: Int = GAME_HEIGHT - MAP_HEIGHT
    val MAP_CENTER = XYCoordinates(MAP_WIDTH / 2, MAP_HEIGHT / 2)


    @JvmStatic
    fun main(args: Array<String>) {
        // Create Zircon app
        val tileGrid = SwingApplications.startTileGrid(
            AppConfig.newBuilder()
                .withSize(GAME_WIDTH, GAME_HEIGHT)
                .withDefaultTileset(CP437TilesetResources.rexPaint16x16())
                .build())
        val screen = tileGrid.toScreen()
        screen.display()

        // Network stuff
        val networkClient = NetworkClient()

        // Create FoW, Entity layers & attach to screen
        val mapFoWTileGraphics: TileGraphics = DrawSurfaces.tileGraphicsBuilder()
            .withSize(Size.create(MAP_WIDTH, MAP_HEIGHT))
            .build()
        screen.addLayer(LayerBuilder.newBuilder().withTileGraphics(mapFoWTileGraphics).build())
        val mapEntityTileGraphics: TileGraphics = DrawSurfaces.tileGraphicsBuilder()
            .withSize(Size.create(MAP_WIDTH, MAP_HEIGHT))
            .build()
        screen.addLayer(LayerBuilder.newBuilder().withTileGraphics(mapEntityTileGraphics).build())

        // Add input handler
        tileGrid.processKeyboardEvents(KeyboardEventType.KEY_PRESSED) { keyboardEvent: KeyboardEvent, uiEventPhase: UIEventPhase ->
            handleKeyPress(keyboardEvent, networkClient)
            renderGameState(mapFoWTileGraphics, mapEntityTileGraphics, networkClient.refreshEncounterState())
            UIEventResponse.pass()
        }

        renderGameState(mapFoWTileGraphics, mapEntityTileGraphics, networkClient.refreshEncounterState())
    }

    private fun renderGameState(mapFoWTileGraphics: TileGraphics,
                                mapEntityTileGraphics: TileGraphics,
                                encounterState: EncounterState?) {
        // TODO: log this
        if (encounterState == null) {
            return
        }

        // TODO: Game over/reset screen
        if (encounterState.completed) {
            return
        }

        // Draw the map
        mapFoWTileGraphics.clear()
        mapEntityTileGraphics.clear()
        val playerPos = encounterState.playerEntity().getComponent(EncounterLocationComponent::class).position
        val cameraX = playerPos.x
        val cameraY = playerPos.y

        renderFoWTiles(mapFoWTileGraphics, encounterState, cameraX, cameraY)
        renderDisplayEntities(mapEntityTileGraphics, encounterState, cameraX, cameraY)
    }

    // TODO: Remove CameraX, CameraY
    private fun renderFoWTiles(tileGraphics: TileGraphics, encounterState: EncounterState, cameraX: Int, cameraY: Int) {
        val tiles = encounterState.getEncounterTileMap()
        val fov = encounterState.fovCache

        val unexploredTile = Tile.newBuilder()
            .withBackgroundColor(ANSITileColor.BLACK)
            .build()
        val exploredTile = Tile.newBuilder()
            .withBackgroundColor(ANSITileColor.GRAY)
            .build()
        val visibleTile = Tile.newBuilder()
            .withBackgroundColor(ANSITileColor.WHITE)
            .build()
        for (x in 0 until tiles.width) {
            for (y in 0 until tiles.height) {
                // TODO: Normalize using either x, y or XYCoords or ???
                val pos = XYCoordinates(x, y)
                val tile = tiles.getTileView(x, y)
                val drawTile = when {
                    !tile!!.explored -> { unexploredTile }
                    !fov!!.isInFoV(pos) -> { exploredTile }
                    else -> { visibleTile }
                }
                draw(tileGraphics, drawTile, pos, cameraX, cameraY)
            }
        }
    }

    private fun draw(tileGraphics: TileGraphics, tile: Tile, pos: XYCoordinates, cameraX: Int, cameraY: Int) {
        val screenPos = toCameraCoordinates(pos, cameraX, cameraY)
        tileGraphics.draw(tile, Position.create(screenPos.x, tileGraphics.height - screenPos.y - 1))
    }

    private fun toCameraCoordinates(pos: XYCoordinates, cameraX: Int, cameraY: Int): XYCoordinates {
        return XYCoordinates(pos.x - cameraX + MAP_CENTER.x, pos.y - cameraY + MAP_CENTER.y)
    }

    private fun renderDisplayEntities(tileGraphics: TileGraphics, encounterState: EncounterState, cameraX: Int, cameraY: Int) {
        val fowCache = encounterState.fovCache!!
        encounterState.entities()
            .filter { it.hasComponent(EncounterLocationComponent::class) && it.hasComponent(DisplayComponent::class) }
            .sortedByDescending { it.getComponent(DisplayComponent::class).displayType.priority }
            .map {
                val entityPos = it.getComponent(EncounterLocationComponent::class).position
                toTile(entityPos, it, fowCache)?.let { tile -> draw(tileGraphics, tile, entityPos, cameraX, cameraY) }
            }
    }

    private fun toTile(entityPos: XYCoordinates, entity: Entity, foVCache: FoVCache): Tile? {
        val displayComponent = entity.getComponent(DisplayComponent::class)
        if (!displayComponent.seeInFoW && !foVCache.isInFoV(entityPos)) {
            return null
        } else {
            return when (displayComponent.displayType) {
                DisplayType.PLAYER -> Tile.newBuilder()
                    .withCharacter('@')
                    .withForegroundColor(TileColor.create(0, 255, 0))
                    .withBackgroundColor(TileColor.transparent())
                    .build()
                DisplayType.ENEMY_SCOUT -> Tile.newBuilder()
                    .withCharacter('s')
                    .withForegroundColor(TileColor.create(255, 0, 0))
                    .withBackgroundColor(TileColor.transparent())
                    .build()
                DisplayType.PROJECTILE_SMALL_SHOTGUN -> Tile.newBuilder()
                    .withCharacter('.')
                    .withForegroundColor(TileColor.create(255, 70, 0))
                    .withBackgroundColor(TileColor.transparent())
                    .build()
            }
        }
    }

    private fun handleKeyPress(event: KeyboardEvent, client: NetworkClient): EncounterState? {
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

class NetworkClient(
    private val SERVER_PORT: Int = 8080,
    private val json: Json = Json(JsonConfiguration.Stable.copy(prettyPrint = true),
        context = Serializers.componentSerializersModuleBuilder())
) {
    fun postWaitAction(): EncounterState? {
        val response: Response = httpPost {
            host = "localhost"
            port = SERVER_PORT
            path = "/game/player/action/wait"
        }
        response.use {
            val body = response.asString()
            return if (body != null) {
                json.parse(EncounterState.serializer(), body)
            } else {
                null
            }
        }
    }

    fun postMoveAction(direction: Direction): EncounterState? {
        val response: Response = httpPost {
            host = "localhost"
            port = SERVER_PORT
            path = "/game/player/action/move"
            body {
                json {
                    "direction" to direction.name
                }
            }
        }
        response.use {
            val body = response.asString()
            return if (body != null) {
                json.parse(EncounterState.serializer(), body)
            } else {
                null
            }
        }
    }

    fun refreshEncounterState(): EncounterState? {
        val response: Response = httpGet {
            host = "localhost"
            port = SERVER_PORT
            path = "/game/state"
        }
        response.use {
            val body = response.asString()
            return if (body != null) {
                json.parse(EncounterState.serializer(), body)
            } else {
                null
            }
        }
    }

    fun resetGame(): EncounterState? {
        val response: Response = httpPost {
            host = "localhost"
            port = SERVER_PORT
            path = "/game/reset"
        }
        response.use {
            val body = response.asString()
            return if (body != null) {
                json.parse(EncounterState.serializer(), body)
            } else {
                null
            }
        }
    }
}
