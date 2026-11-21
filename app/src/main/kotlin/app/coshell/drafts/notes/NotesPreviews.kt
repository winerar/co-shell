package app.coshell.drafts.notes

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme

@Preview(showBackground = true)
@Composable
private fun NotesScreenPreview() {
    CoShellTheme {
        NotesScreen(NotesUiState("кофе 3500\nтакси 2000\nхлеб 850 карта\nвчера аптека 4200", 4), NotesActions())
    }
}

@Preview(showBackground = true)
@Composable
private fun NotesScreenEmptyPreview() {
    CoShellTheme(darkTheme = true) { NotesScreen(NotesUiState(), NotesActions()) }
}
