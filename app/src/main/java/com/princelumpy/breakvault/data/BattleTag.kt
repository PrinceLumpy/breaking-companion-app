package com.princelumpy.breakvault.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
@Entity(tableName = "battle_tags")
data class BattleTag(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String
)
