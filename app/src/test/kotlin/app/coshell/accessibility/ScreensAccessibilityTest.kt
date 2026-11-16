package app.coshell.accessibility

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
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

    private val screens: List<Pair<String, @Composable () -> Unit>> = listOf(
        "home" to { HomeScreen(homeStressSample) },
        "accounts" to { AccountsScreen(accountsStressSample, AccountsActions()) },
        "categories" to { CategoriesScreen(categoriesStressSample, CategoriesActions()) },
        "budgets" to { BudgetsScreen(budgetsStressSample, BudgetsActions()) },
        "reports" to { ReportsScreen(reportsStressSample, ReportsActions()) },
        "account-edit" to { AccountEditScreen(accountEditStressSample, AccountEditActions()) },
        "category-edit" to { CategoryEditScreen(categoryEditStressSample, CategoryEditActions()) },
        "entry-sheet" to { TransactionEditScreen(entrySheetStressSample, TransactionEditActions()) },
        "recurring" to { RecurringListScreen(recurringListStressSample, RecurringListActions()) },
    )

    @Test fun `tap targets and labels match the baseline`() {
        var density = 1f
        var current by mutableIntStateOf(0)
        compose.mainClock.autoAdvance = false
        compose.setContent {
            density = LocalDensity.current.density
            CoShellTheme { Surface { key(current) { screens[current].second() } } }
        }
        val found = sortedSetOf<String>()
        screens.forEachIndexed { i, (name, _) ->
            compose.runOnIdle { current = i }
            compose.mainClock.advanceTimeBy(3_000L)
            val root = compose.onRoot().fetchSemanticsNode()
            found += SemanticsAudit.audit(root, density).map { "$name | $it" }
        }
        if (System.getProperty("accessibility.print") == "true" || found != baseline()) {
            println("ACCESSIBILITY-FOUND\n" + found.joinToString("\n"))
        }
        val fresh = found - baseline()
        assertEquals("New accessibility violations (fix them, do not add to the baseline):\n${fresh.joinToString("\n")}", emptySet<String>(), fresh)
    }

    private fun baseline(): Set<String> = File("src/test/resources/accessibility-baseline.txt")
        .takeIf { it.exists() }?.readLines().orEmpty().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }.toSet()
}
