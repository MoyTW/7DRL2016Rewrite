package com.mtw.supplier.client

import com.mtw.supplier.engine.Serializers
import com.mtw.supplier.engine.ecs.components.EncounterLocationComponent
import com.mtw.supplier.engine.encounter.EncounterRunner
import com.mtw.supplier.engine.encounter.rulebook.Action
import com.mtw.supplier.engine.encounter.rulebook.Rulebook
import com.mtw.supplier.engine.encounter.rulebook.actions.AutopilotAction
import com.mtw.supplier.engine.encounter.rulebook.actions.MoveAction
import com.mtw.supplier.engine.encounter.rulebook.actions.PickUpItemAction
import com.mtw.supplier.engine.encounter.rulebook.actions.WaitAction
import com.mtw.supplier.engine.encounter.state.EncounterState
import kotlinx.coroutines.*
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
import java.io.File
import java.nio.file.Paths

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
        ClientApp()
    }
}

class ClientApp() {
    val logger = LoggerFactory.getLogger(this::class.java)

    val networkClient: NetworkClient
    // Screen handles
    val mapFoWTileGraphics: TileGraphics
    val mapProjectilePathTileGraphics: TileGraphics
    val mapEntityTileGraphics: TileGraphics

    var encounterState: EncounterState?

    init {
        // Create Zircon app
        val tileGrid = SwingApplications.startTileGrid(
            AppConfig.newBuilder()
                .withSize(ClientAppConfig.CLIENT_WIDTH, ClientAppConfig.CLIENT_HEIGHT)
                .withDefaultTileset(CP437TilesetResources.rexPaint16x16())
                .build())
        val screen = tileGrid.toScreen()
        screen.display()

        // Network stuff
        networkClient = NetworkClient()

        // Create FoW, Entity layers & attach to screen
        mapFoWTileGraphics = DrawSurfaces.tileGraphicsBuilder()
            .withSize(Size.create(ClientAppConfig.MAP_WIDTH, ClientAppConfig.MAP_HEIGHT))
            .build()
        screen.addLayer(LayerBuilder.newBuilder().withTileGraphics(mapFoWTileGraphics).build())
        mapProjectilePathTileGraphics = DrawSurfaces.tileGraphicsBuilder()
            .withSize(Size.create(ClientAppConfig.MAP_WIDTH, ClientAppConfig.MAP_HEIGHT))
            .build()
        screen.addLayer(LayerBuilder.newBuilder().withTileGraphics(mapProjectilePathTileGraphics).build())
        mapEntityTileGraphics = DrawSurfaces.tileGraphicsBuilder()
            .withSize(Size.create(ClientAppConfig.MAP_WIDTH, ClientAppConfig.MAP_HEIGHT))
            .build()
        screen.addLayer(LayerBuilder.newBuilder().withTileGraphics(mapEntityTileGraphics).build())

        // Add input handler
        tileGrid.processKeyboardEvents(KeyboardEventType.KEY_PRESSED) { keyboardEvent: KeyboardEvent, uiEventPhase: UIEventPhase ->
            handleKeyPress(keyboardEvent, networkClient)
            UIEventResponse.pass()
        }

        encounterState = networkClient.refreshEncounterState()
        drawGameState()
    }

    private fun drawGameState(encounterState: EncounterState? = this.encounterState) {
        ClientDrawer.drawGameState(mapFoWTileGraphics, mapProjectilePathTileGraphics, mapEntityTileGraphics, encounterState)
    }

    private fun executePlayerAction(action: Action) {
        val serverEncounterState = GlobalScope.async {
            networkClient.postAction(action)
        }
        EncounterRunner.runPlayerTurnAndUntilReady(encounterState!!, action)
        drawGameState()
        // I'm reasonably sure using runBlocking like this isn't idiomatic.
        runBlocking {
            val serverEncounterStateString = serverEncounterState.await()
            val stringifiedLocal = Serializers.stringify(encounterState!!)
            if (stringifiedLocal != serverEncounterStateString) {
                logger.error("You've desync'd somehow! F.")
                File(Paths.get("").toAbsolutePath().toString() + "/tmp/client.json").writeText(stringifiedLocal)
                File(Paths.get("").toAbsolutePath().toString() + "/tmp/server.json").writeText(serverEncounterStateString!!)
                encounterState = Serializers.parse(serverEncounterStateString!!)

                drawGameState()
            }
        }
    }

