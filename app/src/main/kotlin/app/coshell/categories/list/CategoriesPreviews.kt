package app.coshell.categories.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme

@Preview(showBackground = true)
@Composable
private fun CategoriesScreenPreview() = CoShellTheme { CategoriesScreen(categoriesPreviewSample, CategoriesActions()) }

/** Also the screenshot reference (`ScreensScreenshotTest`): an open folder, a plain category, the archive. */
internal val categoriesPreviewSample =
    CategoriesUiState(
        groups = listOf(
            CategoriesUiState.Group(
                CategoriesUiState.Item("car", "Машина", 0xFF2196F3, "car"),
                children = listOf(
                    CategoriesUiState.Item("fuel", "Бензин", 0xFFFFC107, "fuel"),
                    CategoriesUiState.Item("parking", "Парковка", null),
                ),
                expanded = true,
            ),
            CategoriesUiState.Group(CategoriesUiState.Item("food", "Продукты", 0xFF4CAF50, "food"), emptyList(), false),
        ),
        archived = listOf(CategoriesUiState.Item("old", "Старое", null)),
    )
