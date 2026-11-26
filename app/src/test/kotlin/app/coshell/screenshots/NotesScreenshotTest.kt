package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.domain.drafts.DraftMissing
import app.coshell.core.model.DraftCategorySource
import app.coshell.debts.list.DebtsActions
import app.coshell.debts.list.DebtsScreen
import app.coshell.debts.list.DebtsUiState
import app.coshell.debts.list.debtsPreviewState
import app.coshell.drafts.list.DraftRowUi
import app.coshell.drafts.list.DraftsActions
import app.coshell.drafts.list.DraftsScreen
import app.coshell.drafts.list.DraftsUiState
import app.coshell.drafts.notes.NotesActions
import app.coshell.drafts.notes.NotesScreen
import app.coshell.drafts.notes.NotesUiState
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** «Быстрые заметки» and «Черновики» (#586; boards Notes586Input, Notes586Unready, Notes586Empty2): same setup as [AiAddonScreenshotTest]. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class NotesScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private val today = LocalDate(2026, 11, 21)
    private val note = NotesUiState("кофе 3500\nтакси 2000\nхлеб 850 карта\nвчера аптека 4200", 4)
    private val rows = listOf(
        DraftRowUi("1", "Кофе", "Galicia", today, "−3 500,00 ARS", null, "Кафе", DraftCategorySource.HISTORY, true, 0xFFF26E22, "cafe", 0xFFFFB300),
        DraftRowUi("2", "Купил хлеб", "Galicia", today, null, DraftMissing.AMOUNT, null, null, false, 0xFFF26E22),
        DraftRowUi("3", "Netflix", "Galicia", today, "−7 500,00 ARS", DraftMissing.CATEGORY, null, null, false, 0xFFF26E22),
        DraftRowUi("4", "Аптека", "Наличные", LocalDate(2026, 11, 20), "−4 200,00 ARS", null, "Здоровье", DraftCategorySource.DICTIONARY, true, null, "health", 0xFFE91E63),
    )

    @Test fun `note dark`() = shot("notes_dark", dark = true) { NotesScreen(note, NotesActions()) }

    @Test fun `note light`() = shot("notes_light") { NotesScreen(note, NotesActions()) }

    @Test fun `drafts dark`() = shot("drafts_dark", dark = true) { DraftsScreen(DraftsUiState(false, rows, today), DraftsActions()) }

    @Test fun `drafts light`() = shot("drafts_light") { DraftsScreen(DraftsUiState(false, rows, today), DraftsActions()) }

    @Test fun `drafts delete dark`() = shot("drafts_delete_dark", dark = true) { DraftsScreen(deleting, DraftsActions()) }

    @Test fun `drafts delete light`() = shot("drafts_delete_light") { DraftsScreen(deleting, DraftsActions()) }

    /** Board `Notes628Delete`: the unready rows ticked to delete, the ready ones unticked. */
    private val deleting get() = DraftsUiState(false, rows.map { it.copy(checked = !it.ready) }, today, ticksTouched = true)

    @Test fun `drafts empty dark`() = shot("drafts_empty_dark", dark = true) { DraftsScreen(DraftsUiState(false, emptyList(), today), DraftsActions()) }

    @Test fun `drafts empty light`() = shot("drafts_empty_light") { DraftsScreen(DraftsUiState(false, emptyList(), today), DraftsActions()) }

    @Test fun `debts dark`() = shot("debts_dark", dark = true) { DebtsScreen(debtsPreviewState(), DebtsActions()) }

    @Test fun `debts light`() = shot("debts_light") { DebtsScreen(debtsPreviewState(), DebtsActions()) }

    @Test fun `debts empty dark`() = shot("debts_empty_dark", dark = true) { DebtsScreen(DebtsUiState(isLoading = false), DebtsActions()) }

    @Test fun `debts empty light`() = shot("debts_empty_light") { DebtsScreen(DebtsUiState(isLoading = false), DebtsActions()) }

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { content() } } }
        compose.mainClock.advanceTimeBy(3_000L)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }
}
