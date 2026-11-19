package app.coshell.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import app.coshell.BuildConfig
import app.coshell.core.domain.settings.ThemeMode
import app.coshell.core.domain.settings.ThemePalette
import app.coshell.currencies.list.ReportCurrenciesViewModel
import app.coshell.hints.HintsViewModel
import app.coshell.more.MoreActions
import app.coshell.more.MoreScreen
import app.coshell.more.MoreState
import app.coshell.newmark.NewMarksViewModel
import app.coshell.theme.ThemeSettingsViewModel
import app.coshell.theme.appearanceSummary

/** «Ещё» with what its rows say (main report currency, theme, «новое» pills) and where each row leads. */
@Composable
internal fun MoreRoute(
    navController: NavHostController,
    availableVersion: String?,
    onStartUpdate: () -> Unit,
    reportCurrencies: ReportCurrenciesViewModel = hiltViewModel(),
    themeSettings: ThemeSettingsViewModel = hiltViewModel(),
    newMarks: NewMarksViewModel = hiltViewModel(),
    hints: HintsViewModel = hiltViewModel(),
) {
    val hintsOn by hints.enabled.collectAsStateWithLifecycle()
    val marks by newMarks.current.collectAsStateWithLifecycle()
    val currencies by reportCurrencies.state.collectAsStateWithLifecycle()
    val mode by themeSettings.mode.collectAsStateWithLifecycle()
    val palette by themeSettings.palette.collectAsStateWithLifecycle()
    MoreScreen(
        actions = MoreActions(
            onAccounts = { navController.navigate(AccountsDestination) },
            onCategories = { navController.navigate(CategoriesDestination) },
            onBudgets = { navController.navigate(BudgetsDestination) },
            onRecurring = { navController.navigate(RecurringListDestination) { launchSingleTop = true } },
            onRates = { navController.navigate(RatesDestination) },
            onConverter = { navController.navigate(ConverterDestination) { launchSingleTop = true } },
            onReportCurrencies = { navController.navigate(ReportCurrenciesDestination) },
            onFamily = { navController.navigate(FamilyDestination) },
            onBackup = { navController.navigate(BackupDestination) },
            onImport = { navController.navigate(WalletImportDestination) },
            onAppearance = { navController.navigate(SettingsDestination) },
            onFeedback = { navController.navigate(FeedbackDestination()) },
            onHintsToggle = hints::setEnabled,
            onHintsReset = hints::reset,
            onOpenUpdate = { navController.navigate(UpdateDestination) },
            onStartUpdate = onStartUpdate,
            onOpenedNew = newMarks::opened,
        ),
        state = MoreState(
            versionName = BuildConfig.VERSION_NAME,
            build = BuildConfig.VERSION_CODE,
            availableVersion = availableVersion,
            mainCurrency = currencies.currencies.firstOrNull(),
            appearance = appearanceSummary(mode ?: ThemeMode.SYSTEM, palette ?: ThemePalette.LIME),
            newMarks = marks,
            hintsEnabled = hintsOn,
        ),
    )
}
