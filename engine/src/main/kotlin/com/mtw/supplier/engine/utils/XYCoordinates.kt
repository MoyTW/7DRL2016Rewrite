package com.mtw.supplier.engine.utils

import kotlinx.serialization.Serializable

@Serializable
data class XYCoordinates(
    val x: Int,
    val y: Int
)