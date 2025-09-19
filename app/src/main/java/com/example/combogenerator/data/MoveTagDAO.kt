package com.example.combogenerator.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MoveTagDao {

    @Insert
    suspend fun addMove(move: Move)

    @Insert
    suspend fun addTag(tag: Tag)

    @Insert
    suspend fun link(moveTag: MoveTagCrossRef)

    // --- Methods for Export --- 
    @Query("SELECT * FROM moves") // Changed from Move
    suspend fun getAllMovesList(): List<Move>

    @Query("SELECT * FROM tags") // Changed from Tag
    suspend fun getAllTagsList(): List<Tag>

    @Query("SELECT * FROM move_tag_cross_refs") // Changed from MoveTagCrossRef
    suspend fun getAllMoveTagCrossRefsList(): List<MoveTagCrossRef>

    // --- Methods for Import --- 
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMoves(moves: List<Move>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTags(tags: List<Tag>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMoveTagCrossRefs(crossRefs: List<MoveTagCrossRef>)

    // --- Existing Methods --- 
    @Transaction
    @Query("SELECT * FROM moves") // Changed from Move
    fun getMovesWithTags(): LiveData<List<MoveWithTags>>

    @Transaction
    @Query("SELECT * FROM moves") // Changed from Move
    suspend fun DEBUG_getAllMovesWithTagsOnce(): List<MoveWithTags>

    @Transaction
    @Query("SELECT * FROM tags") // Changed from Tag
    fun getTagsWithMoves(): LiveData<List<TagWithMoves>>

    @Query("SELECT * FROM tags ORDER BY name ASC") // Changed from Tag
    fun getAllTags(): LiveData<List<Tag>>

    @Transaction
    @Query("SELECT * FROM moves WHERE id = :moveId") // Changed from Move
    suspend fun getMoveWithTagsById(moveId: String): MoveWithTags?

    @Update
    suspend fun updateMove(move: Move)

    @Update
    suspend fun updateTag(tag: Tag)

    @Query("DELETE FROM move_tag_cross_refs WHERE moveId = :moveId") // Changed from MoveTagCrossRef
    suspend fun unlinkMoveFromAllTags(moveId: String)

    @Delete
    suspend fun deleteMove(move: Move)

    @Transaction
    suspend fun deleteMoveCompletely(move: Move) {
        unlinkMoveFromAllTags(move.id)
        deleteMove(move)
    }

    @Query("DELETE FROM move_tag_cross_refs WHERE tagId = :tagId") // Changed from MoveTagCrossRef
    suspend fun unlinkTagFromAllMoves(tagId: String)

    @Delete
    suspend fun deleteTag(tag: Tag)

    @Transaction
    suspend fun deleteTagCompletely(tag: Tag) {
        unlinkTagFromAllMoves(tag.id)
        deleteTag(tag)
    }
}
