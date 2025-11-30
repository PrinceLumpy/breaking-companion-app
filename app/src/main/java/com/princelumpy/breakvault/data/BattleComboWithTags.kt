package com.princelumpy.breakvault.data

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class BattleComboWithTags(
    @Embedded val battleCombo: BattleCombo,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = BattleComboTagCrossRef::class,
            parentColumn = "battleComboId",
            entityColumn = "battleTagId"
        )
    )
    val tags: List<BattleTag>
)
