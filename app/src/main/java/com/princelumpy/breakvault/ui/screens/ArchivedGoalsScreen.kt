package com.princelumpy.breakvault.ui.screens

import AppStyleDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.viewmodel.ArchivedGoalsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedGoalsScreen(
    navController: NavController,
    archivedGoalsViewModel: ArchivedGoalsViewModel = viewModel()
) {
    val archivedGoals by archivedGoalsViewModel.archivedGoals.observeAsState(initial = emptyList())

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(id = R.string.archived_goals_title)) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.common_back_button_description)
                    )
                }
            }
        )
    }) { paddingValues ->
        if (archivedGoals.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(id = R.string.archived_goals_no_goals))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(AppStyleDefaults.SpacingLarge),
                verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
            ) {
                items(archivedGoals) { goalWithStages ->
                    GoalCard(
                        goalWithStages = goalWithStages,
                        onEditClick = { navController.navigate("edit_goal/${goalWithStages.goal.id}") }
                    )
                }
            }
        }
    }
}
