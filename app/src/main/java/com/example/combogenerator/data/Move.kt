package com.example.combogenerator.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable // Added import

@Serializable // Added annotation
@Entity(tableName = "moves") // Existing Room annotation
data class Move(
    @PrimaryKey val id: String = "",
    val name: String = ""
)
