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
    var currentGeneratedMoves by remember { mutableStateOf<List<Move>>(emptyList()) }
    var selectedLength by remember { mutableStateOf<Int?>(null) } // null for Random
    val lengthOptions = listOf(null, 2, 3, 4, 5, 6) // null represents "Random"

    var showLengthWarningDialog by remember { mutableStateOf(false) }
    var warningDialogMessage by remember { mutableStateOf("") }
    var lengthDropdownExpanded by remember { mutableStateOf(false) }

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

            Spacer(modifier = Modifier.height(4.dp)) // Changed from 5.dp to 4.dp

            Text("Number of Moves:", style = MaterialTheme.typography.titleMedium)
            Box(modifier = Modifier.fillMaxWidth()) { // Box to allow better alignment/sizing if needed
                ExposedDropdownMenuBox(
                    expanded = lengthDropdownExpanded,
                    onExpandedChange = { lengthDropdownExpanded = !lengthDropdownExpanded },
                    modifier = Modifier.align(Alignment.Center) // Centers the dropdown box
                ) {
                    OutlinedTextField(
                        value = selectedLength?.toString() ?: "Random",
                        onValueChange = {}, // Not directly editable
                        readOnly = true,
                        label = { Text("Combo Length") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lengthDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(), // Use outlined variant colors
                        modifier = Modifier.fillMaxWidth(0.6f) // Anchor for the dropdown menu (handled by ExposedDropdownMenuBox)
                    )
                    ExposedDropdownMenu(
                        expanded = lengthDropdownExpanded,
                        onDismissRequest = { lengthDropdownExpanded = false }
                    ) {
                        lengthOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option?.toString() ?: "Random") },
                                onClick = {
                                    selectedLength = option
                                    lengthDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp)) // Changed from 16.dp to 12.dp

            Button(
                onClick = {
                    if (selectedGeneratorTags.isNotEmpty()) {
                        val comboMoves = moveViewModel.generateComboFromTags(selectedGeneratorTags, selectedLength)
                        if (comboMoves.isNotEmpty()) {
                            currentGeneratedMoves = comboMoves
                            generatedComboText = comboMoves.joinToString(separator = "  ->  ") { it.name }

                            if (selectedLength != null && selectedLength!! > comboMoves.size) {
                                warningDialogMessage = "Only ${comboMoves.size} are available with the selected tags. A combo of ${comboMoves.size} moves has been generated."
                                showLengthWarningDialog = true
                            }
                        } else {
                            currentGeneratedMoves = emptyList()
                            generatedComboText = "No moves found matching the selected tags. Try a different selection or 'Random' length."
                        }
                    } else {
                        currentGeneratedMoves = emptyList()
                        generatedComboText = "Please select at least one tag to generate a combo."
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedGeneratorTags.isNotEmpty()
            ) {
                Text("Generate Combo")
            }

            Spacer(modifier = Modifier.height(4.dp))

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

            if (currentGeneratedMoves.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        moveViewModel.saveCombo("", currentGeneratedMoves)
                        // Consider giving feedback like a Snackbar that combo is saved
                        // generatedComboText = "Combo saved!"
                        // currentGeneratedMoves = emptyList() // Optionally clear after saving
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

    if (showLengthWarningDialog) {
        AlertDialog(
            onDismissRequest = { showLengthWarningDialog = false },
            title = { Text("Note") },
            text = { Text(warningDialogMessage) },
            confirmButton = {
                TextButton(onClick = { showLengthWarningDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ComboGeneratorScreenPreview() {
    ComboGeneratorTheme {
        val previewViewModel = FakeMoveViewModel()
        ComboGeneratorScreen(
            navController = rememberNavController(),
            moveViewModel = previewViewModel
        )
    }
}
