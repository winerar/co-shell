package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.ReportsUiState.Calendar
import app.coshell.reports.ReportsUiState.CalendarCell
import app.coshell.reports.components.CalendarCard
import app.coshell.reports.components.DayOperationsCard
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** «Календарь трат» (#553): the card of board `RepCalendar` on November 2026 (the 14th is the rent), the 18th chosen, today the 23rd. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h900dp-xhdpi", application = Application::class)
class ReportsCalendarScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private val levels = mapOf(2 to 1, 3 to 3, 5 to 3, 6 to 2, 7 to 4, 8 to 1, 9 to 2, 10 to 3, 12 to 2, 13 to 1, 14 to 4, 15 to 2, 16 to 3, 18 to 3, 19 to 2, 20 to 4, 21 to 1, 22 to 2, 23 to 3)
    private val calendar = Calendar(
        leadingBlanks = 6,
        cells = (1..30).map { d ->
            val level = levels[d] ?: 0
            CalendarCell(LocalDate(2026, 11, d), level, if (level > 0) (level * 25).toString() else null, today = d == 23, future = d > 23)
        },
        noSpendDays = 4,
    )

    @Test
    fun `calendar dark`() = shot("reports_calendar_dark", dark = true) { cards() }

    @Test
    fun `calendar light`() = shot("reports_calendar_light") { cards() }

    @Test @Config(qualifiers = "+en")
    fun `calendar english`() = shot("reports_calendar_en", dark = true) { cards() }

    @Composable
    private fun cards() = Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        CalendarCard(calendar, "USD", LocalDate(2026, 11, 18), onDay = {})
        DayOperationsCard(day = null, onTransaction = {})
    }

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { content() } } }
        compose.mainClock.advanceTimeBy(SETTLE_MS)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    private companion object {
        const val SETTLE_MS = 3_000L
    }
}
