package com.example.combogenerator.data.transfer

import com.example.combogenerator.data.Move
import com.example.combogenerator.data.Tag
import com.example.combogenerator.data.MoveTagCrossRef
import com.example.combogenerator.data.SavedCombo
import com.example.combogenerator.data.SavedComboMoveLink
import kotlinx.serialization.Serializable // <--- ENSURED IMPORT

@Serializable // <--- ENSURED ANNOTATION
data class AppDataExport(
    val moves: List<Move>,
    val tags: List<Tag>,
    val moveTagCrossRefs: List<MoveTagCrossRef>,
    val savedCombos: List<SavedCombo>,
    val savedComboMoveLinks: List<SavedComboMoveLink>
)
