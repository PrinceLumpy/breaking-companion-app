package com.princelumpy.breakvault.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.AppDB
import com.princelumpy.breakvault.data.Move
import com.princelumpy.breakvault.data.MoveTagCrossRef
import com.princelumpy.breakvault.data.MoveWithTags
import com.princelumpy.breakvault.data.SavedCombo
import com.princelumpy.breakvault.data.Tag
import com.princelumpy.breakvault.data.transfer.AppDataExport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.min

interface IMoveViewModel {
    val movesWithTags: LiveData<List<MoveWithTags>>
    val allTags: LiveData<List<Tag>>
    val savedCombos: LiveData<List<SavedCombo>>
    val allMoves: LiveData<List<Move>>

    fun addMove(moveName: String, selectedTags: List<Tag>)
    fun addTag(tagName: String)
    suspend fun getMoveForEditing(moveId: String): MoveWithTags?
    fun updateMoveAndTags(moveId: String, newName: String, newSelectedTags: List<Tag>)
    fun deleteMove(move: Move)
    fun updateTag(tagId: String, newName: String)
    fun deleteTag(tag: Tag)
    fun generateComboFromTags(selectedTags: Set<Tag>, length: Int? = null, allowRepeats: Boolean = false): List<Move>
    fun generateStructuredCombo(tagSequence: List<Tag>): List<Move>
    suspend fun getMovesForTag(tagId: String): List<Move>
    fun getFlashcardMove(excludedTags: Set<Tag>): Move?
    fun saveCombo(comboName: String, moves: List<String>)
    fun deleteSavedCombo(savedComboId: String)
    fun updateSavedComboName(savedComboId: String, newName: String)
    fun resetDatabase()

    suspend fun getSavedComboForEditing(comboId: String): SavedCombo?
    fun updateSavedCombo(comboId: String, newName: String, newMoves: List<String>)

    suspend fun getAppDataForExport(): AppDataExport?
    suspend fun importAppData(appData: AppDataExport): Boolean
}

class MoveViewModel(application: Application) : AndroidViewModel(application), IMoveViewModel {

    private val db = AppDB.getDatabase(application)
    private val moveTagDao = db.moveTagDao()
    private val savedComboDao = db.savedComboDao()
    private val battleComboDao = db.battleComboDao()
    private val battleTagDao = db.battleTagDao()

    override val movesWithTags: LiveData<List<MoveWithTags>> = moveTagDao.getMovesWithTags()
    override val allTags: LiveData<List<Tag>> = moveTagDao.getAllTags()
    override val savedCombos: LiveData<List<SavedCombo>> =
        savedComboDao.getAllSavedCombos()

    override val allMoves: LiveData<List<Move>> = movesWithTags.map {
        it.map { mwt -> mwt.move }
    }

    private val movesWithTagsObserver = Observer<List<MoveWithTags>> { data ->
        if (data.isEmpty()) {
            Log.d("MoveViewModel_Debug", "movesWithTags LiveData is EMPTY or NULL")
        } else {
            Log.d("MoveViewModel_Debug", "movesWithTags LiveData has ${data.size} items:")
        }
    }

    init {
        movesWithTags.observeForever(movesWithTagsObserver)
    }

    override fun getFlashcardMove(excludedTags: Set<Tag>): Move? {
        val currentMoves = movesWithTags.value ?: return null
        if (excludedTags.isEmpty()) return null

        val excludedTagIds = excludedTags.map { it.id }.toSet()
        val eligibleMoves = currentMoves
            .filter { moveWithTags -> moveWithTags.tags.any { it.id in excludedTagIds } }
            .map { it.move }

        return if (eligibleMoves.isNotEmpty()) eligibleMoves.random() else null
    }

    override suspend fun getMovesForTag(tagId: String): List<Move> = withContext(Dispatchers.IO) {
        moveTagDao.getTagWithMoves(tagId)?.moves ?: emptyList()
    }

    override fun generateStructuredCombo(tagSequence: List<Tag>): List<Move> {
        val currentMoves = movesWithTags.value ?: return emptyList()
        if (tagSequence.isEmpty()) return emptyList()

        val generatedMoves = mutableListOf<Move>()
        tagSequence.forEach { tag ->
            val movesForTag = currentMoves.filter { it.tags.contains(tag) }.map { it.move }
            if (movesForTag.isNotEmpty()) {
                generatedMoves.add(movesForTag.random())
            }
        }
        return generatedMoves
    }

    override suspend fun getSavedComboForEditing(comboId: String): SavedCombo? = withContext(Dispatchers.IO) {
        savedComboDao.getSavedComboById(comboId)
    }

