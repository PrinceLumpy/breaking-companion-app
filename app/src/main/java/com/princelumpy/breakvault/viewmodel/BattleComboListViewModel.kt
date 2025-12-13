package com.princelumpy.breakvault.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.AppDB
import com.princelumpy.breakvault.data.model.battlecombo.BattleCombo
import com.princelumpy.breakvault.data.model.battlecombo.BattleComboTagCrossRef
import com.princelumpy.breakvault.data.model.battlecombo.BattleComboWithTags
import com.princelumpy.breakvault.data.model.battlecombo.BattleTag
import com.princelumpy.breakvault.data.model.battlecombo.EnergyLevel
import com.princelumpy.breakvault.data.model.battlecombo.TrainingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BattleComboListViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDB.getDatabase(application)
    private val battleDao = db.battleDao()

    val battleCombos: LiveData<List<BattleComboWithTags>> =
        battleDao.getAllBattleCombosWithTags()
    val allBattleTags: LiveData<List<BattleTag>> = battleDao.getAllBattleTags()

    // Battle Combo Methods
    fun toggleUsed(combo: BattleCombo) {
        viewModelScope.launch(Dispatchers.IO) {
            // Do not update modifiedAt when toggling isUsed
            battleDao.updateBattleCombo(combo.copy(isUsed = !combo.isUsed))
        }
    }

    fun resetBattle() {
        viewModelScope.launch(Dispatchers.IO) {
            battleDao.resetAllBattleCombosUsage()
        }
    }

    fun addBattleCombo(
        description: String,
        energy: EnergyLevel,
        status: TrainingStatus,
        tags: List<String>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val newCombo = BattleCombo(
                description = description,
                energy = energy,
                status = status
            )
            // createdAt/modifiedAt defaults are applied in constructor
            battleDao.insertBattleCombo(newCombo)

            tags.forEach { tagName ->
                var tag = battleDao.getBattleTagByName(tagName)
                // Create moveTag if it doesn't exist (though UI typically ensures this)
                if (tag == null) {
                    tag = BattleTag(name = tagName)
                    battleDao.insertBattleTag(tag)
                }
                // Link
                battleDao.link(BattleComboTagCrossRef(newCombo.id, tag.id))
            }
        }
    }

    fun updateBattleCombo(combo: BattleCombo, tagNames: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            // Ensure modifiedAt is updated for content changes
            battleDao.updateBattleCombo(combo.copy(modifiedAt = System.currentTimeMillis()))

            // Update cross-refs: Delete old, Insert new
            battleDao.unlinkBattleComboFromAllTags(combo.id)

            tagNames.forEach { tagName ->
                var tag = battleDao.getBattleTagByName(tagName)
                if (tag == null) {
                    tag = BattleTag(name = tagName)
                    battleDao.insertBattleTag(tag)
                }
                battleDao.link(BattleComboTagCrossRef(combo.id, tag.id))
            }
        }
    }

    fun deleteBattleCombo(combo: BattleCombo) {
        viewModelScope.launch(Dispatchers.IO) {
            battleDao.deleteBattleCombo(combo)
        }
    }

    suspend fun getBattleComboById(id: String): BattleComboWithTags? {
        return withContext(Dispatchers.IO) {
            battleDao.getBattleComboWithTags(id)
        }
    }

    // Battle MoveTag Methods
    fun addBattleTag(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (battleDao.getBattleTagByName(name) == null) {
                battleDao.insertBattleTag(BattleTag(name = name))
            }
        }
    }

    fun updateBattleTag(tag: BattleTag) {
        viewModelScope.launch(Dispatchers.IO) {
            battleDao.updateBattleTag(tag.copy(modifiedAt = System.currentTimeMillis()))
        }
    }

    fun deleteBattleTag(tag: BattleTag) {
        viewModelScope.launch(Dispatchers.IO) {
            battleDao.deleteBattleTag(tag)
        }
    }
}
