package com.example.combogenerator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build // Added for Settings
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings // Current icon for TagList
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.navArgument
import com.example.combogenerator.ui.screens.AddEditMoveScreen
import com.example.combogenerator.ui.screens.ComboGeneratorScreen
import com.example.combogenerator.ui.screens.FlashcardScreen
import com.example.combogenerator.ui.screens.MoveListScreen
import com.example.combogenerator.ui.screens.SavedCombosScreen
import com.example.combogenerator.ui.screens.SettingsScreen // Will be created
import com.example.combogenerator.ui.screens.TagListScreen
import com.example.combogenerator.ui.theme.ComboGeneratorTheme

sealed class Screen(val route: String, val label: String? = null, val icon: ImageVector? = null) {
    object MoveList : Screen("move_list", "Moves", Icons.AutoMirrored.Filled.List)
    object SavedCombos : Screen("saved_combos", "Saved", Icons.Filled.Favorite)
    object TagList :
        Screen("tag_list", "Tags", Icons.Filled.Settings) // Existing Settings icon for Tags

    object Settings : Screen("settings", "Settings", Icons.Filled.Build) // New Settings screen

    object AddEditMove : Screen("add_edit_move", "Add/Edit Move")
    object ComboGenerator : Screen("combo_generator", "Generator", Icons.Filled.PlayArrow)
    object Flashcard : Screen("flashcard", "Flashcards", Icons.Filled.AddCircle)

    fun withOptionalArgs(map: Map<String, String>): String {
        return buildString {
            append(route)
            if (map.isNotEmpty()) {
                append("?")
                append(map.entries.joinToString("&") { "${it.key}=${it.value}" })
            }
        }
    }
}

val bottomNavItems = listOf(
    Screen.MoveList,
    Screen.SavedCombos,
    Screen.TagList,
    Screen.Settings // Added Settings to bottom nav
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComboGeneratorTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { screen ->
                    screen.icon?.let {
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { screen.label?.let { Text(it) } },
                            selected = currentDestination?.hierarchy?.any {
                                it.route == screen.route || currentDestination.route?.startsWith(
                                    screen.route + "?"
                                ) == true
                            } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = screen != Screen.MoveList
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.MoveList.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.MoveList.route) { MoveListScreen(navController = navController) }
            composable(Screen.SavedCombos.route) { SavedCombosScreen() }
            composable(Screen.TagList.route) { TagListScreen() }
            composable(Screen.Settings.route) { SettingsScreen() } // Added route for SettingsScreen

            composable(
                route = Screen.AddEditMove.route + "?moveId={moveId}",
                arguments = listOf(navArgument("moveId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val moveId = backStackEntry.arguments?.getString("moveId")
                AddEditMoveScreen(navController = navController, moveId = moveId)
            }
            composable(Screen.ComboGenerator.route) { ComboGeneratorScreen(navController = navController) }
            composable(Screen.Flashcard.route) { FlashcardScreen(navController = navController) }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComboGeneratorTheme {
        MainAppScreen()
    }
}
