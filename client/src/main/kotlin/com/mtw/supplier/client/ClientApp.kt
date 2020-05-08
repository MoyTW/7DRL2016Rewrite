package com.mtw.supplier.client

import org.hexworks.zircon.api.CP437TilesetResources
import org.hexworks.zircon.api.ColorThemes
import org.hexworks.zircon.api.SwingApplications
import org.hexworks.zircon.api.application.AppConfig
import org.hexworks.zircon.api.color.TileColor
import org.hexworks.zircon.api.data.Position
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.extensions.toScreen
import org.hexworks.zircon.api.screen.Screen
import org.hexworks.zircon.api.uievent.KeyboardEvent
import org.hexworks.zircon.api.uievent.KeyboardEventType
import org.hexworks.zircon.api.uievent.UIEventPhase
import org.hexworks.zircon.api.uievent.UIEventResponse

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        ClientApp()
    }
}

class ClientApp {
    private val screen: Screen

    init {
        // Create Zircon app
        val tileGrid = SwingApplications.startTileGrid(
            AppConfig.newBuilder()
                .withSize(ClientAppConfig.CLIENT_WIDTH, ClientAppConfig.CLIENT_HEIGHT)
                .withDefaultTileset(CP437TilesetResources.rexPaint16x16())
                .build())
        screen = tileGrid.toScreen()
        screen.display()
        screen.theme = (ColorThemes.arc());

        // Add input handler
        tileGrid.processKeyboardEvents(KeyboardEventType.KEY_PRESSED) { keyboardEvent: KeyboardEvent, uiEventPhase: UIEventPhase ->
            handler()
            UIEventResponse.pass()
        }
    }

    /**
     * run with ./gradlew client:run
     *
     * I would expect when running this that it shows X, then it shows Y, then it shows Z, but it instead waits until
     * 2 seconds have passed and then shows Z. I assume this is because it doesn't actually flush the draw buffer while
     * the input is being handled, so it saves all those changes until the end. Is that a correct mental model of how
     * the input handling works?
     */
    private fun handler() {
        screen.clear()
        val tileX = Tile.newBuilder()
            .withCharacter('X')
            .withForegroundColor(TileColor.create(255, 255, 255))
            .withBackgroundColor(TileColor.create(217, 112, 213))
            .build()
        screen.draw(tileX, Position.create(5, 5))

        Thread.sleep(1000)

        screen.clear()
        val tileY = Tile.newBuilder()
            .withCharacter('Y')
            .withForegroundColor(TileColor.create(255, 255, 255))
            .withBackgroundColor(TileColor.create(217, 112, 213))
            .build()
        screen.draw(tileY, Position.create(5, 5))

        Thread.sleep(1000)

        screen.clear()
        val tileZ = Tile.newBuilder()
            .withCharacter('Z')
            .withForegroundColor(TileColor.create(255, 255, 255))
            .withBackgroundColor(TileColor.create(217, 112, 213))
            .build()
        screen.draw(tileZ, Position.create(5, 5))
    }
}
