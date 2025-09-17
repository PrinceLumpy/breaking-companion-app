package com.example.combogenerator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings // Using Settings as a placeholder for TagList
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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.combogenerator.ui.theme.ComboGeneratorTheme

// 1. Define Navigation Routes
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object MoveList : Screen("move_list", "Moves", Icons.Filled.List)
    object SavedCombos : Screen("saved_combos", "Saved", Icons.Filled.Favorite)
    object TagList : Screen("tag_list", "Tags", Icons.Filled.Settings) // Placeholder icon
    // Add other screens here as needed, e.g., AddEditMove, ComboGenerator, Flashcard
}

val bottomNavItems = listOf(
    Screen.MoveList,
    Screen.SavedCombos,
    Screen.TagList,
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
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.MoveList.route, // Your PRD mentions Move List as landing
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.MoveList.route) { MoveListScreen(navController = navController) }
            composable(Screen.SavedCombos.route) { SavedCombosScreen(navController = navController) }
            composable(Screen.TagList.route) { TagListScreen(navController = navController) }
            // Define other composables for AddEditMove, ComboGenerator, etc.
            // For example:
            // composable(Screen.AddEditMove.route + "/{moveId}?") { backStackEntry ->
            //     val moveId = backStackEntry.arguments?.getString("moveId")
            //     AddEditMoveScreen(navController = navController, moveId = moveId)
            // }
        }
    }
}

// 2. Create Placeholder Screen Composables
@Composable
fun MoveListScreen(navController: androidx.navigation.NavController) {
    // PRD: Landing screen (Move List): List of moves as cards, Button: “Add Move”, Button: “Generate Combo”
    Text(text = "Move List Screen - TODO: Implement Cards, Add Move Button, Generate Combo Button")
    // Example navigation:
    // Button(onClick = { navController.navigate(Screen.AddEditMove.route) }) { Text("Add Move") }
}

@Composable
fun SavedCombosScreen(navController: androidx.navigation.NavController) {
    // PRD: List of saved combos, Delete option on each combo
    Text(text = "Saved Combos Screen - TODO: Implement list of saved combos")
}

@Composable
fun TagListScreen(navController: androidx.navigation.NavController) {
    // PRD: List of all user tags, Click tag to view associated moves, Buttons: Edit tag name, Delete tag
    Text(text = "Tag List Screen - TODO: Implement list of tags, edit/delete functionality")
}

// --- AddEditMoveScreen (Example of a screen that might take arguments) ---
// @Composable
// fun AddEditMoveScreen(navController: androidx.navigation.NavController, moveId: String?) {
//    Text(text = if (moveId == null) "Add New Move" else "Edit Move: $moveId")
// }

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ComboGeneratorTheme {
        MainAppScreen()
    }
}
