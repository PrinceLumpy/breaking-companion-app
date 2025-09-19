package com.example.combogenerator.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.combogenerator.data.transfer.AppDataExport
import com.example.combogenerator.ui.theme.ComboGeneratorTheme
import com.example.combogenerator.viewmodel.FakeMoveViewModel
import com.example.combogenerator.viewmodel.IMoveViewModel
import com.example.combogenerator.viewmodel.MoveViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    moveViewModel: IMoveViewModel = viewModel<MoveViewModel>()
) {
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val exportDataLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri: Uri? ->
            uri?.let {
                scope.launch {
                    try {
                        val appData = moveViewModel.getAppDataForExport()
                        if (appData != null) {
                            val jsonString =
                                Json.encodeToString(appData) // Assumes AppDataExport is @Serializable
                            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                                outputStream.write(jsonString.toByteArray())
                            }
                            snackbarHostState.showSnackbar("Data exported successfully to $uri")
                            Log.d("SettingsScreen", "Data exported to $uri")
                        } else {
                            snackbarHostState.showSnackbar("Error: Could not retrieve data for export.")
                        }
                    } catch (e: Exception) {
                        Log.e("SettingsScreen", "Error exporting data", e)
                        snackbarHostState.showSnackbar("Export failed: ${e.message}")
                    }
                }
            }
        }
    )

    val importDataLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                scope.launch {
                    try {
                        val jsonString =
                            context.contentResolver.openInputStream(it)?.use { inputStream ->
                                BufferedReader(InputStreamReader(inputStream)).readText()
                            }
                        if (jsonString != null) {
                            val appData =
                                Json.decodeFromString<AppDataExport>(jsonString) // Assumes AppDataExport is @Serializable
                            val success = moveViewModel.importAppData(appData)
                            if (success) {
                                snackbarHostState.showSnackbar("Data imported successfully!")
                                Log.d("SettingsScreen", "Data imported from $uri")
                            } else {
                                snackbarHostState.showSnackbar("Import failed. Check logs.")
                            }
                        } else {
                            snackbarHostState.showSnackbar("Error: Could not read data from file.")
                        }
                    } catch (e: Exception) {
                        Log.e("SettingsScreen", "Error importing data", e)
                        snackbarHostState.showSnackbar("Import failed: ${e.message}")
                    }
                }
            }
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsButton(text = "Export Data") {
                Log.d("SettingsScreen", "Export Data Clicked")
                exportDataLauncher.launch("combos_backup.json")
            }

            SettingsButton(text = "Import Data") {
                Log.d("SettingsScreen", "Import Data Clicked")
                importDataLauncher.launch(arrayOf("application/json"))
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { showResetConfirmDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reset Database")
            }
        }
    }

    if (showResetConfirmDialog) {
        ResetConfirmationDialog(
            onConfirm = {
                Log.d("SettingsScreen", "Database reset confirmed")
                scope.launch {
                    moveViewModel.resetDatabase()
                    snackbarHostState.showSnackbar("Database reset successfully.")
                }
                showResetConfirmDialog = false
            },
            onDismiss = {
                showResetConfirmDialog = false
            }
        )
    }
}

@Composable
private fun SettingsButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text)
    }
}

@Composable
private fun ResetConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Database?") },
        text = { Text("Are you sure you want to reset all data? This action cannot be undone.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reset")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    ComboGeneratorTheme {
        // Note: SAF launchers won't work in Preview, so interaction will be limited.
        SettingsScreen(moveViewModel = FakeMoveViewModel())
    }
}

@Preview
@Composable
fun ResetConfirmationDialogPreview() {
    ComboGeneratorTheme {
        ResetConfirmationDialog(onConfirm = {}, onDismiss = {})
    }
}
