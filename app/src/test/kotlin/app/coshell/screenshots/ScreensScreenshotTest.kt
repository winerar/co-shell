package app.coshell.screenshots

import app.coshell.more.MoreActions
import app.coshell.more.MoreScreen
import app.coshell.more.MoreState
import app.coshell.appearance.AppearanceActions
import app.coshell.appearance.AppearanceScreen
import app.coshell.appearance.AppearanceState
import app.coshell.core.domain.settings.ThemeMode
import app.coshell.backup.BackupActions
import app.coshell.backup.BackupScreen
import app.coshell.backup.BackupUiState
import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import app.coshell.accounts.edit.AccountEditActions
import app.coshell.accounts.edit.AccountEditScreen
import app.coshell.accounts.edit.accountEditPreviewSample
import app.coshell.accounts.list.AccountsActions
import app.coshell.accounts.list.AccountsScreen
import app.coshell.accounts.list.accountsPreviewSample
import app.coshell.budgets.list.BudgetsActions
import app.coshell.budgets.list.BudgetsScreen
import app.coshell.budgets.list.budgetsPreviewSample
import app.coshell.categories.edit.CategoryEditActions
import app.coshell.categories.edit.CategoryEditScreen
import app.coshell.categories.edit.categoryEditPreviewSample
import app.coshell.categories.list.CategoriesActions
import app.coshell.categories.list.CategoriesScreen
import app.coshell.categories.list.categoriesPreviewSample
import app.coshell.core.designsystem.theme.CoShellTheme
import app.coshell.home.HomeScreen
import app.coshell.home.HomeUiState
import app.coshell.home.homePreviewSample
import app.coshell.transactions.list.ListFilter
import app.coshell.transactions.list.components.AccountsChoices
import app.coshell.transactions.list.components.CategoriesChoices
import app.coshell.transactions.list.components.PeriodChoices
import app.coshell.transactions.list.transactionsFilterChoices
import app.coshell.transactions.list.transactionsFilteredSample
import app.coshell.transactions.list.TransactionsScreen
import app.coshell.transactions.list.TransactionsUiState
import app.coshell.transactions.list.transactionsPreviewSample
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.datetime.LocalDate
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

    /** Board `HomeAttention`: a missing rate turns the second tile into «Внимание». */
    @Test
    fun `home attention dark`() = shot("home_attention_dark", dark = true) {
        HomeScreen(homePreviewSample.copy(attention = HomeUiState.Attention(missingRates = listOf("VES"), uncategorized = 2)))
    }

    @Test
    fun `transactions light`() = shot("transactions_light") { TransactionsScreen(transactionsPreviewSample) }

    @Test
    fun `transactions dark`() = shot("transactions_dark", dark = true) { TransactionsScreen(transactionsPreviewSample) }

    /** Board `Fb141A` (#141): set filters are lit chips with ✕, the total (4А) under them. */
    @Test
    fun `transactions filtered dark`() = shot("transactions_filtered_dark", dark = true) { TransactionsScreen(transactionsFilteredSample) }

    @Test
    fun `transactions filtered light`() = shot("transactions_filtered_light") { TransactionsScreen(transactionsFilteredSample) }

    /** Board `Fb141AAmount`: how an amount query was understood. */
    @Test
    fun `transactions amount search dark`() = shot("transactions_amount_dark", dark = true) {
        TransactionsScreen(
            transactionsPreviewSample.copy(
                filter = ListFilter(query = "3000-5000"),
                total = TransactionsUiState.Total(38, "≈ -161,20 USD", "-162 900,00 ARS\n-9 600,00 AMD", TransactionsUiState.Kind.EXPENSE),
            ),
        )
    }

    /** Sheets of #141 (bodies; in the app they sit in a `CoShellSheet`). */
    @Test
    fun `filter categories sheet dark`() = shot("filter_categories_dark", dark = true) {
        Column { CategoriesChoices(transactionsFilteredSample.filter, transactionsFilterChoices) {} }
    }

    @Test
    fun `filter period sheet light`() = shot("filter_period_light") {
        Column { PeriodChoices(transactionsFilteredSample.filter, LocalDate(2026, 11, 12)) {} }
    }

    @Test
    fun `filter accounts sheet dark`() = shot("filter_accounts_dark", dark = true) {
        Column { AccountsChoices(ListFilter(accountIds = setOf("galicia", "usdt"), who = ListFilter.Who.Member("anya")), transactionsFilterChoices) {} }
    }

    @Test
    fun `accounts light`() = shot("accounts_light") { AccountsScreen(accountsPreviewSample, AccountsActions()) }

    @Test
    fun `accounts dark`() = shot("accounts_dark", dark = true) { AccountsScreen(accountsPreviewSample, AccountsActions()) }

    @Test
    fun `budgets light`() = shot("budgets_light") { BudgetsScreen(budgetsPreviewSample, BudgetsActions()) }

    @Test
    fun `budgets dark`() = shot("budgets_dark", dark = true) { BudgetsScreen(budgetsPreviewSample, BudgetsActions()) }

    @Test
    fun `categories light`() = shot("categories_light") { CategoriesScreen(categoriesPreviewSample, CategoriesActions()) }

    @Test
    fun `categories dark`() = shot("categories_dark", dark = true) { CategoriesScreen(categoriesPreviewSample, CategoriesActions()) }

    @Test
    fun `category edit light`() = shot("category_edit_light") { CategoryEditScreen(categoryEditPreviewSample, CategoryEditActions()) }

    @Test
    fun `category edit dark`() = shot("category_edit_dark", dark = true) { CategoryEditScreen(categoryEditPreviewSample, CategoryEditActions()) }

    @Test
    fun `account edit light`() = shot("account_edit_light") { AccountEditScreen(accountEditPreviewSample, AccountEditActions()) }

    @Test
    fun `account edit dark`() = shot("account_edit_dark", dark = true) { AccountEditScreen(accountEditPreviewSample, AccountEditActions()) }

    @Test
    fun `more light`() = shot("more_light") { MoreScreen(MoreActions(), moreSample) }

    @Test
    fun `more update dark`() = shot("more_update_dark", dark = true) { MoreScreen(MoreActions(), moreSample.copy(availableBuild = 248)) }

    @Test
    fun `appearance dark`() = shot("appearance_dark", dark = true) { AppearanceScreen(AppearanceState(ThemeMode.DARK), AppearanceActions()) }

    @Test
    fun `backup dark`() = shot("backup_dark", dark = true) { BackupScreen(BackupUiState(), BackupActions()) }

    /** The clock is wound past every entrance animation (blocks cascade in), so each shot is the settled screen. */
    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        // Surface: editors have no Scaffold, in the app the window gives them the background.
        compose.setContent { CoShellTheme(darkTheme = dark) { Surface { content() } } }
        compose.mainClock.advanceTimeBy(SETTLE_MS)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    private companion object {
        const val SETTLE_MS = 3_000L
        val moreSample = MoreState("0.1.0", 247, mainCurrency = "USD", appearance = "Вольт · тёмная")
    }
}
