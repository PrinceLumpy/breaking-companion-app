package com.princelumpy.breakvault.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.princelumpy.breakvault.data.model.move.Move
import com.princelumpy.breakvault.data.model.move.MoveTag
import com.princelumpy.breakvault.data.model.move.MoveTagCrossRef
import com.princelumpy.breakvault.data.model.move.MoveWithTags
import com.princelumpy.breakvault.data.model.move.TagWithMoves
import kotlinx.coroutines.flow.Flow

@Dao
interface MoveDao {
    // --- Methods for Export ---
    @Query("SELECT * FROM moves")
    suspend fun getAllMovesList(): List<Move>

    @Query("SELECT * FROM move_tags")
    suspend fun getAllTagsList(): List<MoveTag>

    @Query("SELECT * FROM move_tag_cross_refs")
    suspend fun getAllMoveTagCrossRefsList(): List<MoveTagCrossRef>

    // --- Methods for Import ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMoves(moves: List<Move>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTags(moveTags: List<MoveTag>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMoveTagCrossRefs(crossRefs: List<MoveTagCrossRef>)

    // --- Methods for Move ---
    @Transaction
    @Query("SELECT * FROM moves")
    fun getAllMovesWithTags(): LiveData<List<MoveWithTags>>

    @Transaction
    @Query("SELECT * FROM moves")
    suspend fun getMovesWithTagsList(): List<MoveWithTags>

    @Transaction
    @Query("SELECT * FROM moves WHERE id = :moveId")
    suspend fun getMoveWithTags(moveId: String): MoveWithTags?

    @Insert
    suspend fun insertMove(move: Move)

    @Update
    suspend fun updateMove(move: Move)

    @Query("UPDATE moves SET name = :name, modifiedAt = :modifiedAt WHERE id = :id")
    suspend fun updateMoveName(id: String, name: String, modifiedAt: Long)

    @Delete
    suspend fun deleteMove(move: Move)

    @Transaction
    suspend fun deleteMoveCompletely(move: Move) {
        unlinkMoveFromAllTags(move.id)
        deleteMove(move)
    }

    // --- Methods for Move Tag ---
    @Transaction
    @Query("SELECT * FROM move_tags ORDER BY name ASC")
    fun getAllTags(): LiveData<List<MoveTag>>

    @Query("SELECT * FROM move_tags ORDER BY name ASC")
    fun getAllTagsAsFlow(): Flow<List<MoveTag>>

    @Query("SELECT * FROM move_tags")
    fun getTagsWithMoves(): LiveData<List<TagWithMoves>>

    @Transaction
    @Query("SELECT * FROM move_tags WHERE id = :tagId")
    suspend fun getTagWithMoves(tagId: String): TagWithMoves?

    @Insert
    suspend fun insertMoveTag(moveTag: MoveTag)

    @Update
    suspend fun updateMoveTag(moveTag: MoveTag)

    @Query("UPDATE move_tags SET name = :name, modifiedAt = :modifiedAt WHERE id = :id")
    suspend fun updateTagName(id: String, name: String, modifiedAt: Long)

    @Delete
    suspend fun deleteTag(moveTag: MoveTag)

    @Transaction
    suspend fun deleteTagCompletely(moveTag: MoveTag) {
        unlinkTagFromAllMoves(moveTag.id)
        deleteTag(moveTag)
    }

    // --- Methods for Cross Ref ---
    @Query("DELETE FROM move_tag_cross_refs WHERE moveId = :moveId")
    suspend fun unlinkMoveFromAllTags(moveId: String)

    @Query("DELETE FROM move_tag_cross_refs WHERE tagId = :tagId")
    suspend fun unlinkTagFromAllMoves(tagId: String)

    @Insert
    suspend fun link(moveTag: MoveTagCrossRef)
}
