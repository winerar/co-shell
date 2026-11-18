package app.coshell.accessibility

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.accounts.edit.AccountEditActions
import app.coshell.accounts.edit.AccountEditScreen
import app.coshell.accounts.edit.accountEditStressSample
import app.coshell.accounts.list.AccountsActions
import app.coshell.accounts.list.AccountsScreen
import app.coshell.accounts.list.accountsStressSample
import app.coshell.budgets.list.BudgetsActions
import app.coshell.budgets.list.BudgetsScreen
import app.coshell.budgets.list.budgetsStressSample
import app.coshell.categories.edit.CategoryEditActions
import app.coshell.categories.edit.CategoryEditScreen
import app.coshell.categories.edit.categoryEditStressSample
import app.coshell.categories.list.CategoriesActions
import app.coshell.categories.list.CategoriesScreen
import app.coshell.categories.list.categoriesStressSample
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.home.HomeScreen
import app.coshell.home.homeStressSample
import app.coshell.recurring.list.RecurringListActions
import app.coshell.recurring.list.RecurringListScreen
import app.coshell.recurring.list.recurringListStressSample
import app.coshell.reports.ReportsActions
import app.coshell.reports.ReportsScreen
import app.coshell.reports.reportsStressSample
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.transactions.edit.TransactionEditScreen
import app.coshell.transactions.edit.entrySheetStressSample
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * #380, `release-checklist.md` §3: every main screen (stress data, light and dark) keeps tap targets ≥ 48 dp and
 * labels on everything tappable. What the audit finds and is not fixed yet lives in `accessibility-baseline.txt`
 * as `screen | rule | label | size` — a line may only leave it, never join. Re-print: `-Daccessibility.print=true`.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "ru-w411dp-h891dp-xhdpi", application = Application::class)
class ScreensAccessibilityTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun `home`() = audit("home") { HomeScreen(homeStressSample) }

    @Test fun `accounts`() = audit("accounts") { AccountsScreen(accountsStressSample, AccountsActions()) }

    @Test fun `categories`() = audit("categories") { CategoriesScreen(categoriesStressSample, CategoriesActions()) }

    @Test fun `budgets`() = audit("budgets") { BudgetsScreen(budgetsStressSample, BudgetsActions()) }

    @Test fun `reports`() = audit("reports") { ReportsScreen(reportsStressSample, ReportsActions()) }

    @Test fun `account edit`() = audit("account-edit") { AccountEditScreen(accountEditStressSample, AccountEditActions()) }

    @Test fun `category edit`() = audit("category-edit") { CategoryEditScreen(categoryEditStressSample, CategoryEditActions()) }

    @Test fun `entry sheet`() = audit("entry-sheet") { TransactionEditScreen(entrySheetStressSample, TransactionEditActions()) }

    @Test fun `recurring`() = audit("recurring") { RecurringListScreen(recurringListStressSample, RecurringListActions()) }

    /** One screen per test: swapping the content inside one composition leaves the old semantics for a frame. */
    private fun audit(name: String, content: @Composable () -> Unit) {
        var density = 1f
        compose.mainClock.autoAdvance = false
        compose.setContent {
            density = LocalDensity.current.density
            CoShellTheme { Surface { content() } }
        }
        compose.mainClock.advanceTimeBy(3_000L)
        val found = SemanticsAudit.audit(compose.onRoot().fetchSemanticsNode(), density).map { "$name | $it" }.toSortedSet()
        val known = baseline().filter { it.startsWith("$name | ") }.toSet()
        if (System.getProperty("accessibility.print") == "true" || found != known) println("ACCESSIBILITY-FOUND $name\n" + found.joinToString("\n"))
        val fresh = found - known
        assertEquals("New accessibility violations on $name (fix them, do not add to the baseline):\n${fresh.joinToString("\n")}", emptySet<String>(), fresh)
    }

    private fun baseline(): Set<String> = File("src/test/resources/accessibility-baseline.txt")
        .takeIf { it.exists() }?.readLines().orEmpty().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }.toSet()
}
