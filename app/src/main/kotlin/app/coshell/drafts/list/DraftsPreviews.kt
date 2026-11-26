package app.coshell.drafts.list

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.drafts.DraftMissing
import app.coshell.core.model.DraftCategorySource
import kotlinx.datetime.LocalDate

internal val DraftsPreviewDay = LocalDate(2026, 11, 21)

internal fun draftsPreviewSample(): List<DraftRowUi> = listOf(
    DraftRowUi("1", "Кофе", "Galicia", DraftsPreviewDay, "−3 500,00 ARS", null, "Кафе", DraftCategorySource.HISTORY, true, 0xFFF26E22, "cafe", 0xFFFFB300),
    DraftRowUi("2", "Купил хлеб", "Galicia", DraftsPreviewDay, null, DraftMissing.AMOUNT, null, null, false, 0xFFF26E22),
    DraftRowUi("3", "Netflix", "Galicia", DraftsPreviewDay, "−7 500,00 ARS", DraftMissing.CATEGORY, null, null, false, 0xFFF26E22),
    DraftRowUi("4", "Аптека", "Наличные", LocalDate(2026, 11, 20), "−4 200,00 ARS", null, "Здоровье", DraftCategorySource.DICTIONARY, true, null, "health", 0xFFE91E63),
)

@Preview(showBackground = true)
@Composable
private fun DraftsScreenPreview() {
    CoShellTheme { DraftsScreen(DraftsUiState(false, draftsPreviewSample(), DraftsPreviewDay), DraftsActions()) }
}

@Preview(showBackground = true)
@Composable
private fun DraftsScreenEmptyPreview() {
    CoShellTheme(darkTheme = true) { DraftsScreen(DraftsUiState(false, emptyList(), DraftsPreviewDay), DraftsActions()) }
}
