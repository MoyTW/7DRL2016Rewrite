package com.mtw.supplier.client

import com.mtw.supplier.engine.ecs.Entity
import com.mtw.supplier.engine.ecs.components.*
import com.mtw.supplier.engine.ecs.components.ai.PathAIComponent
import com.mtw.supplier.engine.encounter.state.EncounterState
import com.mtw.supplier.engine.encounter.state.FoVCache
import com.mtw.supplier.engine.utils.XYCoordinates
import org.hexworks.zircon.api.color.ANSITileColor
import org.hexworks.zircon.api.color.TileColor
import org.hexworks.zircon.api.data.Position
import org.hexworks.zircon.api.data.Tile
import org.hexworks.zircon.api.graphics.TileGraphics

object ClientDrawer {

    fun drawGameState(mapFoWTileGraphics: TileGraphics,
                      encounterState: EncounterState?) {
        if (encounterState == null) { return }

        // Draw the map
        mapFoWTileGraphics.clear()
        val playerPos = encounterState.playerEntity().getComponent(EncounterLocationComponent::class).position
        val cameraX = playerPos.x
        val cameraY = playerPos.y

        drawFoWTiles(mapFoWTileGraphics, encounterState, cameraX, cameraY)
    }

    // TODO: Remove CameraX, CameraY
    private fun drawFoWTiles(tileGraphics: TileGraphics, encounterState: EncounterState, cameraX: Int, cameraY: Int) {
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
                val drawTile = when {
                    !tiles.isExplored(x, y) -> { unexploredTile }
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
        return XYCoordinates(pos.x - cameraX + ClientAppConfig.MAP_CENTER.x, pos.y - cameraY + ClientAppConfig.MAP_CENTER.y)
    }

}