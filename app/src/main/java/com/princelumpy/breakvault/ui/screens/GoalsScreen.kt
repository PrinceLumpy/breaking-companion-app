package com.princelumpy.breakvault.ui.screens

import AppStyleDefaults
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.model.goal.GoalWithStages
import com.princelumpy.breakvault.viewmodel.GoalViewModel
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    navController: NavController,
    goalViewModel: GoalViewModel = viewModel()
) {
    val uiState by goalViewModel.goalsScreenUiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            if (uiState.goals.isNotEmpty()) {
                FloatingActionButton(onClick = {
                    runBlocking {
                        val newGoalId = goalViewModel.createGoal(title = "", description = "")
                        navController.navigate("edit_goal/$newGoalId")
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.goals_screen_add_goal_description))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(AppStyleDefaults.SpacingLarge)
        ) {
            if (uiState.goals.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(id = R.string.goals_screen_no_goals_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(id = R.string.goals_screen_no_goals_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = AppStyleDefaults.SpacingMedium)
                    )
                    Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingLarge))
                    Button(
                        onClick = {
                            runBlocking {
                                val newGoalId = goalViewModel.createGoal(title = "", description = "")
                                navController.navigate("edit_goal/$newGoalId")
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.padding(AppStyleDefaults.SpacingSmall))
                        Text(stringResource(id = R.string.goals_screen_create_goal_button))
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
                ) {
                    items(uiState.goals) { goalWithStages ->
                        if (goalWithStages.goal.title.isNotBlank() || goalWithStages.goal.description.isNotBlank()) {
                            GoalCard(
                                goalWithStages = goalWithStages,
                                onEditClick = { navController.navigate("edit_goal/${goalWithStages.goal.id}") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GoalCard(
    goalWithStages: GoalWithStages,
    onEditClick: () -> Unit
) {
    val progress = if (goalWithStages.stages.isNotEmpty()) {
        goalWithStages.stages.sumOf { it.currentCount.toDouble() / it.targetCount } / goalWithStages.stages.size
    } else {
        0.0
    }

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = AppStyleDefaults.SpacingSmall),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { /* Tapping currently does nothing/expands, or go to edit if desired */ },
                onLongClick = onEditClick
            )
    ) {
        Column(
            modifier = Modifier
                .padding(AppStyleDefaults.SpacingLarge)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (goalWithStages.goal.title.isBlank()) stringResource(id = R.string.goals_screen_untitled_goal) else goalWithStages.goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            if (goalWithStages.goal.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))
                Text(
                    text = goalWithStages.goal.description,
                    style = MaterialTheme. typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (goalWithStages.stages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingMedium))
                LinearProgressIndicator(
                    progress = { progress.toFloat() },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))
                Text(
                    text = stringResource(id = R.string.goals_screen_progress_text, (progress * 100).toInt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
