package com.princelumpy.breakvault.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.AppDB
import com.princelumpy.breakvault.data.model.move.Move
import com.princelumpy.breakvault.data.model.move.MoveTag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class GenerationMode {
    Random, Structured
}

data class ComboGeneratorUiState(
    val currentMode: GenerationMode = GenerationMode.Random,
    val allTags: List<MoveTag> = emptyList(),
    val selectedTags: Set<MoveTag> = emptySet(),
    val generatedComboText: String = "",
    val currentGeneratedMoves: List<Move> = emptyList(),
    val selectedLength: Int? = null,
    val allowRepeats: Boolean = false,
    val structuredMoveTagSequence: List<MoveTag> = emptyList(),
    val showLengthWarningDialog: Boolean = false,
    val warningDialogMessage: String = "",
    val snackbarMessage: String? = null
)

class ComboGeneratorViewModel : ViewModel() {

    private val moveDao by lazy { AppDB.getDatabase(application).moveDao() }
    private val savedComboDao by lazy { AppDB.getDatabase(application).savedComboDao() }

    open val uiState: StateFlow<ComboGeneratorUiState> = moveDao.getAllTagsAsFlow()
        .map { tags -> ComboGeneratorUiState(allTags = tags) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ComboGeneratorUiState()
        )

    private val _uiState = MutableStateFlow(ComboGeneratorUiState())


    open fun onModeChange(mode: GenerationMode) {
        _uiState.update { it.copy(currentMode = mode) }
    }

    open fun onTagsChange(tags: Set<MoveTag>) {
        _uiState.update { it.copy(selectedGeneratorMoveTags = tags) }
    }

    open fun onLengthChange(length: Int?) {
        _uiState.update { it.copy(selectedLength = length) }
    }

    open fun onDropdownExpand(expanded: Boolean) {
        _uiState.update { it.copy(lengthDropdownExpanded = expanded) }
    }

    open fun onAllowRepeatsChange(allow: Boolean) {
        _uiState.update { it.copy(allowRepeats = allow) }
    }

    open fun onSequenceChange(sequence: List<MoveTag>) {
        _uiState.update { it.copy(structuredMoveTagSequence = sequence) }
    }

    open fun onSnackbarMessageShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    open fun onDismissLengthWarning() {
        _uiState.update { it.copy(showLengthWarningDialog = false) }
    }

    open fun generateCombo() {
        viewModelScope.launch {
            val state = _uiState.value
            val comboMoves = when (state.currentMode) {
                GenerationMode.Random -> {
                    val tagsToUse =
                        state.selectedGeneratorMoveTags.ifEmpty { state.allTags.toSet() }
                    if (tagsToUse.isNotEmpty()) {
                        generateComboFromTags(
                            tagsToUse,
                            state.selectedLength,
                            state.allowRepeats
                        )
                    } else {
                        _uiState.update { it.copy(generatedComboText = "No tags available to generate a combo.") }
                        emptyList()
                    }
                }

                GenerationMode.Structured -> {
                    if (state.structuredMoveTagSequence.isNotEmpty()) {
                        generateStructuredCombo(state.structuredMoveTagSequence)
                    } else {
                        _uiState.update { it.copy(generatedComboText = "Please select at least one tag to generate a combo.") }
                        emptyList()
                    }
                }
            }

            if (comboMoves.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        currentGeneratedMoves = comboMoves,
                        generatedComboText = comboMoves.joinToString(separator = "  ->  ") { move -> move.name }
                    )
                }

                if (!state.allowRepeats && state.currentMode == GenerationMode.Random && state.selectedLength != null && state.selectedLength!! > comboMoves.size) {
                    _uiState.update {
                        it.copy(
                            showLengthWarningDialog = true,
                            warningDialogMessage = "Only ${comboMoves.size} moves are available with the selected tags. A combo of ${comboMoves.size} moves has been generated."
                        )
                    }
                }
            } else if ((state.currentMode == GenerationMode.Random && state.allTags.isNotEmpty()) || (state.currentMode == GenerationMode.Structured && state.structuredMoveTagSequence.isNotEmpty())) {
                _uiState.update {
                    it.copy(
                        currentGeneratedMoves = emptyList(),
                        generatedComboText = "No moves found matching the selected tags. Add some moves."
                    )
                }
            }
        }
    }

    open fun saveCombo() {
        val state = _uiState.value
        if (state.currentGeneratedMoves.isNotEmpty()) {
            viewModelScope.launch {
                savedComboDao.insertSavedCombo(
                    com.princelumpy.breakvault.data.model.savedcombo.SavedCombo(
                        name = "",
                        moves = state.currentGeneratedMoves.map { it.name }
                    )
                )
                _uiState.update { it.copy(snackbarMessage = "Combo saved!") }
            }
        }
    }

    private suspend fun generateComboFromTags(
        tags: Set<MoveTag>,
        length: Int?,
        allowRepeats: Boolean
    ): List<Move> {
        val moves = tags.flatMap { moveDao.getTagWithMoves(it.id)?.moves ?: emptyList() }.distinct()
        if (moves.isEmpty()) return emptyList()

        val targetLength = when {
            length == null -> (1..6).random()
            length < 1 -> 1
            length > 6 -> 6
            else -> length
        }

        return if (allowRepeats) {
            List(targetLength) { moves.random() }
        } else {
            moves.shuffled().take(targetLength)
        }
    }

    private suspend fun generateStructuredCombo(sequence: List<MoveTag>): List<Move> {
        return sequence.mapNotNull { moveDao.getTagWithMoves(it.id)?.moves?.random() }
    }
}