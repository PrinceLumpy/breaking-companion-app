package com.princelumpy.breakvault.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.AppDB
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddEditGoalStageUiState(
    val stageId: String? = null,
    val goalId: String,
    val name: String = "",
    val targetCount: String = "",
    val unit: String = "reps",
    val isNameError: Boolean = false,
    val isTargetError: Boolean = false,
    val isNewStage: Boolean = true,
    val showDeleteDialog: Boolean = false
)

class GoalStageViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<AddEditGoalStageUiState?>(null)
    val uiState: StateFlow<AddEditGoalStageUiState?> = _uiState.asStateFlow()

    private val goalDao = AppDB.getDatabase(application).goalDao()

    fun loadStage(goalId: String, stageId: String?) {
        if (stageId == null) {
            _uiState.value = AddEditGoalStageUiState(goalId = goalId)
            return
        }

        viewModelScope.launch {
            val stage = goalDao.getGoalStageById(stageId)
            if (stage != null) {
                _uiState.value = AddEditGoalStageUiState(
                    stageId = stageId,
                    goalId = goalId,
                    name = stage.name,
                    targetCount = stage.targetCount.toString(),
                    unit = stage.unit,
                    isNewStage = false
                )
            } else {
                // Handle error case where stage is not found
            }
        }
    }

    fun onNameChange(newName: String) {
        if (newName.length <= 30) {
            _uiState.update { it?.copy(name = newName, isNameError = false) }
        }
    }

    fun onTargetCountChange(newTarget: String) {
        if (newTarget.isEmpty() || newTarget.all { it.isDigit() }) {
            _uiState.update { it?.copy(targetCount = newTarget, isTargetError = false) }
        }
    }

    fun onUnitChange(newUnit: String) {
        if (newUnit.length <= 10) {
            _uiState.update { it?.copy(unit = newUnit) }
        }
    }

    fun saveStage(onSuccess: () -> Unit) {
        val currentUiState = _uiState.value ?: return

        val target = currentUiState.targetCount.toIntOrNull()

        if (currentUiState.name.isBlank()) {
            _uiState.update { it?.copy(isNameError = true) }
            return
        }

        if (target == null || target <= 0) {
            _uiState.update { it?.copy(isTargetError = true) }
            return
        }

        viewModelScope.launch {
            if (currentUiState.isNewStage) {
                goalDao.insertGoalStage(
                    com.princelumpy.breakvault.data.model.goal.GoalStage(
                        goalId = currentUiState.goalId,
                        name = currentUiState.name,
                        targetCount = target,
                        unit = currentUiState.unit
                    )
                )
            } else {
                goalDao.updateGoalStage(
                    com.princelumpy.breakvault.data.model.goal.GoalStage(
                        id = currentUiState.stageId!!,
                        goalId = currentUiState.goalId,
                        name = currentUiState.name,
                        targetCount = target,
                        unit = currentUiState.unit
                    )
                )
            }
            onSuccess()
        }
    }

    fun showDeleteDialog(show: Boolean) {
        _uiState.update { it?.copy(showDeleteDialog = show) }
    }

    fun deleteStage(onSuccess: () -> Unit) {
        val stageId = _uiState.value?.stageId ?: return
        viewModelScope.launch {
            goalDao.getGoalStageById(stageId)?.let {
                goalDao.deleteGoalStage(it)
                onSuccess()
            }
        }
    }
}
