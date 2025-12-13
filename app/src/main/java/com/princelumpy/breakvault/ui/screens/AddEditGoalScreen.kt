package com.princelumpy.breakvault.ui.screens

import GoalInputDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.Screen
import com.princelumpy.breakvault.viewmodel.GoalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditGoalScreen(
    navController: NavController,
    goalId: String?,
    goalViewModel: GoalViewModel = viewModel()
) {
    val uiState by goalViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(goalId) {
        goalViewModel.loadGoal(goalId)
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            goalViewModel.onSnackbarMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = if (uiState.isNewGoal) R.string.add_edit_goal_create_title else R.string.add_edit_goal_edit_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.common_back_button_description))
                    }
                },
                actions = {
                    if (!uiState.isNewGoal) {
                        IconButton(onClick = { /* Handle archive */ }) {
                            Icon(Icons.Default.Archive, contentDescription = stringResource(id = R.string.add_edit_goal_archive_description))
                        }
                        IconButton(onClick = { /* Handle delete */ }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.add_edit_goal_delete_description))
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                goalViewModel.saveGoal {
                    focusManager.clearFocus()
                    navController.popBackStack()
                }
            }) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = stringResource(id = R.string.save_goal_content_description),
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(AppStyleDefaults.SpacingLarge)
        ) {
            OutlinedTextField(
                value = uiState.title,
                onValueChange = { goalViewModel.onTitleChange(it) },
                label = { Text(stringResource(id = R.string.add_edit_goal_title_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingMedium))

            OutlinedTextField(
                value = uiState.description,
                onValueChange = { goalViewModel.onDescriptionChange(it) },
                label = { Text(stringResource(id = R.string.add_edit_goal_description_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(GoalInputDefaults.DESCRIPTION_FIELD_HEIGHT)
            )

            Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingExtraLarge))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.add_edit_goal_stages_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = {
                    goalViewModel.onAddStageClicked { savedGoalId ->
                        navController.navigate(
                            Screen.AddEditGoalStage.withOptionalArgs(mapOf("goalId" to savedGoalId))
                        )
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add_edit_goal_add_stage_button))
                    Spacer(modifier = Modifier.padding(start = AppStyleDefaults.SpacingSmall))
                    Text(stringResource(id = R.string.add_edit_goal_add_stage_button))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = AppStyleDefaults.SpacingLarge))

            if (uiState.stages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AppStyleDefaults.SpacingLarge),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.add_edit_goal_no_stages_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column {
                    uiState.stages.forEach { goalStage ->
                        Text(text = goalStage.name, modifier = Modifier.padding(vertical = AppStyleDefaults.SpacingMedium))
                    }
                }
            }
        }
    }
}
