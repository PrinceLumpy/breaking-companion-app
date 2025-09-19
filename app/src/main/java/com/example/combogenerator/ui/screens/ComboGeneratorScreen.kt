package com.example.combogenerator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Save // Added for Save icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.combogenerator.data.Move // Added import
import com.example.combogenerator.data.Tag
// Import the interface and the Fake implementation
import com.example.combogenerator.viewmodel.IMoveViewModel
import com.example.combogenerator.viewmodel.FakeMoveViewModel
import com.example.combogenerator.viewmodel.MoveViewModel // Still needed for the default viewModel()
import com.example.combogenerator.ui.theme.ComboGeneratorTheme
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ComboGeneratorScreen(
    navController: NavController,
    moveViewModel: IMoveViewModel = viewModel<MoveViewModel>()
) {
    val allTags by moveViewModel.allTags.observeAsState(initial = emptyList())
    var selectedGeneratorTags by remember { mutableStateOf(setOf<Tag>()) }
    var generatedComboText by remember { mutableStateOf("Your combo will appear here.") }
    var currentGeneratedMoves by remember { mutableStateOf<List<Move>>(emptyList()) } // Added to store the generated moves

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Combo Generator") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Select Tags for your Combo:", style = MaterialTheme.typography.titleMedium)

            if (allTags.isEmpty()) {
                Text(
                    "No tags available to generate a combo.",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    allTags.forEach { tag ->
                        FilterChip(
                            selected = selectedGeneratorTags.contains(tag),
                            onClick = {
                                selectedGeneratorTags = if (selectedGeneratorTags.contains(tag)) {
                                    selectedGeneratorTags - tag
                                } else {
                                    selectedGeneratorTags + tag
                                }
                            },
                            label = { Text(tag.name) },
                            leadingIcon = if (selectedGeneratorTags.contains(tag)) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Done,
                                        contentDescription = "Selected",
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else {
                                null
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (selectedGeneratorTags.isNotEmpty()) {
                        val comboMoves = moveViewModel.generateComboFromTags(selectedGeneratorTags)
                        if (comboMoves.isNotEmpty()) {
                            currentGeneratedMoves = comboMoves // Store the actual moves
                            generatedComboText = comboMoves.joinToString(separator = "  ->  ") { it.name }
                        } else {
                            currentGeneratedMoves = emptyList() // Clear if no moves found
                            generatedComboText = "No moves found matching the selected tags. Try a different selection."
                        }
                    } else {
                        currentGeneratedMoves = emptyList() // Clear if no tags selected
                        generatedComboText = "Please select at least one tag to generate a combo."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedGeneratorTags.isNotEmpty()
            ) {
                Text("Generate Combo")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Generated Combo:", style = MaterialTheme.typography.titleMedium)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 100.dp)
            ) {
                Text(
                    text = generatedComboText,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Save Combo Button
            if (currentGeneratedMoves.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        moveViewModel.saveCombo("", currentGeneratedMoves) // Pass empty string for auto-name
                        // Optionally, provide user feedback e.g., show a Snackbar
                        // And clear the current combo to prevent re-saving the same one immediately
                        // generatedComboText = "Combo saved! Generate a new one."
                        // currentGeneratedMoves = emptyList()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Save, contentDescription = "Save Combo", modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Save Combo")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ComboGeneratorScreenPreview() {
    ComboGeneratorTheme {
        // Create a FakeMoveViewModel that also has some saved combos for preview if needed
        val previewViewModel = FakeMoveViewModel()
        // Example of pre-populating generated moves for previewing the save button
        // val sampleMoves = listOf(Move(id = "m1", name = "Preview Move 1"), Move(id = "m2", name = "Preview Move 2"))

        ComboGeneratorScreen(
            navController = rememberNavController(),
            moveViewModel = previewViewModel
        )
    }
}