    override fun updateSavedCombo(comboId: String, newName: String, newMoves: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentCombo = savedComboDao.getSavedComboById(comboId)
                if (currentCombo != null) {
                    val updatedCombo = currentCombo.copy(name = newName, moves = newMoves)
                    savedComboDao.updateSavedCombo(updatedCombo)
                    Log.i("MoveViewModel", "updateSavedCombo successful for id: $comboId")
                }
            } catch (e: Exception) {
                Log.e("MoveViewModel", "Error in updateSavedCombo for id: $comboId", e)
            }
        }
    }

    override fun addMove(moveName: String, selectedTags: List<Tag>) {
        viewModelScope.launch(Dispatchers.IO) {
            val newMoveId = UUID.randomUUID().toString()
            val move = Move(id = newMoveId, name = moveName)
            moveTagDao.addMove(move)
            selectedTags.forEach { tag ->
                moveTagDao.link(MoveTagCrossRef(moveId = newMoveId, tagId = tag.id))
            }
        }
    }

    override fun addTag(tagName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            moveTagDao.addTag(Tag(id = UUID.randomUUID().toString(), name = tagName))
        }
    }

    override suspend fun getMoveForEditing(moveId: String): MoveWithTags? = withContext(Dispatchers.IO) {
        moveTagDao.getMoveWithTagsById(moveId)
    }

    override fun updateMoveAndTags(moveId: String, newName: String, newSelectedTags: List<Tag>) {
        viewModelScope.launch(Dispatchers.IO) {
            moveTagDao.updateMove(Move(id = moveId, name = newName))
            moveTagDao.unlinkMoveFromAllTags(moveId)
            newSelectedTags.forEach { tag ->
                moveTagDao.link(MoveTagCrossRef(moveId = moveId, tagId = tag.id))
            }
        }
    }

    override fun deleteMove(move: Move) {
        viewModelScope.launch(Dispatchers.IO) {
            moveTagDao.deleteMoveCompletely(move)
        }
    }

    override fun updateTag(tagId: String, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (newName.isNotBlank()) {
                moveTagDao.updateTag(Tag(id = tagId, name = newName))
            }
        }
    }

    override fun deleteTag(tag: Tag) {
        viewModelScope.launch(Dispatchers.IO) {
            moveTagDao.deleteTagCompletely(tag)
        }
    }

    override fun generateComboFromTags(selectedTags: Set<Tag>, length: Int?, allowRepeats: Boolean): List<Move> {
        val currentMovesWithTags = movesWithTags.value ?: return emptyList()
        if (selectedTags.isEmpty()) return emptyList()

        val selectedTagIds = selectedTags.map { it.id }.toSet()
        // Only keep unique moves for the pool, unless allowRepeats is handled in selection
        val matchingMoves = currentMovesWithTags
            .filter { moveWithTags -> moveWithTags.tags.any { it.id in selectedTagIds } }
            .map { it.move }
            .distinct() // Ensure pool is unique moves first

        if (matchingMoves.isEmpty()) return emptyList()

        // Logic: Length between 1 and 6.
        val targetLength = when {
            length == null -> (1..6).random()
            length < 1 -> 1
            length > 6 -> 6
            else -> length
        }

        return if (allowRepeats) {
            // If repeats allowed, pick random move N times
            List(targetLength) { matchingMoves.random() }
        } else {
            // If no repeats, we are limited by the pool size
            val actualLength = min(targetLength, matchingMoves.size)
            matchingMoves.shuffled().take(actualLength)
        }
    }

    override fun saveCombo(comboName: String, moves: List<String>) {
        val finalComboName = comboName.ifBlank {
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                .let { "Combo $it" }
        }
        viewModelScope.launch(Dispatchers.IO) {
            val newSavedCombo = SavedCombo(name = finalComboName, moves = moves)
            savedComboDao.insertSavedCombo(newSavedCombo)
        }
    }

    override fun deleteSavedCombo(savedComboId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            savedComboDao.deleteSavedComboById(savedComboId)
        }
    }

    override fun updateSavedComboName(savedComboId: String, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            savedComboDao.updateSavedComboName(savedComboId, newName)
        }
    }

    override fun resetDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            db.clearAllTables()
            AppDB.prepopulateExampleData(db)
        }
    }

    override suspend fun getAppDataForExport(): AppDataExport? = withContext(Dispatchers.IO) {
        // Use the new method to fetch all battle combos as list
        val battleCombosWithTags = battleComboDao.getAllBattleCombosList()
        val battleCombosOnly = battleCombosWithTags.map { it.battleCombo }
        val battleTags = battleTagDao.getAllBattleTagsList()
        val battleComboTagCrossRefs = battleComboDao.getAllBattleComboTagCrossRefs()

        AppDataExport(
            moves = moveTagDao.getAllMovesList(),
            tags = moveTagDao.getAllTagsList(),
            moveTagCrossRefs = moveTagDao.getAllMoveTagCrossRefsList(),
            savedCombos = savedComboDao.getAllSavedCombosList(),
            battleCombos = battleCombosOnly,
            battleTags = battleTags,
            battleComboTagCrossRefs = battleComboTagCrossRefs
        )
    }

    override suspend fun importAppData(appData: AppDataExport): Boolean = withContext(Dispatchers.IO) {
        try {
            db.clearAllTables()
            moveTagDao.insertAllMoves(appData.moves)
            moveTagDao.insertAllTags(appData.tags)
            moveTagDao.insertAllMoveTagCrossRefs(appData.moveTagCrossRefs)
            savedComboDao.insertAllSavedCombos(appData.savedCombos)
            
            // Import Battle Data
            appData.battleCombos.forEach { battleComboDao.insertBattleCombo(it) }
            appData.battleTags.forEach { battleTagDao.insertBattleTag(it) }
            appData.battleComboTagCrossRefs.forEach { battleComboDao.insertBattleComboTagCrossRef(it) }

            AppDB.prepopulateExampleData(db)
            true
        } catch (e: Exception) {
            Log.e("MoveViewModel", "Import failed", e)
            false
        }
    }

    override fun onCleared() {
        super.onCleared()
        movesWithTags.removeObserver(movesWithTagsObserver)
    }
}

