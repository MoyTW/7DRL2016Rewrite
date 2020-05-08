package com.mtw.supplier.client

import com.mtw.supplier.engine.utils.XYCoordinates

object ClientAppConfig {
    val CLIENT_WIDTH: Int = 50
    val CLIENT_HEIGHT: Int = 50
    val MAP_WIDTH: Int = 50
    val MAP_HEIGHT: Int = 50
    // TODO: Game log display
    // val LOG_WIDTH: Int = GAME_WIDTH
    // val LOG_HEIGHT: Int = GAME_HEIGHT - MAP_HEIGHT
    val MAP_CENTER = XYCoordinates(MAP_WIDTH / 2, MAP_HEIGHT / 2)
}