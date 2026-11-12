package app.coshell.screenshots

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.accounts.list.AccountsActions
import app.coshell.accounts.list.AccountsScreen
import app.coshell.accounts.list.accountsPreviewSample
import app.coshell.budgets.list.BudgetsActions
import app.coshell.budgets.list.BudgetsScreen
import app.coshell.budgets.list.budgetsPreviewSample
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.home.HomeScreen
import app.coshell.home.homePreviewSample
import app.coshell.transactions.list.TransactionsScreen
import app.coshell.transactions.list.transactionsPreviewSample
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot references of the key screens (#226) on the preview samples (no Room, Hilt or network),
 * light and dark, Russian locale. A diff means the screen looks different — on purpose →
 * `scripts/agent/agent.sh shots` and attach the new PNG to the PR; by accident → fix the code.
 * References: `app/src/test/screenshots/`. Keep the set small: ~1.5 s per shot.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
// A plain Application: CoShellApp would open Room (native SQLite) and schedule work at start.
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class ScreensScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `home light`() = shot("home_light") { HomeScreen(homePreviewSample) }

    @Test
    fun `home dark`() = shot("home_dark", dark = true) { HomeScreen(homePreviewSample) }

    @Test
    fun `transactions light`() = shot("transactions_light") { TransactionsScreen(transactionsPreviewSample) }

    @Test
    fun `transactions dark`() = shot("transactions_dark", dark = true) { TransactionsScreen(transactionsPreviewSample) }

    @Test
    fun `accounts light`() = shot("accounts_light") { AccountsScreen(accountsPreviewSample, AccountsActions()) }

    @Test
    fun `accounts dark`() = shot("accounts_dark", dark = true) { AccountsScreen(accountsPreviewSample, AccountsActions()) }

    @Test
    fun `budgets light`() = shot("budgets_light") { BudgetsScreen(budgetsPreviewSample, BudgetsActions()) }

    @Test
    fun `budgets dark`() = shot("budgets_dark", dark = true) { BudgetsScreen(budgetsPreviewSample, BudgetsActions()) }

    /** The clock is wound past every entrance animation (blocks cascade in), so each shot is the settled screen. */
    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent { CoShellTheme(darkTheme = dark) { content() } }
        compose.mainClock.advanceTimeBy(SETTLE_MS)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    private companion object {
        const val SETTLE_MS = 3_000L
    }
}
