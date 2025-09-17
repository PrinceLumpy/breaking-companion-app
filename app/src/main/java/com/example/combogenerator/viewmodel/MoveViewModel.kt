package com.example.combogenerator.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.combogenerator.data.*

class MoveViewModel(app: Application) : AndroidViewModel(app) {

    private val db = Room.databaseBuilder(
        app,
        AppDB::class.java,
        "moves.db"
    ).build()

    private val dao = db.moveTagDao()

    val movesWithTags = dao.getMovesWithTags()
    val tagsWithMoves = dao.getTagsWithMoves()

    fun addMove(name: String, tags: List<Tag>) = viewModelScope.launch(Dispatchers.IO) {
        val newMove = Move(name = name)
        dao.addMove(newMove)
        tags.forEach { dao.link(MoveTagCrossRef(newMove.id, it.id)) }
    }
}