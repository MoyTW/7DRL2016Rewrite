package com.mtw.supplier.utils

import kotlinx.serialization.Serializable
import kotlin.math.abs

@Serializable
class Path(val positions: List<XYCoordinates>) {
    var _currentStep: Int = 0

    fun currentPosition(): XYCoordinates {
        return positions[_currentStep]
    }

    fun step(): XYCoordinates {
        this._currentStep += 1
        return currentPosition()
    }

    fun project(turns: Int) {
        // TODO: Confirm this lol
        positions.subList(_currentStep, _currentStep + turns)
    }

    fun atEnd(): Boolean {
        return this._currentStep >= positions.size - 1
    }
}

object PathBuilder {
    /**
     * Defines a straight-line path from (x0, y0) to (x1, y1), inclusive.
     */
    fun linePath(start: XYCoordinates, end: XYCoordinates): Path {
        val acc = mutableListOf<XYCoordinates>()
        acc.add(start)

        val isVertical = start.x - end.x == 0
        val dError: Float? = if (isVertical) {
            null
        } else {
            abs((end.y.toFloat() - start.y.toFloat()) / (end.x.toFloat() - start.x.toFloat()))
        }

        val yErr = if (end.y - start.y > 0) 1 else -1
        val xDiff = if (end.x - start.x > 0) 1 else -1

        var error = 0F
        var cX: Int = start.x
        var cY: Int = start.y
        var steps: Int = 0

        while (!(cX == end.x && cY == end.y) && steps < 100) {
            if (isVertical) {
                cY += yErr
                acc.add(XYCoordinates(cX, cY))
            } else if (error >= 0.5F) {
                cY += yErr
                error -= 1F
                acc.add(XYCoordinates(cX, cY))
            } else {
                cX += xDiff
                error += dError!!
                acc.add(XYCoordinates(cX, cY))
            }
            steps += 1
        }
        return Path(acc)
    }
}