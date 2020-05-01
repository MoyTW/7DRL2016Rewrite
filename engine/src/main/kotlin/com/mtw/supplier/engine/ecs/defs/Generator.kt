package com.mtw.supplier.engine.ecs.defs

import kotlinx.serialization.Serializable

@Serializable
abstract class Generator {
    abstract fun generate(): Any
}

@Serializable
class FixedIntegerGenerator(val value: Int): Generator() {
    override fun generate(): Int {
        return value
    }
}