    private fun directionToMoveAction(direction: Direction): MoveAction {
        val oldPlayerPos = encounterState!!.playerEntity().getComponent(EncounterLocationComponent::class).position
		val newPlayerPos = oldPlayerPos.copy(
			x = oldPlayerPos.x + direction.dx, y = oldPlayerPos.y + direction.dy)

		return MoveAction(encounterState!!.playerEntity().id, newPlayerPos)
    }

    fun handleKeyPress(event: KeyboardEvent, client: NetworkClient) {
        when (event.code) {
            // ===== Movement =====
            KeyCode.NUMPAD_1, KeyCode.KEY_B ->  executePlayerAction(directionToMoveAction(Direction.SW))
            KeyCode.NUMPAD_2, KeyCode.KEY_J ->  executePlayerAction(directionToMoveAction(Direction.S))
            KeyCode.NUMPAD_3, KeyCode.KEY_N ->  executePlayerAction(directionToMoveAction(Direction.SE))
            KeyCode.NUMPAD_4, KeyCode.KEY_H ->  executePlayerAction(directionToMoveAction(Direction.W))
            // DCSS uses period, but I want period to be a valid jump key.
            KeyCode.NUMPAD_5, KeyCode.SPACE ->  executePlayerAction(WaitAction(encounterState!!.playerEntity().id))
            KeyCode.NUMPAD_6, KeyCode.KEY_L ->  executePlayerAction(directionToMoveAction(Direction.E))
            KeyCode.NUMPAD_7, KeyCode.KEY_Y ->  executePlayerAction(directionToMoveAction(Direction.NW))
            KeyCode.NUMPAD_8, KeyCode.KEY_K ->  executePlayerAction(directionToMoveAction(Direction.N))
            KeyCode.NUMPAD_9, KeyCode.KEY_U ->  executePlayerAction(directionToMoveAction(Direction.NE))

            // ===== Instant Actions =====
            KeyCode.KEY_G -> executePlayerAction(PickUpItemAction(encounterState!!.playerEntity().id))
            /**
             * To implement autopilot:
             * 1. Do we allow the pilot to interrupt autopilot?
             *   This influences how the client/server relationship works. If we don't allow interruptions, we can just
             * model it as an Autopilot Action or something and resolve it fully on the client and server.
             *   If we make it interruptible, we have either:
             *   1a. Autopilot is entirely on the client side, we just send a storm of movement actions to server. This
             *     seems...bad. The issue here is you're restricting the
             * WAIT STOP THE PRESSES
             * DCSS online client does autoexplore as "Instant, but it displays the path you took by footprints"
             * Hmm! And it *doesn't* do that on the desktop client, so that's a online-specific affordance...
             * ...I think I'll steal it! Let's just do that. Okay. This comment is over now.
             */
            // TODO: Pick a zone!
            KeyCode.KEY_A -> executePlayerAction(AutopilotAction(encounterState!!.playerEntity().id, "unused value"))
            KeyCode.KEY_I -> println("Implement using items from inventory") // TODO
            KeyCode.KEY_D -> println("Implement dropping from inventory") // TODO
            KeyCode.PERIOD, KeyCode.GREATER, KeyCode.COMMA, KeyCode.LESS -> println("Implement stairs movement") // TODO
            KeyCode.KEY_C -> println("Implement level up screen") // TODO
            KeyCode.KEY_R -> println("Implement intel screen") // TODO
            // TODO: Since we're dropping mouse support, add a "look" option!

            else -> null
        }
    }
}
