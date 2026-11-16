package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import app.coshell.accounts.list.AccountsActions
import app.coshell.accounts.list.AccountsScreen
import app.coshell.accounts.list.accountsEmptySample
import app.coshell.accounts.list.accountsStressSample
import app.coshell.budgets.list.BudgetsActions
import app.coshell.budgets.list.BudgetsScreen
import app.coshell.budgets.list.budgetsStressSample
import app.coshell.categories.list.CategoriesActions
import app.coshell.categories.list.CategoriesScreen
import app.coshell.categories.list.categoriesStressSample
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.home.HomeScreen
import app.coshell.home.HomeUiState
import app.coshell.home.homeStressSample
import app.coshell.reports.ReportsActions
import app.coshell.reports.ReportsScreen
import app.coshell.reports.reportsStressSample
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Main screens under stress (#378, `release-checklist.md` §2): long names and words without spaces, billion-sized,
 * negative and zero amounts, JPY / KWD / BTC, 10+ currencies, 30-item lists and empty states. Each screen light at
 * 411 dp and dark at 320 dp with font ×1.3. Rule: text ends in «…» or wraps, nothing overlaps, amounts are not cut.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class ScreensStressScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun `home stress light`() = shot("home_stress_light") { HomeScreen(homeStressSample) }

    @Test @Config(qualifiers = NARROW)
    fun `home stress narrow dark`() = shot("home_stress_narrow_dark", dark = true, fontScale = 1.3f) { HomeScreen(homeStressSample) }

    @Test fun `home empty light`() = shot("home_empty_light") { HomeScreen(HomeUiState()) }

    @Test fun `accounts stress light`() = shot("accounts_stress_light") { AccountsScreen(accountsStressSample, AccountsActions()) }

    @Test @Config(qualifiers = NARROW)
    fun `accounts stress narrow dark`() = shot("accounts_stress_narrow_dark", dark = true, fontScale = 1.3f) {
        AccountsScreen(accountsStressSample, AccountsActions())
    }

    @Test fun `accounts empty dark`() = shot("accounts_empty_dark", dark = true) { AccountsScreen(accountsEmptySample, AccountsActions()) }

    @Test fun `categories stress light`() = shot("categories_stress_light") { CategoriesScreen(categoriesStressSample, CategoriesActions()) }

    @Test @Config(qualifiers = NARROW)
    fun `categories stress narrow dark`() = shot("categories_stress_narrow_dark", dark = true, fontScale = 1.3f) {
        CategoriesScreen(categoriesStressSample, CategoriesActions())
    }

    @Test fun `budgets stress light`() = shot("budgets_stress_light") { BudgetsScreen(budgetsStressSample, BudgetsActions()) }

    @Test @Config(qualifiers = NARROW)
    fun `budgets stress narrow dark`() = shot("budgets_stress_narrow_dark", dark = true, fontScale = 1.3f) {
        BudgetsScreen(budgetsStressSample, BudgetsActions())
    }

    @Test fun `reports stress light`() = shot("reports_stress_light") { ReportsScreen(reportsStressSample, ReportsActions()) }

    @Test @Config(qualifiers = NARROW)
    fun `reports stress narrow dark`() = shot("reports_stress_narrow_dark", dark = true, fontScale = 1.3f) {
        ReportsScreen(reportsStressSample, ReportsActions())
    }

    private fun shot(name: String, dark: Boolean = false, fontScale: Float = 1f, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                CoShellTheme(darkTheme = dark) { Surface { content() } }
            }
        }
        compose.mainClock.advanceTimeBy(3_000L)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    private companion object {
        const val NARROW = "ru-w320dp-h720dp-xhdpi"
    }
}
