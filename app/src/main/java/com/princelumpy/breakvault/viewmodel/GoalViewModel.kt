package com.princelumpy.breakvault.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.princelumpy.breakvault.data.AppDB
import com.princelumpy.breakvault.data.model.goal.Goal
import com.princelumpy.breakvault.data.model.goal.GoalStage
import com.princelumpy.breakvault.data.model.goal.GoalWithStages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GoalsScreenUiState(
    val goals: List<GoalWithStages> = emptyList(),
    val isLoading: Boolean = true
)

data class AddEditGoalUiState(
    val goalId: String? = null,
    val title: String = "",
    val description: String = "",
    val stages: List<GoalStage> = emptyList(),
    val isNewGoal: Boolean = true,
    val isLoading: Boolean = true,
    val snackbarMessage: String? = null
)

class GoalViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AddEditGoalUiState())
    val uiState: StateFlow<AddEditGoalUiState> = _uiState.asStateFlow()

    private val _goalsScreenUiState = MutableStateFlow(GoalsScreenUiState())
    val goalsScreenUiState: StateFlow<GoalsScreenUiState> = _goalsScreenUiState.asStateFlow()

    private val goalDao = AppDB.getDatabase(application).goalDao()

    val activeGoalsWithStages: LiveData<List<GoalWithStages>> = goalDao.getActiveGoalsWithStages()

    init {
        activeGoalsWithStages.observeForever { goals ->
            _goalsScreenUiState.update { it.copy(goals = goals, isLoading = false) }
        }
    }

    fun loadGoal(goalId: String?) {
        if (goalId == null) {
            _uiState.value = AddEditGoalUiState(isLoading = false, isNewGoal = true)
            return
        }

        viewModelScope.launch {
            val goalBeingEdited = getGoalForEditing(goalId)
            if (goalBeingEdited != null) {
                _uiState.value = AddEditGoalUiState(
                    goalId = goalId,
                    title = goalBeingEdited.goal.title,
                    description = goalBeingEdited.goal.description,
                    stages = goalBeingEdited.stages,
                    isNewGoal = false,
                    isLoading = false
                )
            } else {
                _uiState.update {
                    it.copy(
                        snackbarMessage = "Could not find goal with ID $goalId",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        if (newTitle.length <= 100) {
            _uiState.update { it.copy(title = newTitle) }
        }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun saveGoal(onSuccess: (goalId: String) -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.title.isBlank()) {
                _uiState.update { it.copy(snackbarMessage = "Goal title cannot be blank.") }
                return@launch
            }

            if (currentState.isNewGoal) {
                val newId = createGoal(currentState.title, currentState.description)
                onSuccess(newId)
            } else {
                updateGoal(currentState.goalId!!, currentState.title, currentState.description)
                onSuccess(currentState.goalId)
            }
        }
    }

    fun onSnackbarMessageShown() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun onAddStageClicked(navigate: (goalId: String) -> Unit) {
        viewModelScope.launch {
            saveGoal { savedGoalId ->
                navigate(savedGoalId)
            }
        }
    }

    suspend fun createGoal(title: String, description: String): String {
        val newGoal = Goal(
            title = title,
            description = description
        )
        goalDao.insertGoal(newGoal)
        return newGoal.id
    }

    suspend fun getGoalForEditing(goalId: String): GoalWithStages? {
        return goalDao.getGoalWithStages(goalId)
    }

    private fun updateGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.updateGoal(goal.copy(lastUpdated = System.currentTimeMillis()))
        }
    }

    private suspend fun updateGoal(goalId: String, title: String, description: String) {
        val goalToUpdate = goalDao.getGoalById(goalId)
        if (goalToUpdate != null) {
            val updatedGoal = goalToUpdate.copy(
                title = title,
                description = description
            )
            goalDao.updateGoal(updatedGoal)
        }
    }

    fun archiveGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.updateGoal(
                goal.copy(
                    isArchived = true,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.deleteAllStagesForGoal(goal.id)
            goalDao.deleteGoal(goal)
        }
    }

    fun getGoalById(goalId: String): LiveData<Goal?> {
        return goalDao.getGoalLive(goalId)
    }

    fun getStagesForGoal(goalId: String): LiveData<List<GoalStage>> {
        return goalDao.getStagesForGoal(goalId)
    }

    fun addGoalStage(goalId: String, name: String, targetCount: Int, unit: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val stage = GoalStage(
                goalId = goalId,
                name = name,
                targetCount = targetCount,
                unit = unit
            )
            goalDao.insertGoalStage(stage)
            goalDao.getGoal(goalId)?.let {
                goalDao.updateGoal(it.copy(lastUpdated = System.currentTimeMillis()))
            }
        }
    }

    fun updateGoalStage(stage: GoalStage) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.updateGoalStage(stage)
        }
    }

    fun incrementStageProgress(stage: GoalStage, amount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val newCount =
                (stage.currentCount + amount).coerceAtLeast(0).coerceAtMost(stage.targetCount)
            goalDao.updateGoalStage(stage.copy(currentCount = newCount))
            goalDao.getGoal(stage.goalId)?.let {
                goalDao.updateGoal(it.copy(lastUpdated = System.currentTimeMillis()))
            }
        }
    }

    fun updateGoalStageById(stageId: String, name: String, targetCount: Int, unit: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val stage = goalDao.getGoalStage(stageId)
            stage?.let {
                goalDao.updateGoalStage(
                    it.copy(
                        name = name,
                        targetCount = targetCount,
                        unit = unit
                    )
                )
            }
        }
    }

    fun deleteGoalStage(stage: GoalStage) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.deleteGoalStage(stage)
        }
    }

    fun deleteGoalStageById(stageId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val stage = goalDao.getGoalStage(stageId)
            stage?.let {
                goalDao.deleteGoalStage(it)
            }
        }
    }

    suspend fun getStageById(stageId: String): GoalStage? {
        return withContext(Dispatchers.IO) {
            goalDao.getGoalStage(stageId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        activeGoalsWithStages.removeObserver { }
    }
}
