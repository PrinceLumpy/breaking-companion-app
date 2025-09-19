package com.example.combogenerator.ui.screens

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.combogenerator.Screen
import com.example.combogenerator.data.Move
import com.example.combogenerator.data.MoveWithTags
import com.example.combogenerator.data.Tag
import com.example.combogenerator.ui.theme.ComboGeneratorTheme
// Import the interface and the Fake implementation
import com.example.combogenerator.viewmodel.IMoveViewModel
import com.example.combogenerator.viewmodel.FakeMoveViewModel
import com.example.combogenerator.viewmodel.MoveViewModel // Still needed for the default viewModel()

@Composable
fun MoveListScreen(
    navController: NavController,
    // Use the interface IMoveViewModel
    moveViewModel: IMoveViewModel = viewModel<MoveViewModel>()
) {
    val movesList by moveViewModel.movesWithTags.observeAsState(initial = emptyList())
    var moveToDelete by remember { mutableStateOf<MoveWithTags?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { navController.navigate(Screen.AddEditMove.route) }) {
                Text("Add Move")
            }
            Button(onClick = { navController.navigate(Screen.ComboGenerator.route) }) {
                Text("Generate Combo")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (movesList.isEmpty()) {
            Text("No moves yet! Click 'Add Move' to get started.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(movesList, key = { it.move.id }) { moveWithTags ->
                    MoveCard(
                        moveWithTags = moveWithTags,
                        onEditClick = { moveId ->
                            navController.navigate(Screen.AddEditMove.withOptionalArgs(mapOf("moveId" to moveId)))
                        },
                        onDeleteClick = { mwt ->
                            moveToDelete = mwt
                        }
                    )
                }
            }
        }
    }

    moveToDelete?.let { moveWithTagsToDelete ->
        AlertDialog(
            onDismissRequest = { moveToDelete = null },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete '${moveWithTagsToDelete.move.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        moveViewModel.deleteMove(moveWithTagsToDelete.move)
                        moveToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { moveToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MoveCard(
    moveWithTags: MoveWithTags,
    onEditClick: (String) -> Unit,
    onDeleteClick: (MoveWithTags) -> Unit
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
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = moveWithTags.move.name, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                if (moveWithTags.tags.isNotEmpty()) {
                    Text(
                        text = "Tags: ${moveWithTags.tags.joinToString(", ") { it.name }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "No tags",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Button(
                    onClick = { onEditClick(moveWithTags.move.id) },
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text("Edit")
                }
                IconButton(onClick = { onDeleteClick(moveWithTags) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete Move",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MoveListScreenPreview() {
    ComboGeneratorTheme {
        // Use FakeMoveViewModel for the preview
        MoveListScreen(navController = rememberNavController(), moveViewModel = FakeMoveViewModel())
    }
}

@Preview(showBackground = true)
@Composable
fun MoveCardPreview() {
    ComboGeneratorTheme {
        val previewMove = Move(id = "prev1", name = "Preview Jab")
        val previewTags = listOf(Tag(id = "t1", name = "Fast"), Tag(id = "t2", name = "Setup"))
        val moveWithTags = MoveWithTags(move = previewMove, tags = previewTags)
        MoveCard(
            moveWithTags = moveWithTags,
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}
