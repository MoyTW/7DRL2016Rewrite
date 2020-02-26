package com.mtw.supplier.ecs.components.ai

import com.mtw.supplier.encounter.rulebook.Action
import com.mtw.supplier.encounter.rulebook.actions.MoveAction
import com.mtw.supplier.encounter.rulebook.actions.SelfDestructAction
import com.mtw.supplier.utils.XYCoordinates
import com.mtw.supplier.encounter.state.EncounterState
import com.mtw.supplier.utils.Path
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
class PathAIComponent(
    val path: Path
) : AIComponent() {
    override var _parentId: Int? = null

    override fun decideNextAction(encounterState: EncounterState): Action {
        return if (!path.atEnd()) {
            val nextPos = path.step()
            MoveAction(this.getParent(encounterState), nextPos)
        } else {
            SelfDestructAction(this.getParent(encounterState))
        }
    }
}