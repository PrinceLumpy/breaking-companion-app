package com.example.combogenerator.data

import androidx.room.Entity
import androidx.room.Index
import kotlinx.serialization.Serializable // Added import

@Serializable // Added annotation
@Entity(
    primaryKeys = ["moveId", "tagId"],
    indices = [Index(value = ["tagId"])],
    tableName = "move_tag_cross_refs" // Explicit table name often good practice
)
data class MoveTagCrossRef(
    val moveId: String,
    val tagId: String
)
