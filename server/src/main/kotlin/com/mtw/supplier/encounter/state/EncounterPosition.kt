package com.mtw.supplier.encounter.state

import kotlinx.serialization.Serializable

@Serializable
data class EncounterPosition(
    val x: Int,
    val y: Int
)