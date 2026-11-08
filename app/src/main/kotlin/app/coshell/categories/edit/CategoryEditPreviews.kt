package app.coshell.categories.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme

@Preview(showBackground = true)
@Composable
private fun CategoryEditFolderPreview() {
    CoShellTheme {
        CategoryEditScreen(
            CategoryEditUiState(
                isNew = true,
                asFolder = true,
                name = "Машина",
                candidates = listOf(
                    CategoryEditUiState.Option("fuel", "Бензин"),
                    CategoryEditUiState.Option("taxi", "Такси"),
                ),
                childIds = setOf("fuel"),
            ),
            CategoryEditActions(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryEditExistingPreview() {
    CoShellTheme {
        CategoryEditScreen(
            CategoryEditUiState(
                isNew = false,
                name = "Бензин",
                typeLocked = true,
                parentId = "car",
                folders = listOf(CategoryEditUiState.Option("car", "Машина")),
            ),
            CategoryEditActions(),
        )
    }
}
