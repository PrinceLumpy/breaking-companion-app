package com.princelumpy.breakvault.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.princelumpy.breakvault.data.AppDB
import com.princelumpy.breakvault.data.model.goal.GoalWithStages

class ArchivedGoalsViewModel(application: Application) : AndroidViewModel(application) {

    private val goalDao = AppDB.getDatabase(application).goalDao()

    val archivedGoals: LiveData<List<GoalWithStages>> = goalDao.getArchivedGoalsWithStages()
}
