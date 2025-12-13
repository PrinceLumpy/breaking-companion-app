package com.princelumpy.breakvault.data.transfer

import com.princelumpy.breakvault.data.model.battlecombo.BattleCombo
import com.princelumpy.breakvault.data.model.battlecombo.BattleComboTagCrossRef
import com.princelumpy.breakvault.data.model.battlecombo.BattleTag
import com.princelumpy.breakvault.data.model.goal.Goal
import com.princelumpy.breakvault.data.model.goal.GoalStage
import com.princelumpy.breakvault.data.model.move.Move
import com.princelumpy.breakvault.data.model.move.MoveTag
import com.princelumpy.breakvault.data.model.move.MoveTagCrossRef
import com.princelumpy.breakvault.data.model.savedcombo.SavedCombo
import kotlinx.serialization.Serializable

@Serializable
data class AppDataExport(
    val moves: List<Move>,
    val moveTags: List<MoveTag>,
    val moveTagCrossRefs: List<MoveTagCrossRef>,
    val savedCombos: List<SavedCombo>,
    val battleCombos: List<BattleCombo> = emptyList(),
    val battleTags: List<BattleTag> = emptyList(),
    val battleComboTagCrossRefs: List<BattleComboTagCrossRef> = emptyList(),
    val goals: List<Goal> = emptyList(),
    val goalStages: List<GoalStage> = emptyList()
)
