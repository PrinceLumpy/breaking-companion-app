package com.example.combogenerator.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MoveTagDao {

    @Insert
    fun addMove(move: Move)

    @Insert
    fun addTag(tag: Tag)

    @Insert
    fun link(moveTag: MoveTagCrossRef)

    @Transaction
    @Query("SELECT * FROM Move")
    fun getMovesWithTags(): LiveData<List<MoveWithTags>>

    @Transaction
    @Query("SELECT * FROM Tag")
    fun getTagsWithMoves(): LiveData<List<TagWithMoves>>

    @Query("DELETE FROM MoveTagCrossRef WHERE moveId = :moveId")
    fun unlinkMoveFromAllTags(moveId: String)

    @Delete
    fun deleteMove(move: Move)

    @Transaction
    fun deleteMoveCompletely(move: Move) {
        unlinkMoveFromAllTags(move.id)
        deleteMove(move)
    }

    @Query("DELETE FROM MoveTagCrossRef WHERE tagId = :tagId")
    fun unlinkTagFromAllMoves(tagId: String)

    @Delete
    fun deleteTag(tag: Tag)

    @Transaction
    fun deleteTagCompletely(tag: Tag) {
        unlinkTagFromAllMoves(tag.id)
        deleteTag(tag)
    }
}
