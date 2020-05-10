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
import org.hexworks.zircon.api.data.Size
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

data class PlayerInputEvent(val event: KeyboardEvent, override val emitter: Any): Event

class ClientApp {
    val logger = LoggerFactory.getLogger(this::class.java)

    // Screen handles
    val mapFoWTileGraphics: TileGraphics
    //val mapProjectilePathTileGraphics: TileGraphics
    //val mapEntityTileGraphics: TileGraphics

    var encounterState: EncounterState?
    val playerInputQueue: BlockingQueue<PlayerInputEvent> = ArrayBlockingQueue(2500)

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
        mapFoWTileGraphics = DrawSurfaces.tileGraphicsBuilder()
            .withSize(Size.create(ClientAppConfig.MAP_WIDTH, ClientAppConfig.MAP_HEIGHT))
            .build()
        screen.addLayer(LayerBuilder.newBuilder().withTileGraphics(mapFoWTileGraphics).build())
        /*mapProjectilePathTileGraphics = DrawSurfaces.tileGraphicsBuilder()
            .withSize(Size.create(ClientAppConfig.MAP_WIDTH, ClientAppConfig.MAP_HEIGHT))
            .build()
        screen.addLayer(LayerBuilder.newBuilder().withTileGraphics(mapProjectilePathTileGraphics).build())
        mapEntityTileGraphics = DrawSurfaces.tileGraphicsBuilder()
            .withSize(Size.create(ClientAppConfig.MAP_WIDTH, ClientAppConfig.MAP_HEIGHT))
            .build()
        screen.addLayer(LayerBuilder.newBuilder().withTileGraphics(mapEntityTileGraphics).build())*/

        // Popluate & do the initial draw from the encounterState
        encounterState = generateNewGameState()
        drawGameState()
    }

    private fun generateNewGameState(): EncounterState {
        val state = EncounterState(SeededRand(100), Constants.MAP_WIDTH, Constants.MAP_HEIGHT)

        val player = Entity(state.getNextEntityId(), "player")
            .addComponent(PlayerComponent())
            .addComponent(DefenderComponent(9999, 50, 50))
            .addComponent(FactionComponent(2))
            .addComponent(CollisionComponent.defaultFighter())
            .addComponent(ActionTimeComponent(100))
            .addComponent(SpeedComponent(100))
            .addComponent(DisplayComponent(DisplayType.PLAYER, false))
        state.placeEntity(player, XYCoordinates(25, 25))

        makeAndPlaceScout(state, 10, 30)
        makeAndPlaceScout(state, 10, 26)
        makeAndPlaceScout(state, 10, 22)
        makeAndPlaceScout(state, 10, 18)
        makeAndPlaceScout(state, 10, 14)
        makeAndPlaceScout(state, 10, 10)
        makeAndPlaceScout(state, 30, 30)
        makeAndPlaceScout(state, 30, 26)
        makeAndPlaceScout(state, 30, 22)
        makeAndPlaceScout(state, 30, 18)
        makeAndPlaceScout(state, 30, 14)
        makeAndPlaceScout(state, 30, 10)

        EncounterRunner.runUntilPlayerReady(state)

        return state
    }

    private fun makeAndPlaceScout(state: EncounterState, x: Int, y: Int) {
        val activatedAi = EnemyScoutAIComponent()
        activatedAi.isActive = true
        val scout = Entity(state.getNextEntityId(), "Scout")
            .addComponent(activatedAi)
            .addComponent(DefenderComponent(0, 10, 10))
            .addComponent(FactionComponent(0))
            .addComponent(CollisionComponent.defaultFighter())
            .addComponent(ActionTimeComponent(75))
            .addComponent(SpeedComponent(75))
            .addComponent(DisplayComponent(DisplayType.ENEMY_SCOUT, false))
        state.placeEntity(scout, XYCoordinates(x, y))
    }

    private fun drawGameState(encounterState: EncounterState? = this.encounterState) {
        ClientDrawer.drawGameState(mapFoWTileGraphics, encounterState)
    }
}
