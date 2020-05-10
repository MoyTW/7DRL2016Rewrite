package com.mtw.supplier.engine.utils

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.random.Random

@Serializable
class SeededRand(private var seed: Int) {
    @Transient private var random: Random? = null

    fun reseed() {
        this.seed = this.random!!.nextInt()
        this.random = Random(seed)
    }

    fun getRandom(): Random {
        if (this.random == null) {
            this.random = Random(seed)
        }
        return this.random!!
    }
}