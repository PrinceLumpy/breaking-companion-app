package com.example.combogenerator.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable // Added import

@Serializable // Added annotation
@Entity(tableName = "tags") // Existing Room annotation
data class Tag(
    @PrimaryKey val id: String = "",
    val name: String = ""
)
