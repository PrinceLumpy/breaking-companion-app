package com.princelumpy.breakvault.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.princelumpy.breakvault.data.model.savedcombo.SavedCombo

@Dao
interface SavedComboDao {
    // --- 1. Methods for Import/Export ---
    @Query("SELECT * FROM saved_combos")
    suspend fun getAllRawSavedCombos(): List<SavedCombo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSavedCombos(savedCombos: List<SavedCombo>)

    // --- 2. Methods for UI Data ---
    @Query("SELECT * FROM saved_combos ORDER BY createdAt DESC")
    fun getAllSavedCombos(): LiveData<List<SavedCombo>>

    @Query("SELECT * FROM saved_combos WHERE id = :savedComboId")
    suspend fun getSavedComboById(savedComboId: String): SavedCombo?

    // --- 3. Methods for CRUD Operations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedCombo(savedCombo: SavedCombo)

    @Update
    suspend fun updateSavedCombo(savedCombo: SavedCombo)

    @Query("UPDATE saved_combos SET name = :newName, moves = :newMoves, modifiedAt = :modifiedAt WHERE id = :comboId")
    suspend fun updateSavedCombo(comboId: String, newName: String, newMoves: List<String>, modifiedAt: Long)

    @Query("UPDATE saved_combos SET name = :newName WHERE id = :savedComboId")
    suspend fun updateSavedComboName(savedComboId: String, newName: String)

    @Query("DELETE FROM saved_combos WHERE id = :savedComboId")
    suspend fun deleteSavedComboById(savedComboId: String)
}
