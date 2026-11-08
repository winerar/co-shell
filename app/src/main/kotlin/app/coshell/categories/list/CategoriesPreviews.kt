package app.coshell.categories.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme

@Preview(showBackground = true)
@Composable
private fun CategoriesScreenPreview() {
    CoShellTheme {
        CategoriesScreen(
            state = CategoriesUiState(
                groups = listOf(
                    CategoriesUiState.Group(
                        CategoriesUiState.Item("car", "Машина", 0xFF2196F3),
                        children = listOf(
                            CategoriesUiState.Item("fuel", "Бензин", 0xFFFFC107),
                            CategoriesUiState.Item("parking", "Парковка", null),
                        ),
                        expanded = true,
                    ),
                    CategoriesUiState.Group(CategoriesUiState.Item("food", "Продукты", 0xFF4CAF50), emptyList(), false),
                ),
                archived = listOf(CategoriesUiState.Item("old", "Старое", null)),
            ),
            actions = CategoriesActions(),
        )
    }
}
