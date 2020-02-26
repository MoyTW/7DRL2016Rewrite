package com.mtw.supplier.ecs.components.ai

import com.mtw.supplier.ecs.components.ai.AIComponent
import com.mtw.supplier.encounter.rulebook.Action
import com.mtw.supplier.encounter.rulebook.actions.MoveAction
import com.mtw.supplier.encounter.rulebook.actions.SelfDestructAction
import com.mtw.supplier.encounter.state.EncounterPosition
import com.mtw.supplier.encounter.state.EncounterState
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
class ProjectileAIComponent(
    val path: Queue<EncounterPosition>
) : AIComponent() {
    override var _parentId: Int? = null

    override fun decideNextAction(encounterState: EncounterState): Action {
        if (path.isNotEmpty()) {
            val nextPos = path.poll()
            return MoveAction(this.getParent(encounterState), nextPos)
        } else {
            return SelfDestructAction(this.getParent(encounterState))
        }
    }
}