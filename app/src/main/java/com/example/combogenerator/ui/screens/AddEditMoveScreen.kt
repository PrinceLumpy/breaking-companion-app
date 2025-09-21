package com.example.combogenerator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Changed for consistency
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.combogenerator.data.Tag
import com.example.combogenerator.ui.theme.ComboGeneratorTheme
// Import the interface and the Fake implementation
import com.example.combogenerator.viewmodel.IMoveViewModel
import com.example.combogenerator.viewmodel.FakeMoveViewModel
import com.example.combogenerator.viewmodel.MoveViewModel // Still needed for the default viewModel()

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditMoveScreen(
    navController: NavController,
    moveId: String?,
    moveViewModel: IMoveViewModel = viewModel<MoveViewModel>()
) {
    var moveName by remember { mutableStateOf("") }
    var newTagName by remember { mutableStateOf("") }
    val allTags by moveViewModel.allTags.observeAsState(initial = emptyList())
    var selectedTags by remember { mutableStateOf(setOf<Tag>()) }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(key1 = moveId) {
        if (moveId != null) {
            val moveBeingEdited = moveViewModel.getMoveForEditing(moveId)
            if (moveBeingEdited != null) {
                moveName = moveBeingEdited.move.name
                selectedTags = moveBeingEdited.tags.toSet()
            } else {
                println("AddEditMoveScreen: Could not find move with ID $moveId for editing.")
            }
        } else {
            moveName = ""
            selectedTags = setOf()
        }
    }

    val saveMoveAction: () -> Unit = {
        if (moveName.isNotBlank()) {
            if (moveId == null) {
                moveViewModel.addMove(moveName, selectedTags.toList())
            } else {
                moveViewModel.updateMoveAndTags(moveId, moveName, selectedTags.toList())
            }
            keyboardController?.hide()
            navController.popBackStack()
        } else {
            println("Move name cannot be blank.") // TODO: Show user-facing error
        }
    }

    val addTagAction: () -> Unit = {
        if (newTagName.isNotBlank()) {
            val trimmedTagName = newTagName.trim()
            if (!allTags.any { it.name.equals(trimmedTagName, ignoreCase = true) }) {
                moveViewModel.addTag(trimmedTagName)
                newTagName = "" // Clear input after successful add
            } else {
                println("Tag '$trimmedTagName' already exists.") // TODO: Show user-facing error
                newTagName = "" // Clear input after attempting to add existing tag
            }
            keyboardController?.hide()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (moveId == null) "Add New Move" else "Edit Move") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = saveMoveAction) {
                Icon(Icons.Filled.Done, contentDescription = "Save Move")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = moveName,
                onValueChange = { moveName = it },
                label = { Text("Move Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { saveMoveAction() })
            )

            Text("Select Tags:", style = MaterialTheme.typography.titleMedium)
            if (allTags.isEmpty() && newTagName.isBlank()) {
                Text("No tags available. Add some below!", style = MaterialTheme.typography.bodySmall)
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                allTags.forEach { tag ->
                    FilterChip(
                        selected = selectedTags.any { it.id == tag.id },
                        onClick = {
                            selectedTags = if (selectedTags.any { it.id == tag.id }) {
                                selectedTags.filterNot { selectedTag -> selectedTag.id == tag.id }.toSet()
                            } else {
                                selectedTags + tag
                            }
                        },
                        label = { Text(tag.name) },
                        leadingIcon = if (selectedTags.any { it.id == tag.id }) {
                            { Icon(Icons.Filled.Done, "Selected", Modifier.size(FilterChipDefaults.IconSize)) }
                        } else { null }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Add New Tag:", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    label = { Text("New Tag Name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { addTagAction() })
                )
                Button(onClick = addTagAction) {
                    Text("Add Tag")
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Add Mode")
@Composable
fun AddEditMoveScreenPreview_AddMode() {
    ComboGeneratorTheme {
        AddEditMoveScreen(
            navController = rememberNavController(),
            moveId = null,
            moveViewModel = FakeMoveViewModel() // Use Fake ViewModel
        )
    }
}

@Preview(showBackground = true, name = "Edit Mode")
@Composable
fun AddEditMoveScreenPreview_EditMode() {
    ComboGeneratorTheme {
        AddEditMoveScreen(
            navController = rememberNavController(),
            moveId = "previewEditId", // Use the ID faked in FakeMoveViewModel
            moveViewModel = FakeMoveViewModel() // Use Fake ViewModel
        )
    }
}
