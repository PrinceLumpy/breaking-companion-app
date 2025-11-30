package com.princelumpy.breakvault.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.princelumpy.breakvault.data.BattleTag
import com.princelumpy.breakvault.viewmodel.BattleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattleTagListScreen(
    navController: NavController,
    battleViewModel: BattleViewModel = viewModel()
) {
    val tagsList by battleViewModel.allBattleTags.observeAsState(initial = emptyList())
    var showEditDialog by remember { mutableStateOf<BattleTag?>(null) }
    var showDeleteDialog by remember { mutableStateOf<BattleTag?>(null) }
    var tagNameForEdit by remember { mutableStateOf("") }

    var showAddTagDialog by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Battle Tags") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTagDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Tag")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (tagsList.isEmpty() && !showAddTagDialog) {
                Text("No battle tags found. Click '+' to add one.")
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = tagsList,
                    key = { it.id }
                ) { tag ->
                    BattleTagListItem(
                        tag = tag,
                        onEditClick = {
                            tagNameForEdit = it.name
                            showEditDialog = it
                        },
                        onDeleteClick = { showDeleteDialog = it }
                    )
                }
            }
        }
    }

    if (showAddTagDialog) {
        AlertDialog(
            onDismissRequest = { showAddTagDialog = false; newTagName = "" },
            title = { Text("Add New Battle Tag") },
            text = {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    label = { Text("Tag Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTagName.isNotBlank()) {
                            battleViewModel.addBattleTag(newTagName)
                            showAddTagDialog = false
                            newTagName = ""
                        }
                    },
                    enabled = newTagName.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTagDialog = false; newTagName = "" }) {
                    Text("Cancel")
                }
            }
        )
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
                            battleViewModel.updateBattleTag(tagToEdit.copy(name = tagNameForEdit))
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
                TextButton(onClick = { showEditDialog = null; tagNameForEdit = "" }) {
                    Text("Cancel")
                }
            }
        )
    }

    showDeleteDialog?.let { tagToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete '${tagToDelete.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        battleViewModel.deleteBattleTag(tagToDelete)
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
fun BattleTagListItem(
    tag: BattleTag,
    onEditClick: (BattleTag) -> Unit,
    onDeleteClick: (BattleTag) -> Unit
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
