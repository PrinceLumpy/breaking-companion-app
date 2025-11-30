package com.princelumpy.breakvault.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.Move
import com.princelumpy.breakvault.data.Tag
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme
import com.princelumpy.breakvault.viewmodel.FakeMoveViewModel
import com.princelumpy.breakvault.viewmodel.IMoveViewModel
import com.princelumpy.breakvault.viewmodel.MoveViewModel
import kotlinx.coroutines.launch

enum class GenerationMode {
    Random, Structured
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ComboGeneratorScreen(
    navController: NavController,
    moveViewModel: IMoveViewModel = viewModel<MoveViewModel>()
) {
    val allTags by moveViewModel.allTags.observeAsState(initial = emptyList())
    var selectedGeneratorTags by remember { mutableStateOf(setOf<Tag>()) }
    var generatedComboText by remember { mutableStateOf("") }
    var currentGeneratedMoves by remember { mutableStateOf<List<Move>>(emptyList()) }
    var selectedLength by remember { mutableStateOf<Int?>(null) }
    val lengthOptions = listOf(null, 2, 3, 4, 5)

    var showLengthWarningDialog by remember { mutableStateOf(false) }
    var warningDialogMessage by remember { mutableStateOf("") }
    var lengthDropdownExpanded by remember { mutableStateOf(false) }

    var currentMode by remember { mutableStateOf(GenerationMode.Random) }
    var structuredTagSequence by remember { mutableStateOf<List<Tag>>(emptyList()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        generatedComboText = context.getString(R.string.combo_generator_initial_text)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.combo_generator_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.common_back_button_description))
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentGeneratedMoves.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        moveViewModel.saveCombo("", currentGeneratedMoves.map { it.name })
                        scope.launch {
                            snackbarHostState.showSnackbar(message = context.getString(R.string.combo_generator_combo_saved_snackbar))
                        }
                    },
                    icon = { Icon(Icons.Filled.Save, contentDescription = stringResource(id = R.string.combo_generator_save_combo_icon_description)) },
                    text = { Text(stringResource(id = R.string.combo_generator_save_combo_button)) }
                )
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TabRow(selectedTabIndex = currentMode.ordinal) {
                GenerationMode.values().forEach { mode ->
                    Tab(
                        selected = currentMode == mode,
                        onClick = { currentMode = mode },
                        text = { Text(mode.name) }
                    )
                }
            }

            when (currentMode) {
                GenerationMode.Random -> {
                    RandomModeUI(allTags, selectedGeneratorTags, onTagsChange = { selectedGeneratorTags = it }, selectedLength, onLengthChange = { selectedLength = it }, lengthDropdownExpanded, onDropdownExpand = { lengthDropdownExpanded = it }, lengthOptions)
                }
                GenerationMode.Structured -> {
                    StructuredModeUI(allTags, structuredTagSequence, onSequenceChange = { structuredTagSequence = it })
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    val comboMoves = when (currentMode) {
                        GenerationMode.Random -> {
                            // If no tags selected, use ALL tags
                            val tagsToUse = if (selectedGeneratorTags.isNotEmpty()) selectedGeneratorTags else allTags.toSet()
                            
                            if (tagsToUse.isNotEmpty()) {
                                moveViewModel.generateComboFromTags(tagsToUse, selectedLength)
                            } else {
                                generatedComboText = context.getString(R.string.combo_generator_no_tags_message)
                                emptyList()
                            }
                        }
                        GenerationMode.Structured -> {
                            if (structuredTagSequence.isNotEmpty()) {
                                moveViewModel.generateStructuredCombo(structuredTagSequence)
                            } else {
                                generatedComboText = context.getString(R.string.combo_generator_select_at_least_one_tag_message)
                                emptyList()
                            }
                        }
                    }

                    if (comboMoves.isNotEmpty()) {
                        currentGeneratedMoves = comboMoves
                        generatedComboText = comboMoves.joinToString(separator = "  ->  ") { it.name }
                        if (currentMode == GenerationMode.Random && selectedLength != null && selectedLength!! > comboMoves.size) {
                            warningDialogMessage = context.getString(R.string.combo_generator_length_warning_dialog_message, comboMoves.size)
                            showLengthWarningDialog = true
                        }
                    } else if ((currentMode == GenerationMode.Random && allTags.isNotEmpty()) || (currentMode == GenerationMode.Structured && structuredTagSequence.isNotEmpty())) {
                        currentGeneratedMoves = emptyList()
                        generatedComboText = context.getString(R.string.combo_generator_no_moves_found_message)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                // Enable if: (Random AND we have at least some tags in the system) OR (Structured AND sequence is not empty)
                enabled = (currentMode == GenerationMode.Random && allTags.isNotEmpty()) || (currentMode == GenerationMode.Structured && structuredTagSequence.isNotEmpty())
            ) {
                Text(stringResource(id = R.string.combo_generator_generate_combo_button))
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(stringResource(id = R.string.combo_generator_generated_combo_label), style = MaterialTheme.typography.titleMedium)
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
        }
    }

    if (showLengthWarningDialog) {
        AlertDialog(
            onDismissRequest = { showLengthWarningDialog = false },
            title = { Text(stringResource(id = R.string.combo_generator_length_warning_dialog_title)) },
            text = { Text(warningDialogMessage) },
            confirmButton = {
                TextButton(onClick = { showLengthWarningDialog = false }) {
                    Text(stringResource(id = R.string.common_ok))
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RandomModeUI(
    allTags: List<Tag>,
    selectedTags: Set<Tag>,
    onTagsChange: (Set<Tag>) -> Unit,
    selectedLength: Int?,
    onLengthChange: (Int?) -> Unit,
    lengthDropdownExpanded: Boolean,
    onDropdownExpand: (Boolean) -> Unit,
    lengthOptions: List<Int?>
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(id = R.string.combo_generator_select_tags_label), style = MaterialTheme.typography.titleMedium)

        if (allTags.isEmpty()) {
            Text(
                stringResource(id = R.string.combo_generator_no_tags_message),
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
                        selected = selectedTags.contains(tag),
                        onClick = { onTagsChange(if (selectedTags.contains(tag)) selectedTags - tag else selectedTags + tag) },
                        label = { Text(tag.name) },
                        leadingIcon = if (selectedTags.contains(tag)) {
                            { Icon(Icons.Filled.Done, stringResource(id = R.string.add_edit_move_selected_chip_description), Modifier.size(FilterChipDefaults.IconSize)) }
                        } else { null }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stringResource(id = R.string.combo_generator_number_of_moves_label), style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(
                expanded = lengthDropdownExpanded,
                onExpandedChange = onDropdownExpand,
                modifier = Modifier.width(180.dp)
            ) {
                OutlinedTextField(
                    value = selectedLength?.toString() ?: stringResource(id = R.string.combo_generator_random_length_option),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(id = R.string.combo_generator_combo_length_label)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lengthDropdownExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = lengthDropdownExpanded,
                    onDismissRequest = { onDropdownExpand(false) }
                ) {
                    lengthOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option?.toString() ?: stringResource(id = R.string.combo_generator_random_length_option)) },
                            onClick = {
                                onLengthChange(option)
                                onDropdownExpand(false)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StructuredModeUI(
    allTags: List<Tag>,
    tagSequence: List<Tag>,
    onSequenceChange: (List<Tag>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedTag by remember { mutableStateOf<Tag?>(null) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(id = R.string.combo_generator_define_structure_label), style = MaterialTheme.typography.titleMedium)

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedTag?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(id = R.string.combo_generator_add_tag_to_sequence_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                allTags.forEach { tag ->
                    DropdownMenuItem(
                        text = { Text(tag.name) },
                        onClick = {
                            selectedTag = tag
                            expanded = false
                        }
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { onSequenceChange(tagSequence + selectedTag!!) }, enabled = selectedTag != null && tagSequence.size < 5) {
                Text(stringResource(id = R.string.combo_generator_add_to_sequence_button))
            }

            if (tagSequence.isNotEmpty()) {
                FilledTonalButton(onClick = { onSequenceChange(tagSequence.dropLast(1)) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.Undo,
                        contentDescription = stringResource(id = R.string.combo_generator_undo_button_description)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(id = R.string.combo_generator_undo_button))
                }
            }
        }

        if (tagSequence.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(id = R.string.combo_generator_current_sequence_label), style = MaterialTheme.typography.titleMedium)
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = tagSequence.map { it.name }.joinToString(" -> "),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
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
