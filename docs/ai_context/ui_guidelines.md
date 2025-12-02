# UI/UX Guidelines (UI Agent)

## Tech Stack
- **Framework:** Jetpack Compose.
- **Design System:** Material3 (`androidx.compose.material3`).
- **Icons:** Material Icons Extended.

## Common Patterns
1.  **Screen Structure:**
    - Always use `Scaffold` for top-level screens.
    - Use `TopAppBar` for navigation headers.
    - Use `SnackbarHost` for transient messages (success/error).

2.  **Lists & Layouts:**
    - Use `LazyColumn` for scrollable lists.
    - Use `FlowRow` (from `androidx.compose.foundation.layout`) for displaying chips/tags.
    - Use `Card` or `ListItem` for individual data entries.

3.  **Input Forms:**
    - `OutlinedTextField` for text input.
    - `FilterChip` or `InputChip` for tag selection.
    - `FloatingActionButton` (FAB) for primary creation actions.

4.  **State Management:**
    - Observe ViewModel LiveData using `observeAsState()`.
    - Hoist state to the Screen composable level; pass lambdas down to sub-components.
    - Use `LaunchedEffect` for one-time events (e.g., loading data, navigation side-effects).

## Theming
- Use `MaterialTheme.colorScheme` for colors (e.g., `primary`, `error`, `surface`).
- Use `MaterialTheme.typography` for text styles.
- Avoid hardcoded colors; defined in `ui/theme/Color.kt` if custom are needed.

## Example Snippet
```kotlin
@Composable
fun ExampleScreen(viewModel: MyViewModel = viewModel()) {
    val data by viewModel.data.observeAsState(emptyList())
    Scaffold(
        topBar = { TopAppBar(title = { Text("Title") }) }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(data) { item ->
                Text(text = item.name)
            }
        }
    }
}
```
