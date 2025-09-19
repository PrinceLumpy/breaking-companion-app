package com.example.combogenerator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Corrected import
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // Keep for real app usage
import androidx.navigation.NavController
import com.example.combogenerator.data.Tag
// Import the interface and the Fake implementation
import com.example.combogenerator.viewmodel.IMoveViewModel
import com.example.combogenerator.viewmodel.FakeMoveViewModel
import com.example.combogenerator.viewmodel.MoveViewModel // Still needed for the default viewModel()
import com.example.combogenerator.ui.theme.ComboGeneratorTheme
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagListScreen(
    navController: NavController,
    // Use the interface IMoveViewModel.
    // The viewModel() delegate will still provide the concrete MoveViewModel in the actual app.
    moveViewModel: IMoveViewModel = viewModel<MoveViewModel>()
) {
    val tagsList by moveViewModel.allTags.observeAsState(initial = emptyList())
    var showEditDialog by remember { mutableStateOf<Tag?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Tag?>(null) }
    var tagNameForEdit by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Tags") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (tagsList.isEmpty()) {
                Text("No tags found. Add tags when creating or editing moves.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tagsList, key = { it.id }) { tag ->
                        TagListItem(
                            tag = tag,
                            onEditClick = {
                                tagNameForEdit = it.name
                                showEditDialog = it
                            },
                            onDeleteClick = {
                                showDeleteDialog = it
                            }
                        )
                    }
                }
            }
        }
    }

    showEditDialog?.let { tagToEdit ->
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text("Edit Tag Name") },
            text = {
                OutlinedTextField(
                    value = tagNameForEdit,
                    onValueChange = { tagNameForEdit = it },
                    label = { Text("New Tag Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tagNameForEdit.isNotBlank() && tagNameForEdit != tagToEdit.name) {
                            moveViewModel.updateTag(tagToEdit.id, tagNameForEdit)
                        }
                        showEditDialog = null
                        tagNameForEdit = ""
                    },
                    enabled = tagNameForEdit.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showEditDialog = null
                    tagNameForEdit = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    showDeleteDialog?.let { tagToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete the tag '${tagToDelete.name}'? This will remove it from all associated moves.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        moveViewModel.deleteTag(tagToDelete)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TagListItem(
    tag: Tag,
    onEditClick: (Tag) -> Unit,
    onDeleteClick: (Tag) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = tag.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Row {
                IconButton(onClick = { onEditClick(tag) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit Tag")
                }
                IconButton(onClick = { onDeleteClick(tag) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Tag", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TagListScreenPreview() {
    ComboGeneratorTheme {
        val dummyNavController = rememberNavController()
        // Use FakeMoveViewModel for the preview
        TagListScreen(navController = dummyNavController, moveViewModel = FakeMoveViewModel())
    }
}

@Preview(showBackground = true)
@Composable
fun TagListItemPreview() {
    ComboGeneratorTheme {
        TagListItem(tag = Tag("1", "Beginner"), onEditClick = {}, onDeleteClick = {})
    }
}
