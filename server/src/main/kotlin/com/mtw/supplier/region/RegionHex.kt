package com.mtw.supplier.region

import kotlinx.serialization.Serializable
import org.hexworks.mixite.core.api.contract.SatelliteData

enum class HexEffects {
    POISONOUS_TO_TOUCH_VEGETATION,
    HYPTERGROWTJ_VEGETATION
}

@Serializable
class RegionHex(
    val vegetationPercentage: Int,
    val elevation: Int,
    val hexEffects: MutableList<HexEffects> = mutableListOf(),
    // These parameters are not used, and are vestigal SatelliteData requirements.
    override var passable: Boolean = true,
    override var opaque: Boolean = false,
    override var movementCost: Double = 0.0
) : SatelliteData