package com.example.combogenerator.data

import androidx.room.Entity
import androidx.room.Index

@Entity(
    primaryKeys = ["moveId", "tagId"],
    indices = [Index(value = ["tagId"])]
)
data class MoveTagCrossRef(
    val moveId: String,
    val tagId: String
)