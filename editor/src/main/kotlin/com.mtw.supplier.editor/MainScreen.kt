package com.mtw.supplier.editor

import com.mtw.supplier.Direction
import com.mtw.supplier.Serializers
import com.mtw.supplier.ecs.components.PlayerComponent
import com.mtw.supplier.ecs.components.ai.AIComponent
import com.mtw.supplier.ecs.components.ai.PathAIComponent
import com.mtw.supplier.encounter.state.EncounterState
import com.mtw.supplier.utils.XYCoordinates
import io.github.rybalkinsd.kohttp.dsl.httpGet
import io.github.rybalkinsd.kohttp.dsl.httpPost
import io.github.rybalkinsd.kohttp.ext.asString
//import com.mtw.supplier.region.*
import javafx.scene.Group
import javafx.scene.control.ListView
import javafx.scene.control.ScrollPane
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import okhttp3.Response
import tornadofx.*

class GameScreen: View() {
    private val SERVER_PORT = 8080

    private val json = Json(JsonConfiguration.Stable.copy(prettyPrint = true),
        context = Serializers.componentSerializersModuleBuilder())

    private var mainScrollPane: ScrollPane by singleAssign()
    private var regionLinesStackpane: StackPane by singleAssign()
    private var logListView: ListView<String> by singleAssign()

    private var encounterState: EncounterState? = null

    override val root = borderpane {
        top {
            menubar {
                menu("File") {
                    item("Refersh", "Shortcut+R").action {
                        encounterState = resetGame()
                        encounterStateRender()
                    }
                    item("Quit", "Shortcut+Q").action {
                        println("QUIT")
                    }
                }
            }
        }
        center {
            mainScrollPane = scrollpane {
                stackpane {
                    regionLinesStackpane = stackpane ()
                    // TODO: Figure out a better way
                    keyboard {
                        addEventFilter(KeyEvent.KEY_PRESSED) { handleKeyPress(it) }
                    }
                }
            }
        }
        bottom {
            logListView = listview<String> {
                this.maxHeight = 200.0
            }
        }
        encounterState = refreshEncounterState()
        encounterStateRender()
    }

    private fun handleKeyPress(event: KeyEvent) {
        when (event.code) {
            KeyCode.NUMPAD1 -> { encounterState = postMoveAction(Direction.SW); encounterStateRender() }
            KeyCode.NUMPAD2 -> { encounterState = postMoveAction(Direction.S); encounterStateRender() }
            KeyCode.NUMPAD3 -> { encounterState = postMoveAction(Direction.SE); encounterStateRender() }
            KeyCode.NUMPAD4 -> { encounterState = postMoveAction(Direction.W); encounterStateRender() }
            KeyCode.NUMPAD5 -> { encounterState = postWaitAction(); encounterStateRender() }
            KeyCode.NUMPAD6 -> { encounterState = postMoveAction(Direction.E); encounterStateRender() }
            KeyCode.NUMPAD7 -> { encounterState = postMoveAction(Direction.NW); encounterStateRender() }
            KeyCode.NUMPAD8 -> { encounterState = postMoveAction(Direction.N); encounterStateRender() }
            KeyCode.NUMPAD9 -> { encounterState = postMoveAction(Direction.NE); encounterStateRender() }
            else -> {}
        }
    }

    private fun postWaitAction(): EncounterState? {
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

    private fun postMoveAction(direction: Direction): EncounterState? {
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

    private fun refreshEncounterState(): EncounterState? {
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

    private fun resetGame(): EncounterState? {
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

    private fun squares(encounterState: EncounterState?): Group {
        val UNEXPLORED_COLOR = Color.BLACK
        val EXPLORED_COLOR = Color.DARKGRAY
        val VISIBLE_COLOR = Color.LIGHTGRAY
        val PROJECTILE_COLOR = Color.ORANGERED
        val ENEMY_COLOR = Color.DARKRED

        if (encounterState == null) {
            return group()
        }

        val tiles = encounterState.getEncounterTileMap()
        val fov = encounterState.fovCache

        return group {
            val tileSize = 20.0
            for (x in 0 until tiles.width) {
                for (y in 0 until tiles.height) {

                    val tile = tiles.getTileView(x, y)
                    rectangle {
                        this.x = x * tileSize
                        this.y = -y * tileSize
                        width = tileSize
                        height = tileSize
                        stroke = Color.WHITE
                        fill = when {
                            tile?.explored == false -> { UNEXPLORED_COLOR }
                            !fov!!.isInFoV(XYCoordinates(x, y)) -> { EXPLORED_COLOR }
                            else -> { VISIBLE_COLOR }
                        }
                    }
                    tile?.entities?.map {
                        if (it.hasComponent(PathAIComponent::class)) {
                            circle {
                                centerX = x * tileSize - (tileSize / 2)
                                centerY = -(y * tileSize - (tileSize / 2))
                                radius = tileSize / 4
                                fill = PROJECTILE_COLOR
                            }
                        } else if (it.hasComponent(AIComponent::class)) {
                            rectangle {
                                this.x = x * tileSize
                                this.y = -y * tileSize
                                width = tileSize * .75
                                height = tileSize * .75
                                rotate = 45.0
                                fill = ENEMY_COLOR
                            }
                        } else if (it.hasComponent(PlayerComponent::class)) {
                            rectangle {
                                this.x = x * tileSize
                                this.y = -y * tileSize
                                width = tileSize * .75
                                height = tileSize * .75
                                rotate = 45.0
                                fill = Color.GREEN
                            }
                        } else { }
                    }

                }
            }
        }
    }

    private fun encounterStateRender() {
        regionLinesStackpane.replaceChildren(squares(this.encounterState))
        val messages = encounterState?.messageLog?.getMessages()?.reversed()
        if (messages != null) {
            logListView.items.clear()
            logListView.items.addAll(messages)
            logListView.scrollTo(messages.size - 1)
        }
    }
}
