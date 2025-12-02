package com.princelumpy.breakvault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.princelumpy.breakvault.data.Tag
import com.princelumpy.breakvault.ui.screens.AddEditBattleComboScreen
import com.princelumpy.breakvault.ui.screens.AddEditMoveScreen
import com.princelumpy.breakvault.ui.screens.BattleScreen
import com.princelumpy.breakvault.ui.screens.BattleTagListScreen
import com.princelumpy.breakvault.ui.screens.ComboGeneratorScreen
import com.princelumpy.breakvault.ui.screens.CreateEditComboScreen
import com.princelumpy.breakvault.ui.screens.FlashcardScreen
import com.princelumpy.breakvault.ui.screens.MoveListScreen
import com.princelumpy.breakvault.ui.screens.MovesByTagScreen
import com.princelumpy.breakvault.ui.screens.SavedCombosScreen
import com.princelumpy.breakvault.ui.screens.SettingsScreen
import com.princelumpy.breakvault.ui.screens.TagListScreen
import com.princelumpy.breakvault.ui.theme.ComboGeneratorTheme

sealed class Screen(
    val route: String,
    @StringRes val labelResId: Int? = null,
    val icon: ImageVector? = null
) {
    // Top Level Screens (Show Bottom Bar)
    object MoveList : Screen("move_list", R.string.screen_label_moves, Icons.AutoMirrored.Filled.List)
    object SavedCombos : Screen("saved_combos", R.string.screen_label_saved_combos, Icons.Filled.Favorite)
    object TagList : Screen("tag_list", R.string.screen_label_tags, Icons.AutoMirrored.Filled.Label)
    object BattleArena : Screen("battle_arena", R.string.screen_label_battle_arena, Icons.Filled.FlashOn)
    object Settings : Screen("settings", R.string.screen_label_settings, Icons.Filled.Build)

    // Other Screens (Hide Bottom Bar)
    object ComboGenerator : Screen("combo_generator", R.string.screen_label_combo_generator, Icons.Filled.PlayArrow)
    object Flashcard : Screen("flashcard", R.string.screen_label_flashcards, Icons.Filled.School)
    object AddEditMove : Screen("add_edit_move")
    object CreateEditCombo : Screen("create_edit_combo")
    object MovesByTag : Screen("moves_by_tag")
    object AddEditBattleCombo : Screen("add_edit_battle_combo")
    object BattleTagList : Screen("battle_tag_list")
    
    fun withOptionalArgs(map: Map<String, String>): String {
        return buildString {
            append(route)
            if (map.isNotEmpty()) {
                append("?")
                append(map.entries.joinToString("&") { "${it.key}=${it.value}" })
            }
        }
    }

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}

// Unified Bottom Navigation Items
val bottomNavItems = listOf(
    Screen.MoveList,
    Screen.SavedCombos,
    Screen.TagList,
    Screen.BattleArena, // Added Battle here
    Screen.Settings
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
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Logic to hide bottom bar on detail screens
    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        val itemLabel = screen.labelResId?.let { stringResource(id = it) } ?: ""
                        val isSelected = currentDestination?.hierarchy?.any {
                            it.route == screen.route
                        } == true

                        NavigationBarItem(
                            icon = { Icon(screen.icon ?: Icons.Filled.Build, contentDescription = itemLabel) },
                            label = { Text(itemLabel) },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
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
            // --- Practice Mode Routes ---
            composable(Screen.MoveList.route) { MoveListScreen(navController = navController) }
            composable(Screen.SavedCombos.route) { SavedCombosScreen(navController = navController) }
            composable(Screen.TagList.route) { TagListScreen(navController = navController) }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(Screen.ComboGenerator.route) { ComboGeneratorScreen(navController = navController) }
            composable(Screen.Flashcard.route) { FlashcardScreen(navController = navController) }

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

            composable(
                route = Screen.CreateEditCombo.route + "?comboId={comboId}",
                arguments = listOf(navArgument("comboId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val comboId = backStackEntry.arguments?.getString("comboId")
                CreateEditComboScreen(navController = navController, comboId = comboId)
            }

            composable(
                route = Screen.MovesByTag.route + "/{tagId}/{tagName}",
                arguments = listOf(
                    navArgument("tagId") { type = NavType.StringType },
                    navArgument("tagName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val tagId = backStackEntry.arguments?.getString("tagId") ?: ""
                val tagName = backStackEntry.arguments?.getString("tagName") ?: ""
                MovesByTagScreen(navController = navController, tag = Tag(tagId, tagName))
            }

            // --- Battle Mode Routes ---
            composable(Screen.BattleArena.route) { BattleScreen(navController = navController) }
            composable(Screen.BattleTagList.route) { BattleTagListScreen(navController = navController) }
            
            composable(
                route = Screen.AddEditBattleCombo.route + "?comboId={comboId}",
                arguments = listOf(navArgument("comboId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val comboId = backStackEntry.arguments?.getString("comboId")
                AddEditBattleComboScreen(navController = navController, comboId = comboId)
            }
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
