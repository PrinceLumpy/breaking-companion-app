package com.princelumpy.breakvault.data.model.goal

import androidx.room.Embedded
import androidx.room.Relation

data class GoalWithStages(
    @Embedded val goal: Goal,
    @Relation(
        parentColumn = "id",
        entityColumn = "goalId",
    )
    val stages: List<GoalStage>
)
