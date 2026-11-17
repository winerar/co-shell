package app.coshell.screenshots

import app.coshell.more.MoreActions
import app.coshell.more.MoreScreen
import app.coshell.more.MoreState
import app.coshell.newmark.NewFeatures
import androidx.compose.runtime.CompositionLocalProvider
import app.coshell.core.designsystem.components.LocalCompactLists
import app.coshell.appearance.AppearanceActions
import app.coshell.settings.AccountsSettingsScreen
import app.coshell.settings.HomeDetailsScreen
import app.coshell.settings.HomeSettingsScreen
import app.coshell.core.domain.settings.HomeCard
import app.coshell.core.domain.settings.HomeLayout
import app.coshell.settings.DataSettingsScreen
import app.coshell.settings.OpsSettingsScreen
import app.coshell.settings.ReportsSettingsScreen
import app.coshell.settings.ScreensSettingsScreen
import app.coshell.core.domain.settings.ScreenOptions
import app.coshell.settings.InputSettingsScreen
import app.coshell.settings.MoneySettingsScreen
import app.coshell.settings.NoticeSettingsScreen
import app.coshell.settings.SettingsActions
import app.coshell.settings.SettingsScreen
import app.coshell.appearance.AppearanceScreen
import app.coshell.appearance.AppearanceState
import app.coshell.language.LanguageChoice
import app.coshell.language.LanguageChoices
import app.coshell.language.components.LanguageList
import app.coshell.core.domain.settings.ThemePalette
import app.coshell.core.designsystem.theme.CoShellPalette
import app.coshell.core.domain.settings.ThemeMode
import app.coshell.backup.BackupActions
import app.coshell.backup.BackupScreen
import app.coshell.backup.BackupUiState
import app.coshell.backup.auto.AutoBackupUiState
import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
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
import app.coshell.core.data.family.FamilyState
import app.coshell.core.model.Family
import app.coshell.core.model.Member
import app.coshell.family.FamilyActions
import app.coshell.family.FamilyScreen
import app.coshell.family.FamilyUiState
import app.coshell.rates.list.RatesActions
import app.coshell.rates.list.RatesScreen
import app.coshell.rates.list.ratesLongSample
import app.coshell.rates.list.ratesPreviewSample
import app.coshell.currencies.list.ReportCurrenciesActions
import app.coshell.currencies.list.ReportCurrenciesScreen
import app.coshell.currencies.list.reportCurrenciesPreviewSample
import app.coshell.home.HomeScreen
import app.coshell.home.components.VersionGateBannerState
import app.coshell.home.HomeUiState
import app.coshell.home.homeBudgetPreviewSample
import app.coshell.home.homePreviewSample
import app.coshell.transactions.list.ListFilter
import app.coshell.transactions.list.components.AccountsChoices
import app.coshell.transactions.list.components.CategoriesChoices
import app.coshell.transactions.list.components.PeriodChoices
import app.coshell.transactions.list.components.TagsChoices
import app.coshell.transactions.list.transactionsFilterChoices
import app.coshell.transactions.list.transactionsFilteredSample
import app.coshell.transactions.list.transactionsLongSample
import app.coshell.transactions.list.TransactionsScreen
import app.coshell.transactions.list.TransactionsUiState
import app.coshell.transactions.list.transactionsPreviewSample
import app.coshell.ui.top.ScreenScrollHost
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

    /** Translations (#270): longer English and Spanish texts still fit. Sample data stays Russian. */
    @Test @Config(qualifiers = "+en")
    fun `home english`() = shot("home_en") { HomeScreen(homePreviewSample, budget = homeBudgetPreviewSample) }

    @Test @Config(qualifiers = "+es")
    fun `home spanish`() = shot("home_es") { HomeScreen(homePreviewSample, budget = homeBudgetPreviewSample) }

    @Test @Config(qualifiers = "+en")
    fun `budgets english`() = shot("budgets_en", dark = true) { BudgetsScreen(budgetsPreviewSample, BudgetsActions()) }

    @Test @Config(qualifiers = "+es")
    fun `budgets spanish`() = shot("budgets_es", dark = true) { BudgetsScreen(budgetsPreviewSample, BudgetsActions()) }

    @Test @Config(qualifiers = "+en")
    fun `more english`() = shot("more_en") { MoreScreen(MoreActions(), moreSample) }

    @Test @Config(qualifiers = "+es")
    fun `more spanish`() = shot("more_es") { MoreScreen(MoreActions(), moreSample) }

    /** Board `Fb335A`: the «новое» pill next to a row with something new (#335). */
    @Test
    fun `more new mark`() = shot("more_new") { MoreScreen(MoreActions(), moreSample.copy(newMarks = setOf(NewFeatures.RECURRING))) }

    @Test
    fun `more new mark dark`() = shot("more_new_dark", dark = true) {
        MoreScreen(MoreActions(), moreSample.copy(newMarks = setOf(NewFeatures.RECURRING)))
    }

    @Test
    fun `home dark`() = shot("home_dark", dark = true) { HomeScreen(homePreviewSample) }

    /** Board `HomeAttention`: a missing rate turns the second tile into «Внимание». */
    @Test
    fun `home attention dark`() = shot("home_attention_dark", dark = true) {
        HomeScreen(homePreviewSample.copy(attention = HomeUiState.Attention(missingRates = listOf("VES"), uncategorized = 2)))
    }

    /** Board `BudHome` (#184): the budget tile next to the expenses and «Горит в бюджете». */
    @Test
    fun `home budget dark`() = shot("home_budget_dark", dark = true) {
        HomeScreen(homePreviewSample, budget = homeBudgetPreviewSample)
    }

    /** Board `Fb334A` (#334): the other phone runs a newer version — looking is open, entry waits for the update. */
    @Test
    fun `home version gate light`() = shot("home_gate_light") {
        HomeScreen(homePreviewSample, gate = VersionGateBannerState("0.41", "5,6"))
    }

    @Test
    fun `home budget light`() = shot("home_budget_light") { HomeScreen(homePreviewSample, budget = homeBudgetPreviewSample) }

    @Test
    fun `transactions light`() = shot("transactions_light") { TransactionsScreen(transactionsPreviewSample) }

    @Test
    fun `transactions dark`() = shot("transactions_dark", dark = true) { TransactionsScreen(transactionsPreviewSample) }

    /** Header poll А (2026-11-15): scrolled — solid header with a hairline, smaller title, the top day below it. */
    @Test
    fun `transactions scrolled dark`() = shot("transactions_scrolled_dark", dark = true, before = {
        compose.onNode(hasScrollToIndexAction()).performTouchInput { swipeUp(startY = bottom * 0.8f, endY = top) }
    }) { TransactionsScreen(transactionsLongSample) }

    // The other theme of references that had one (#382: every screen in both themes).
    @Test
    fun `home attention light`() = shot("home_attention_light") {
        HomeScreen(homePreviewSample.copy(attention = HomeUiState.Attention(missingRates = listOf("VES"), uncategorized = 2)))
    }

    @Test
    fun `transactions scrolled light`() = shot("transactions_scrolled_light", before = {
        compose.onNode(hasScrollToIndexAction()).performTouchInput { swipeUp(startY = bottom * 0.8f, endY = top) }
    }) { TransactionsScreen(transactionsLongSample) }

    @Test
    fun `more dark`() = shot("more_dark", dark = true) { MoreScreen(MoreActions(), moreSample) }

    @Test
    fun `appearance light`() = shot("appearance_light") { AppearanceScreen(AppearanceState(ThemeMode.LIGHT), AppearanceActions()) }

    @Test
    fun `backup light`() = shot("backup_light") { BackupScreen(BackupUiState(), BackupActions()) }

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
    fun `filter tags sheet light`() = shot("filter_tags_light") {
        Column { TagsChoices(ListFilter(tagIds = setOf("аня", "испанский")), transactionsFilterChoices) {} }
    }

    @Test
    fun `accounts light`() = shot("accounts_light") { AccountsScreen(accountsPreviewSample, AccountsActions(onBack = {})) }

    @Test
    fun `accounts dark`() = shot("accounts_dark", dark = true) { AccountsScreen(accountsPreviewSample, AccountsActions(onBack = {})) }

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
    fun `more update dark`() = shot("more_update_dark", dark = true) { MoreScreen(MoreActions(), moreSample.copy(availableVersion = "0.38")) }

    @Test
    fun `appearance dark`() = shot("appearance_dark", dark = true) { AppearanceScreen(AppearanceState(ThemeMode.DARK), AppearanceActions()) }

    @Test
    fun `appearance espresso light`() = shot("appearance_espresso_light", palette = CoShellPalette.Espresso) {
        AppearanceScreen(AppearanceState(ThemeMode.LIGHT, ThemePalette.ESPRESSO), AppearanceActions())
    }

    /** Board `Fb352A` / `Fb352Update`: the language list (body; in the app it sits in a `CoShellSheet`). */
    @Test
    fun `language first launch dark`() = shot("language_first_dark", dark = true) {
        Column { LanguageList(LanguageChoices.all(), LanguageChoice(null), phoneLanguage = "es", uiLanguage = "ru") {} }
    }

    @Test
    fun `language after update light`() = shot("language_update_light") {
        val choices = LanguageChoices.afterUpdate()
        Column { LanguageList(choices, choices.first(), phoneLanguage = "es", uiLanguage = "ru") {} }
    }

    @Test
    fun `backup dark`() = shot("backup_dark", dark = true) { BackupScreen(BackupUiState(), BackupActions()) }

    @Test
    fun `backup auto dark`() = shot("backup_auto_dark", dark = true) { BackupScreen(BackupUiState(), BackupActions(), backupAutoSample) }

    @Test
    fun `backup auto light`() = shot("backup_auto_light") { BackupScreen(BackupUiState(), BackupActions(), backupAutoSample) }

    /** Board `Family`: the hero card with members' avatars, «Синхронизировать сейчас», members, «Пригласить». */
    @Test
    fun `family light`() = shot("family_light") { FamilyScreen(familySample, FamilyActions()) }

    @Test
    fun `family dark`() = shot("family_dark", dark = true) { FamilyScreen(familySample, FamilyActions()) }

    /** Board `Rates`: currency chips, the violet manual-rate card, the dashed «Добавить курс». */
    @Test
    fun `rates light`() = shot("rates_light") { RatesScreen(ratesPreviewSample, RatesActions()) }

    @Test
    fun `rates dark`() = shot("rates_dark", dark = true) { RatesScreen(ratesPreviewSample, RatesActions()) }

    /** #394 Б: an inner screen scrolled — solid header with a hairline (the destination's ScreenScrollHost). */
    @Test
    fun `rates scrolled dark`() = shot("rates_scrolled_dark", dark = true, before = {
        compose.onNode(hasScrollToIndexAction()).performTouchInput { swipeUp(startY = bottom * 0.8f, endY = top) }
    }) { ScreenScrollHost { RatesScreen(ratesLongSample, RatesActions()) } }

    /** Board `ReportCurrencies`: the balance preview hero over the list, main currency outlined. */
    @Test
    fun `report currencies light`() = shot("report_currencies_light") { ReportCurrenciesScreen(reportCurrenciesPreviewSample, ReportCurrenciesActions()) }

    @Test
    fun `report currencies dark`() = shot("report_currencies_dark", dark = true) {
        ReportCurrenciesScreen(reportCurrenciesPreviewSample, ReportCurrenciesActions())
    }

    /** The clock is wound past every entrance animation (blocks cascade in), so each shot is the settled screen. */
    private fun shot(
        name: String,
        dark: Boolean = false,
        palette: CoShellPalette = CoShellPalette.Lime,
        before: () -> Unit = {},
        content: @Composable () -> Unit,
    ) {
        compose.mainClock.autoAdvance = false
        // Surface: editors have no Scaffold, in the app the window gives them the background.
        compose.setContent { CoShellTheme(darkTheme = dark, palette = palette) { Surface { content() } } }
        compose.mainClock.advanceTimeBy(SETTLE_MS)
        before()
        compose.mainClock.advanceTimeBy(SETTLE_MS)
        compose.onRoot().captureRoboImage("src/test/screenshots/$name.png")
    }

    private companion object {
        const val SETTLE_MS = 3_000L
        val moreSample = MoreState("0.37", 400, mainCurrency = "USD", appearance = "Лайм · тёмная")
        private val me = Member("u1", "Михаил", "m@example.com")
        val familySample = FamilyUiState(
            family = FamilyState.Joined(Family("f1", ownerUid = me.uid, listOf(me, Member("u2", "Анна", "a@example.com"))), me),
            lastSync = "сегодня 12:03",
        )
    }

    @Test
    fun `settings light`() = shot("settings_light") { SettingsScreen(SettingsActions()) }

    @Test
    fun `settings dark`() = shot("settings_dark", dark = true) { SettingsScreen(SettingsActions()) }

    @Test
    fun `settings money light`() = shot("settings_money_light") { MoneySettingsScreen({}, {}) }

    @Test
    fun `settings money dark`() = shot("settings_money_dark", dark = true) { MoneySettingsScreen({}, {}) }

    @Test
    fun `settings input light`() = shot("settings_input_light") { InputSettingsScreen(true, {}, true, {}, {}) }

    @Test
    fun `settings input dark`() = shot("settings_input_dark", dark = true) { InputSettingsScreen(true, {}, true, {}, {}) }

    @Test
    fun `settings notices light`() = shot("settings_notices_light") { NoticeSettingsScreen(true, {}, 9, {}, {}) }

    @Test
    fun `settings notices dark`() = shot("settings_notices_dark", dark = true) { NoticeSettingsScreen(true, {}, 9, {}, {}) }

    @Test
    fun `settings data light`() = shot("settings_data_light") { DataSettingsScreen(false, {}, {}, {}, {}, {}) }

    @Test
    fun `settings data dark`() = shot("settings_data_dark", dark = true) { DataSettingsScreen(false, {}, {}, {}, {}, {}) }

    /** «Плотность списков» — compact: the same accounts, about a quarter more rows per screen. */
    @Test
    fun `accounts compact light`() = shot("accounts_compact_light") {
        CompositionLocalProvider(LocalCompactLists provides true) { AccountsScreen(accountsPreviewSample, AccountsActions(onBack = {})) }
    }

    @Test
    fun `appearance compact dark`() = shot("appearance_compact_dark", dark = true) {
        AppearanceScreen(AppearanceState(ThemeMode.DARK, compactLists = true), AppearanceActions())
    }

    @Test
    fun `settings screens light`() = shot("settings_screens_light") { ScreensSettingsScreen({}, {}, {}, {}, {}) }

    @Test
    fun `settings screens dark`() = shot("settings_screens_dark", dark = true) { ScreensSettingsScreen({}, {}, {}, {}, {}) }

    @Test
    fun `settings ops light`() = shot("settings_ops_light") { OpsSettingsScreen(ScreenOptions(opsShowNote = false), {}, {}) }

    @Test
    fun `settings ops dark`() = shot("settings_ops_dark", dark = true) { OpsSettingsScreen(ScreenOptions(opsShowNote = false), {}, {}) }

    @Test
    fun `settings reports light`() = shot("settings_reports_light") { ReportsSettingsScreen(ScreenOptions(), {}, {}) }

    @Test
    fun `settings reports dark`() = shot("settings_reports_dark", dark = true) { ReportsSettingsScreen(ScreenOptions(), {}, {}) }

    @Test
    fun `settings accounts light`() = shot("settings_accounts_light") { AccountsSettingsScreen(ScreenOptions(), {}, {}) }

    @Test
    fun `settings accounts dark`() = shot("settings_accounts_dark", dark = true) { AccountsSettingsScreen(ScreenOptions(), {}, {}) }

    @Test
    fun `settings home light`() = shot("settings_home_light") { HomeSettingsScreen(ScreenOptions(home = HomeLayout().withShown(HomeCard.HOT, false)), {}, {}, {}) }

    @Test
    fun `settings home dark`() = shot("settings_home_dark", dark = true) { HomeSettingsScreen(ScreenOptions(home = HomeLayout().withShown(HomeCard.HOT, false)), {}, {}, {}) }

    @Test
    fun `settings home details light`() = shot("settings_home_details_light") { HomeDetailsScreen(ScreenOptions(), {}, {}) }

    @Test
    fun `settings home details dark`() = shot("settings_home_details_dark", dark = true) { HomeDetailsScreen(ScreenOptions(), {}, {}) }
}

private val backupAutoSample = AutoBackupUiState(enabled = true, folderName = "Google Диск / Co-Shell", lastBackup = "3 ноября", daysLeft = 2)
