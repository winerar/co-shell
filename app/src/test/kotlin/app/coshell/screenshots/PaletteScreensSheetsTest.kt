package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.accounts.list.AccountsActions
import app.coshell.accounts.list.AccountsScreen
import app.coshell.accounts.list.accountsStressSample
import app.coshell.budgets.list.BudgetsActions
import app.coshell.budgets.list.BudgetsScreen
import app.coshell.budgets.list.budgetsStressSample
import app.coshell.core.designsystem.theme.CoShellPalette
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.home.HomeScreen
import app.coshell.home.homeStressSample
import app.coshell.reports.ReportsActions
import app.coshell.reports.ReportsScreen
import app.coshell.reports.reportsStressSample
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.transactions.edit.TransactionEditScreen
import app.coshell.transactions.edit.entrySheetStressSample
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Contact sheets for #381 (`release-checklist.md` §4): main screens and the entry sheet in every palette × light / dark.
 * Not references — files go to `build/palette-sheets/` (CI artifact `palette-sheets`). Look for: chosen vs not chosen,
 * amounts and labels on their backgrounds, pale icons, the neon selection in light palettes.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class PaletteScreensSheetsTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun `home`() = sheets("home") { HomeScreen(homeStressSample) }

    @Test fun `accounts`() = sheets("accounts") { AccountsScreen(accountsStressSample, AccountsActions()) }

    @Test fun `budgets`() = sheets("budgets") { BudgetsScreen(budgetsStressSample, BudgetsActions()) }

    @Test fun `reports`() = sheets("reports") { ReportsScreen(reportsStressSample, ReportsActions()) }

    @Test fun `entry sheet`() = sheets("entry_sheet") { TransactionEditScreen(entrySheetStressSample, TransactionEditActions()) }

    /** One screen per test (a content swap inside one composition lags a frame); the theme is swapped by state. */
    private fun sheets(name: String, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        var palette by androidx.compose.runtime.mutableStateOf(CoShellPalette.Lime)
        var dark by androidx.compose.runtime.mutableStateOf(false)
        compose.setContent { CoShellTheme(darkTheme = dark, palette = palette) { Surface { content() } } }
        CoShellPalette.entries.forEach { p ->
            listOf(false, true).forEach { d ->
                compose.runOnIdle { palette = p; dark = d }
                compose.mainClock.advanceTimeByFrame()
                compose.waitForIdle()
                compose.mainClock.advanceTimeBy(3_000L)
                compose.waitForIdle()
                compose.onRoot().captureRoboImage(
                    "build/palette-sheets/${name}_${p.name.lowercase()}_${if (d) "dark" else "light"}.png",
                    roborazziOptions = RoborazziOptions(taskType = RoborazziTaskType.Record),
                )
            }
        }
    }
}
