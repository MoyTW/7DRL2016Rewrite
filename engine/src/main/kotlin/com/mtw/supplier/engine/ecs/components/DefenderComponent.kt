package com.mtw.supplier.engine.ecs.components

import com.mtw.supplier.engine.ecs.Component
import kotlinx.serialization.Serializable

@Serializable
class DefenderComponent(
    var defense: Int,
    var maxHp: Int,
    var currentHp: Int
): Component() {
    override var _parentId: String? = null

    fun removeHp(hp: Int) {
        this.currentHp -= hp
    }

    fun healHp(hp: Int) {
        this.currentHp += hp
        if (this.currentHp > maxHp) {
            this.currentHp = this.maxHp
        }
    }
}