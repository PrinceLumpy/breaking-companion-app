package com.example.combogenerator.viewmodel

import com.example.combogenerator.data.Move
import com.example.combogenerator.data.MoveTagDao


class MoveRepository(private val dao: MoveTagDao) {
    val moves = dao.getMovesWithTags()
    fun add(move: Move) = dao.addMove(move)
}