package com.princelumpy.breakvault.data.model.move

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TagWithMoves(
    @Embedded val moveTag: MoveTag,
    @Relation(
        parentColumn = "id", // Primary key of the MoveTag entity
        entityColumn = "id", // Primary key of the Move entity
        associateBy = Junction(
            value = MoveTagCrossRef::class,
            parentColumn = "tagId",   // Foreign key in MoveTagCrossRef linking to MoveTag
            entityColumn = "moveId"  // Foreign key in MoveTagCrossRef linking to Move
        )
    )
    val moves: List<Move>
)