class FakeMoveViewModel : IMoveViewModel {
    private val initialFakeMovesWithTags = listOf(
        MoveWithTags(Move("m1", "6-Step"), listOf(Tag("t1", "Footwork"))),
        MoveWithTags(Move("m2", "Toprock Basic"), listOf(Tag("t2", "Toprock")))
    )
    private val initialFakeTags = listOf(Tag("t1", "Footwork"), Tag("t2", "Toprock"), Tag("t3", "Freeze"))
    private val _movesWithTags = MutableLiveData(initialFakeMovesWithTags)
    override val movesWithTags: LiveData<List<MoveWithTags>> = _movesWithTags

    private val _allTags = MutableLiveData(initialFakeTags)
    override val allTags: LiveData<List<Tag>> = _allTags

    override val allMoves: LiveData<List<Move>> = _movesWithTags.map { it.map { mwt -> mwt.move } }
    override val savedCombos: LiveData<List<SavedCombo>> = MutableLiveData(emptyList())

    override fun addMove(moveName: String, selectedTags: List<Tag>) {}
    override fun addTag(tagName: String) {}
    override suspend fun getMoveForEditing(moveId: String): MoveWithTags? = null
    override suspend fun getMovesForTag(tagId: String): List<Move> {
        return movesWithTags.value?.find { it.tags.any { it.id == tagId } }?.let { listOf(it.move) } ?: emptyList()
    }
    override fun getFlashcardMove(excludedTags: Set<Tag>): Move? {
        val excludedTagIds = excludedTags.map { it.id }.toSet()
        return initialFakeMovesWithTags.filter { mwt -> mwt.tags.any { it.id in excludedTagIds } }.map { it.move }.randomOrNull()
    }
    override fun updateMoveAndTags(moveId: String, newName: String, newSelectedTags: List<Tag>) {}
    override fun deleteMove(move: Move) {}
    override fun updateTag(tagId: String, newName: String) {}
    override fun deleteTag(tag: Tag) {}
    override fun generateComboFromTags(selectedTags: Set<Tag>, length: Int?, allowRepeats: Boolean): List<Move> = emptyList()
    override fun generateStructuredCombo(tagSequence: List<Tag>): List<Move> {
        return tagSequence.mapNotNull { tag ->
            initialFakeMovesWithTags.find { it.tags.contains(tag) }?.move
        }
    }
    override fun saveCombo(comboName: String, moves: List<String>) {}
    override fun deleteSavedCombo(savedComboId: String) {}
    override fun updateSavedComboName(savedComboId: String, newName: String) {}
    override fun resetDatabase() {}
    override suspend fun getSavedComboForEditing(comboId: String): SavedCombo? = null
    override fun updateSavedCombo(comboId: String, newName: String, newMoves: List<String>) {}
    override suspend fun getAppDataForExport(): AppDataExport? = null
    override suspend fun importAppData(appData: AppDataExport): Boolean = true
}
