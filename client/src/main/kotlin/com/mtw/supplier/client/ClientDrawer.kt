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
                      mapProjectilePathTileGraphics: TileGraphics,
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
        mapProjectilePathTileGraphics.clear()
        mapEntityTileGraphics.clear()
        val playerPos = encounterState.playerEntity().getComponent(EncounterLocationComponent::class).position
        val cameraX = playerPos.x
        val cameraY = playerPos.y

        drawFoWTiles(mapFoWTileGraphics, encounterState, cameraX, cameraY)
        drawProjectilePaths(mapProjectilePathTileGraphics, encounterState, cameraX, cameraY)
        drawDisplayEntities(mapEntityTileGraphics, encounterState, cameraX, cameraY)
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

    private fun drawProjectilePaths(tileGraphics: TileGraphics, encounterState: EncounterState, cameraX: Int, cameraY: Int) {
        val fovCache = encounterState.fovCache!!
        val markedPositions = mutableSetOf<XYCoordinates>()

        encounterState.entities()
            .filter { it.hasComponent(EncounterLocationComponent::class) && it.hasComponent(PathAIComponent::class) }
            .map {
                val path = it.getComponent(PathAIComponent::class).path
                val projectileSpeed = it.getComponent(SpeedComponent::class).speed
                val projectileTicks = it.getComponent(ActionTimeComponent::class).ticksUntilTurn

                val playerSpeed = encounterState.playerEntity().getComponent(SpeedComponent::class).speed
                val playerTicks = encounterState.playerEntity().getComponent(ActionTimeComponent::class).ticksUntilTurn
                if (projectileTicks <= playerTicks) {
                    val turns = ((playerTicks - projectileTicks) + playerSpeed) / projectileSpeed
                    val stops = path.project(turns)
                    if (stops.size > 1) {
                        for (stop in stops.subList(1, stops.size)) {
                            if (fovCache.isInFoV(stop)) {
                                markedPositions.add(stop)
                            }
                        }
                    }
                }
            }

        markedPositions.forEach {
            // TODO: Consolidate all the Tile.newBuilder()
            val pathTile = Tile.newBuilder()
                .withForegroundColor(TileColor.transparent())
                .withBackgroundColor(TileColor.create(217, 112, 213))
                .build()
            draw(tileGraphics, pathTile, it, cameraX, cameraY)
        }
    }

    private fun drawDisplayEntities(tileGraphics: TileGraphics, encounterState: EncounterState, cameraX: Int, cameraY: Int) {
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
}