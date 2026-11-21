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
        DraftRowUi("1", "Кофе", "Galicia", today, "−3 500,00 ARS", null, "Кафе", DraftCategorySource.HISTORY, true),
        DraftRowUi("2", "Купил хлеб", "Galicia", today, null, DraftMissing.AMOUNT, null, null, false),
        DraftRowUi("3", "Netflix", "Galicia", today, "−7 500,00 ARS", DraftMissing.CATEGORY, null, null, false),
        DraftRowUi("4", "Аптека", "Наличные", LocalDate(2026, 11, 20), "−4 200,00 ARS", null, "Здоровье", DraftCategorySource.AI, true),
    )

    @Test fun `note dark`() = shot("notes_dark", dark = true) { NotesScreen(note, NotesActions()) }

    @Test fun `note light`() = shot("notes_light") { NotesScreen(note, NotesActions()) }

    @Test fun `drafts dark`() = shot("drafts_dark", dark = true) { DraftsScreen(DraftsUiState(false, rows, today), DraftsActions()) }

    @Test fun `drafts light`() = shot("drafts_light") { DraftsScreen(DraftsUiState(false, rows, today), DraftsActions()) }

    @Test fun `drafts empty dark`() = shot("drafts_empty_dark", dark = true) { DraftsScreen(DraftsUiState(false, emptyList(), today), DraftsActions()) }

    @Test fun `drafts empty light`() = shot("drafts_empty_light") { DraftsScreen(DraftsUiState(false, emptyList(), today), DraftsActions()) }

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { content() } } }
        compose.mainClock.advanceTimeBy(3_000L)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }
}
