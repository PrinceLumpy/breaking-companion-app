package com.princelumpy.breakvault.ui.screens

import AppStyleDefaults
import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.princelumpy.breakvault.R
import com.princelumpy.breakvault.data.model.move.Move
import com.princelumpy.breakvault.data.model.move.MoveTag
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme
import com.princelumpy.breakvault.viewmodel.ComboGeneratorUiState
import com.princelumpy.breakvault.viewmodel.ComboGeneratorViewModel
import com.princelumpy.breakvault.viewmodel.GenerationMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ComboGeneratorScreen(
    navController: NavController,
    comboGeneratorViewModel: ComboGeneratorViewModel = viewModel()
) {
//    val uiState by comboGeneratorViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

//    LaunchedEffect(uiState.snackbarMessage) {
//        uiState.snackbarMessage?.let {
//            snackbarHostState.showSnackbar(it)
//            comboGeneratorViewModel.onSnackbarMessageShown()
//        }
//    }

//    Scaffold(
//        snackbarHost = { SnackbarHost(snackbarHostState) },
//        topBar = {
//            TopAppBar(
//                title = { Text(stringResource(id = R.string.combo_generator_title)) },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(
//                            Icons.AutoMirrored.Filled.ArrowBack,
//                            contentDescription = stringResource(id = R.string.common_back_button_description)
//                        )
//                    }
//                }
//            )
//        },
//        floatingActionButton = {
//            if (uiState.currentGeneratedMoves.isNotEmpty()) {
//                ExtendedFloatingActionButton(
//                    onClick = { comboGeneratorViewModel.saveCombo() },
//                    icon = {
//                        Icon(
//                            Icons.Filled.Save,
//                            contentDescription = stringResource(id = R.string.combo_generator_save_combo_icon_description)
//                        )
//                    },
//                    text = { Text(stringResource(id = R.string.combo_generator_save_combo_button)) }
//                )
//            }
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .padding(paddingValues)
//                .padding(AppStyleDefaults.SpacingLarge)
//                .verticalScroll(rememberScrollState())
//                .fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
//        ) {
//            TabRow(selectedTabIndex = uiState.currentMode.ordinal) {
//                GenerationMode.entries.forEach { mode ->
//                    Tab(
//                        selected = uiState.currentMode == mode,
//                        onClick = { comboGeneratorViewModel.onModeChange(mode) },
//                        text = { Text(mode.name) }
//                    )
//                }
//            }

//            when (uiState.currentMode) {
//                GenerationMode.Random -> {
//                    RandomModeUI(
//                        allMoveTags = uiState.allTags,
//                        selectedMoveTags = uiState.selectedGeneratorMoveTags,
//                        onTagsChange = { comboGeneratorViewModel.onTagsChange(it) },
//                        selectedLength = uiState.selectedLength,
//                        onLengthChange = { comboGeneratorViewModel.onLengthChange(it) },
//                        lengthDropdownExpanded = uiState.lengthDropdownExpanded,
//                        onDropdownExpand = { comboGeneratorViewModel.onDropdownExpand(it) },
//                        lengthOptions = listOf(null) + (1..6).toList(),
//                        allowRepeats = uiState.allowRepeats,
//                        onAllowRepeatsChange = { comboGeneratorViewModel.onAllowRepeatsChange(it) }
//                    )
//                }
//
//                GenerationMode.Structured -> {
//                    StructuredModeUI(
//                        uiState.allTags,
//                        uiState.structuredMoveTagSequence,
//                        onSequenceChange = { comboGeneratorViewModel.onSequenceChange(it) })
//                }
//            }
//
//            Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))
//
//            Button(
//                onClick = { comboGeneratorViewModel.generateCombo() },
//                modifier = Modifier.fillMaxWidth(),
//                enabled = (uiState.currentMode == GenerationMode.Random && uiState.allTags.isNotEmpty()) || (uiState.currentMode == GenerationMode.Structured && uiState.structuredMoveTagSequence.isNotEmpty())
//            ) {
//                Text(stringResource(id = R.string.combo_generator_generate_combo_button))
//            }
//
//            Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))
//
//            Text(
//                stringResource(id = R.string.combo_generator_generated_combo_label),
//                style = MaterialTheme.typography.titleMedium
//            )
//            Card(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .defaultMinSize(minHeight = AppStyleDefaults.SpacingExtraLarge * 2)
//            ) {
//                Text(
//                    text = uiState.generatedComboText,
//                    modifier = Modifier.padding(AppStyleDefaults.SpacingLarge),
//                    style = MaterialTheme.typography.bodyLarge
//                )
//            }
//        }
//    }

//    if (uiState.showLengthWarningDialog) {
//        AlertDialog(
//            onDismissRequest = { comboGeneratorViewModel.onDismissLengthWarning() },
//            title = { Text(stringResource(id = R.string.combo_generator_length_warning_dialog_title)) },
//            text = { Text(uiState.warningDialogMessage) },
//            confirmButton = {
//                TextButton(onClick = { comboGeneratorViewModel.onDismissLengthWarning() }) {
//                    Text(stringResource(id = R.string.common_ok))
//                }
//            }
//        )
//    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RandomModeUI(
    allMoveTags: List<MoveTag>,
    selectedMoveTags: Set<MoveTag>,
    onTagsChange: (Set<MoveTag>) -> Unit,
    selectedLength: Int?,
    onLengthChange: (Int?) -> Unit,
    lengthDropdownExpanded: Boolean,
    onDropdownExpand: (Boolean) -> Unit,
    lengthOptions: List<Int?>,
    allowRepeats: Boolean,
    onAllowRepeatsChange: (Boolean) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingLarge)
    ) {
        Text(
            stringResource(id = R.string.combo_generator_select_tags_label),
            style = MaterialTheme.typography.titleMedium
        )

        if (allMoveTags.isEmpty()) {
            Text(
                stringResource(id = R.string.combo_generator_no_tags_message),
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium),
                verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingSmall)
            ) {
                allMoveTags.forEach { tag ->
                    FilterChip(
                        selected = selectedMoveTags.contains(tag),
                        onClick = { onTagsChange(if (selectedMoveTags.contains(tag)) selectedMoveTags - tag else selectedMoveTags + tag) },
                        label = { Text(tag.name) },
                        leadingIcon = if (selectedMoveTags.contains(tag)) {
                            {
                                Icon(
                                    Icons.Filled.Done,
                                    stringResource(id = R.string.add_edit_move_selected_chip_description),
                                    Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
            ) {
                Text(
                    text = stringResource(id = R.string.combo_generator_allow_repeats_label),
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = allowRepeats,
                    onCheckedChange = onAllowRepeatsChange
                )
            }

            ExposedDropdownMenuBox(
                expanded = lengthDropdownExpanded,
                onExpandedChange = onDropdownExpand,
                modifier = Modifier.width(AppStyleDefaults.SpacingExtraLarge * 4)
            ) {
                OutlinedTextField(
                    value = selectedLength?.toString()
                        ?: stringResource(id = R.string.combo_generator_random_length_option),
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
                            text = {
                                Text(
                                    option?.toString()
                                        ?: stringResource(id = R.string.combo_generator_random_length_option)
                                )
                            },
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
    allMoveTags: List<MoveTag>,
    moveTagSequence: List<MoveTag>,
    onSequenceChange: (List<MoveTag>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedMoveTag by remember { mutableStateOf<MoveTag?>(null) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium)
    ) {
        Text(
            stringResource(id = R.string.combo_generator_define_structure_label),
            style = MaterialTheme.typography.titleMedium
        )

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = selectedMoveTag?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(id = R.string.combo_generator_add_tag_to_sequence_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                allMoveTags.forEach { tag ->
                    DropdownMenuItem(
                        text = { Text(tag.name) },
                        onClick = {
                            selectedMoveTag = tag
                            expanded = false
                        }
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(AppStyleDefaults.SpacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onSequenceChange(moveTagSequence + selectedMoveTag!!) },
                enabled = selectedMoveTag != null && moveTagSequence.size < 10
            ) {
                Text(stringResource(id = R.string.combo_generator_add_to_sequence_button))
            }

            if (moveTagSequence.isNotEmpty()) {
                FilledTonalButton(onClick = { onSequenceChange(moveTagSequence.dropLast(1)) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.Undo,
                        contentDescription = stringResource(id = R.string.combo_generator_undo_button_description)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(id = R.string.combo_generator_undo_button))
                }
            }
        }

        if (moveTagSequence.isNotEmpty()) {
            Spacer(modifier = Modifier.height(AppStyleDefaults.SpacingSmall))
            Text(
                stringResource(id = R.string.combo_generator_current_sequence_label),
                style = MaterialTheme.typography.titleMedium
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = moveTagSequence.joinToString(" -> ") { it.name },
                    modifier = Modifier.padding(AppStyleDefaults.SpacingLarge)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ComboGeneratorScreenPreview() {
    ComboGeneratorTheme {
        ComboGeneratorScreen(
            navController = rememberNavController(),
//            comboGeneratorViewModel = fakeComboGeneratorViewModel()
        )
    }
}

@Composable
private fun fakeComboGeneratorViewModel(): ComboGeneratorViewModel {
    val application = LocalContext.current.applicationContext as Application
    return object : ComboGeneratorViewModel(application, forPreview = true) {
        private val _fakeUiState = MutableStateFlow(
            ComboGeneratorUiState(
                allTags = listOf(
                    MoveTag(UUID.randomUUID().toString(), "Footwork"),
                    MoveTag(UUID.randomUUID().toString(), "Freezes"),
                    MoveTag(UUID.randomUUID().toString(), "Power")
                ),
                generatedComboText = "Six Step -> Baby Freeze -> Windmill",
                currentGeneratedMoves = listOf(
                    Move(UUID.randomUUID().toString(), "Six Step"),
                    Move(UUID.randomUUID().toString(), "Baby Freeze"),
                    Move(UUID.randomUUID().toString(), "Windmill")
                )
            )
        )
        override val uiState: StateFlow<ComboGeneratorUiState> = _fakeUiState

        val allTags: LiveData<List<MoveTag>> = MutableLiveData(
            listOf(
                MoveTag(UUID.randomUUID().toString(), "Footwork"),
                MoveTag(UUID.randomUUID().toString(), "Freezes"),
                MoveTag(UUID.randomUUID().toString(), "Power")
            )
        )

        override fun onModeChange(mode: GenerationMode) {
            _fakeUiState.update { it.copy(currentMode = mode) }
        }

        override fun onTagsChange(tags: Set<MoveTag>) {
            _fakeUiState.update { it.copy(selectedGeneratorMoveTags = tags) }
        }

        override fun onLengthChange(length: Int?) {
            _fakeUiState.update { it.copy(selectedLength = length) }
        }

        override fun onDropdownExpand(expanded: Boolean) {
            _fakeUiState.update { it.copy(lengthDropdownExpanded = expanded) }
        }

        override fun onAllowRepeatsChange(allow: Boolean) {
            _fakeUiState.update { it.copy(allowRepeats = allow) }
        }

        override fun onSequenceChange(sequence: List<MoveTag>) {
            _fakeUiState.update { it.copy(structuredMoveTagSequence = sequence) }
        }

        override fun onSnackbarMessageShown() {
            _fakeUiState.update { it.copy(snackbarMessage = null) }
        }

        override fun onDismissLengthWarning() {
            _fakeUiState.update { it.copy(showLengthWarningDialog = false) }
        }

        override fun generateCombo() {
            _fakeUiState.update {
                it.copy(
                    generatedComboText = "Toprock -> CC -> Swipe",
                    currentGeneratedMoves = listOf(
                        Move(UUID.randomUUID().toString(), "Toprock"),
                        Move(UUID.randomUUID().toString(), "CC"),
                        Move(UUID.randomUUID().toString(), "Swipe")
                    )
                )
            }
        }

        override fun saveCombo() {
            _fakeUiState.update { it.copy(snackbarMessage = "Combo saved!") }
        }
    }
}
