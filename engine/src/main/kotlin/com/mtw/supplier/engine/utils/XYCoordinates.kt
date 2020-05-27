package com.mtw.supplier.engine.utils

import kotlinx.serialization.Serializable
import kotlin.math.sqrt

@Serializable
data class XYCoordinates(
    val x: Int,
    val y: Int
) {
    fun distanceTo(pos: XYCoordinates): Double {
        val dx = this.x.toDouble() - pos.x.toDouble()
        val dy = this.y.toDouble() - pos.y.toDouble()
        return sqrt(dx * dx + dy * dy)
    }
}