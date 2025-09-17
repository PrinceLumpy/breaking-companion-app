package com.example.combogenerator.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Move(
    @PrimaryKey val id: String = "",
    val name: String = ""
)
