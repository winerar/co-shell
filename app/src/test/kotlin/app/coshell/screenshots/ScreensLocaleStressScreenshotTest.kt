package app.coshell.screenshots

import app.coshell.settings.SettingsActions
import app.coshell.settings.SettingsPreviewState
import app.coshell.settings.SettingsScreen
import android.app.Application
import kotlinx.datetime.LocalDate
import app.coshell.drafts.notes.NotesUiState
import app.coshell.drafts.notes.NotesScreen
import app.coshell.drafts.notes.NotesActions
import app.coshell.drafts.list.DraftsUiState
import app.coshell.drafts.list.DraftsScreen
import app.coshell.drafts.list.DraftsActions
import app.coshell.drafts.list.DraftRowUi
import app.coshell.core.model.DraftCategorySource
import app.coshell.core.domain.drafts.DraftMissing
import androidx.activity.ComponentActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import app.coshell.accounts.edit.AccountEditActions
import app.coshell.accounts.edit.AccountEditScreen
import app.coshell.accounts.edit.accountEditStressSample
import app.coshell.accounts.list.AccountsActions
import app.coshell.accounts.list.AccountsScreen
import app.coshell.accounts.list.accountsEmptySample
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
import app.coshell.home.HomeUiState
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
import app.coshell.transactions.edit.paymentSheetStressSample
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Stress screens in the longest language and at the biggest font (#379, `release-checklist.md` §2): Spanish at 320 dp
 * with font ×1.3 (light) and Russian at 320 dp with font ×2.0 (dark). Rule: text ends in «…» or wraps, nothing overlaps,
 * amounts are not cut. The same samples as [ScreensStressScreenshotTest].
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = ES, application = Application::class)
class ScreensLocaleStressScreenshotTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun `home es`() = es("home") { HomeScreen(homeStressSample) }
    @Test fun `accounts es`() = es("accounts") { AccountsScreen(accountsStressSample, AccountsActions()) }
    @Test fun `categories es`() = es("categories") { CategoriesScreen(categoriesStressSample, CategoriesActions()) }
    @Test fun `budgets es`() = es("budgets") { BudgetsScreen(budgetsStressSample, BudgetsActions()) }
    @Test fun `reports es`() = es("reports") { ReportsScreen(reportsStressSample, ReportsActions()) }
    @Test fun `recurring es`() = es("recurring") { RecurringListScreen(recurringListStressSample.copy(paused = emptyList()), RecurringListActions()) }
    @Test fun `entry sheet es`() = es("entry_sheet") { TransactionEditScreen(entrySheetStressSample, TransactionEditActions()) }
    @Test fun `account edit es`() = es("account_edit") { AccountEditScreen(accountEditStressSample, AccountEditActions()) }
    @Test fun `category edit es`() = es("category_edit") { CategoryEditScreen(categoryEditStressSample, CategoryEditActions()) }
    @Test fun `notes es`() = es("notes") { NotesScreen(notesStressSample, NotesActions()) }
    @Test fun `drafts es`() = es("drafts") { DraftsScreen(draftsStressSample, DraftsActions()) }
    @Test fun `settings es`() = es("settings") { SettingsScreen(SettingsPreviewState, SettingsActions()) }

    @Test @Config(qualifiers = RU)
    fun `home x2`() = x2("home") { HomeScreen(homeStressSample) }
    @Test @Config(qualifiers = RU)
    fun `accounts x2`() = x2("accounts") { AccountsScreen(accountsStressSample, AccountsActions()) }
    @Test @Config(qualifiers = RU)
    fun `categories x2`() = x2("categories") { CategoriesScreen(categoriesStressSample, CategoriesActions()) }
    @Test @Config(qualifiers = RU)
    fun `budgets x2`() = x2("budgets") { BudgetsScreen(budgetsStressSample, BudgetsActions()) }
    @Test @Config(qualifiers = RU)
    fun `reports x2`() = x2("reports") { ReportsScreen(reportsStressSample, ReportsActions()) }
    @Test @Config(qualifiers = RU)
    fun `recurring x2`() = x2("recurring") { RecurringListScreen(recurringListStressSample.copy(paused = emptyList()), RecurringListActions()) }
    @Test @Config(qualifiers = RU)
    fun `entry sheet x2`() = x2("entry_sheet") { TransactionEditScreen(entrySheetStressSample, TransactionEditActions()) }
    @Test @Config(qualifiers = RU)
    fun `account edit x2`() = x2("account_edit") { AccountEditScreen(accountEditStressSample, AccountEditActions()) }
    @Test @Config(qualifiers = RU)
    fun `category edit x2`() = x2("category_edit") { CategoryEditScreen(categoryEditStressSample, CategoryEditActions()) }
    @Test @Config(qualifiers = RU)
    fun `notes x2`() = x2("notes") { NotesScreen(notesStressSample, NotesActions()) }
    @Test @Config(qualifiers = RU)
    fun `drafts x2`() = x2("drafts") { DraftsScreen(draftsStressSample, DraftsActions()) }
    @Test @Config(qualifiers = RU)
    fun `settings x2`() = x2("settings") { SettingsScreen(SettingsPreviewState, SettingsActions()) }

    private fun es(screen: String, content: @Composable () -> Unit) = shot("${screen}_stress_es_narrow_light", false, 1.3f, content)

    private fun x2(screen: String, content: @Composable () -> Unit) = shot("${screen}_stress_x2_narrow_dark", true, 2f, content)

    private fun shot(name: String, dark: Boolean, fontScale: Float, content: @Composable () -> Unit) {
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
}

private val notesStressSample = NotesUiState("кофе 3500\nтакси 2000\nхлеб 850 карта\nвчера аптека 4200", 4)
private val draftsToday = LocalDate(2026, 11, 21)
private val draftsStressSample = DraftsUiState(
    false,
    listOf(
        DraftRowUi("1", "Кофе", "Galicia", draftsToday, "−3 500,00 ARS", null, "Кафе", DraftCategorySource.HISTORY, true, 0xFFF26E22, "cafe", 0xFFFFB300),
        DraftRowUi("2", "Купил хлеб в магазине у дома по дороге с работы", "Galicia", draftsToday, null, DraftMissing.AMOUNT, null, null, false, 0xFFF26E22),
        DraftRowUi("3", "Netflix", "Galicia", draftsToday, "−7 500,00 ARS", DraftMissing.CATEGORY, null, null, false, 0xFFF26E22),
    ),
    draftsToday,
)

private const val ES = "es-w320dp-h720dp-xhdpi"
private const val RU = "ru-w320dp-h720dp-xhdpi"
