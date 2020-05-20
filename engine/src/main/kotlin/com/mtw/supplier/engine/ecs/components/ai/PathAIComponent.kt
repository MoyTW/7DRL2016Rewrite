package com.mtw.supplier.engine.ecs.components.ai

import com.mtw.supplier.engine.encounter.rulebook.Action
import com.mtw.supplier.engine.encounter.rulebook.actions.MoveAction
import com.mtw.supplier.engine.encounter.rulebook.actions.SelfDestructAction
import com.mtw.supplier.engine.encounter.state.EncounterState
import com.mtw.supplier.engine.utils.Path
import kotlinx.serialization.Serializable

@Serializable
class PathAIComponent(
    val path: Path
) : AIComponent() {
    override var _parentId: String? = null
    override var isActive: Boolean = true

    override fun decideNextActions(encounterState: EncounterState): List<Action> {
        return if (!path.atEnd()) {
            val nextPos = path.step()
            listOf(MoveAction(this.getParent(encounterState), nextPos))
        } else {
            listOf(SelfDestructAction(this.getParent(encounterState)))
        }
    }
}