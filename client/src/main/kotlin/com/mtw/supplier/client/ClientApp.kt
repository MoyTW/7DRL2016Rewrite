package com.mtw.supplier.client

import org.hexworks.cobalt.events.api.Event
import org.hexworks.cobalt.events.api.KeepSubscription
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
import org.hexworks.zircon.internal.Zircon
import kotlin.concurrent.thread

object Main {
    @ExperimentalStdlibApi
    @JvmStatic
    fun main(args: Array<String>) {
        ClientApp()
    }
}

data class CharChangeEvent(val char: Char, val emittedAt: Long, override val emitter: Any): Event

@ExperimentalStdlibApi
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

        val unthreadsafeList: MutableList<CharChangeEvent> = mutableListOf()

        // Add input handler
        tileGrid.processKeyboardEvents(KeyboardEventType.KEY_PRESSED) { keyboardEvent: KeyboardEvent, uiEventPhase: UIEventPhase ->
            handler()
            UIEventResponse.pass()
        }

        Zircon.eventBus.subscribeTo<CharChangeEvent>(key = CharChangeEvent::class.simpleName!!) {
            println("Received ${it.char} emitted at ${it.emittedAt} at ${System.currentTimeMillis()}")
            unthreadsafeList.add(it)
            println("Completed ${it.char} emitted at ${it.emittedAt} at ${System.currentTimeMillis()}")
            KeepSubscription
        }

        // This thread will run...FOREVER!
        thread(start = true) {
            while(true) {
                println("game speed thread")
                if (unthreadsafeList.isNotEmpty()) {
                   val event = unthreadsafeList.removeFirst()
                    screen.clear()
                    val tileX = Tile.newBuilder()
                        .withCharacter(event.char)
                        .withForegroundColor(TileColor.create(255, 255, 255))
                        .withBackgroundColor(TileColor.create(217, 112, 213))
                        .build()
                    screen.draw(tileX, Position.create(5, 5))
                }

                Thread.sleep(1000)
            }
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
        val start = System.currentTimeMillis()
        println("Started recording $start")
        Zircon.eventBus.publish(CharChangeEvent('X', System.currentTimeMillis(), this))
        println("Published X")

        //Thread.sleep(2000)

        Zircon.eventBus.publish(CharChangeEvent('Y', System.currentTimeMillis(),this))
        println("Published Y")

        //Thread.sleep(2000)

        Zircon.eventBus.publish(CharChangeEvent('Z', System.currentTimeMillis(),this))
        println("Published Z")
    }
}
