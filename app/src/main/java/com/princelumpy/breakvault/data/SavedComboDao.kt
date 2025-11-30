package com.princelumpy.breakvault.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface SavedComboDao {

    @Query("SELECT * FROM saved_combos")
    suspend fun getAllSavedCombosList(): List<SavedCombo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSavedCombos(savedCombos: List<SavedCombo>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedCombo(savedCombo: SavedCombo)

    @Query("SELECT * FROM saved_combos ORDER BY createdAt DESC")
    fun getAllSavedCombos(): LiveData<List<SavedCombo>>

    @Query("DELETE FROM saved_combos WHERE id = :savedComboId")
    suspend fun deleteSavedComboById(savedComboId: String)

    @Query("SELECT * FROM saved_combos WHERE id = :savedComboId")
    suspend fun getSavedComboById(savedComboId: String): SavedCombo?

    @Update
    suspend fun updateSavedCombo(savedCombo: SavedCombo)

    @Query("UPDATE saved_combos SET name = :newName WHERE id = :savedComboId")
    suspend fun updateSavedComboName(savedComboId: String, newName: String)
}
