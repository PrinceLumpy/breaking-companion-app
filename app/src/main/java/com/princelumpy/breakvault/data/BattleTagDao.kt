package com.princelumpy.breakvault.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface BattleTagDao {
    @Query("SELECT * FROM battle_tags ORDER BY name ASC")
    fun getAllBattleTags(): LiveData<List<BattleTag>>

    @Query("SELECT * FROM battle_tags")
    suspend fun getAllBattleTagsList(): List<BattleTag>

    @Query("SELECT * FROM battle_tags WHERE id = :id")
    suspend fun getBattleTagById(id: String): BattleTag?

    @Query("SELECT * FROM battle_tags WHERE name = :name")
    suspend fun getBattleTagByName(name: String): BattleTag?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBattleTag(tag: BattleTag)

    @Update
    suspend fun updateBattleTag(tag: BattleTag)

    @Delete
    suspend fun deleteBattleTag(tag: BattleTag)
}
