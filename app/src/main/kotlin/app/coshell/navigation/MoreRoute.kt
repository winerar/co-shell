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
import app.coshell.more.MoreActions
import app.coshell.more.MoreScreen
import app.coshell.more.MoreState
import app.coshell.theme.ThemeSettingsViewModel
import app.coshell.theme.appearanceSummary

/** «Ещё» with what its rows say (main report currency, theme) and where each row leads. */
@Composable
internal fun MoreRoute(
    navController: NavHostController,
    availableBuild: Int?,
    onStartUpdate: () -> Unit,
    reportCurrencies: ReportCurrenciesViewModel = hiltViewModel(),
    themeSettings: ThemeSettingsViewModel = hiltViewModel(),
) {
    val currencies by reportCurrencies.state.collectAsStateWithLifecycle()
    val mode by themeSettings.mode.collectAsStateWithLifecycle()
    val palette by themeSettings.palette.collectAsStateWithLifecycle()
    MoreScreen(
        actions = MoreActions(
            onCategories = { navController.navigate(CategoriesDestination) },
            onBudgets = { navController.navigate(BudgetsDestination) },
            onRates = { navController.navigate(RatesDestination) },
            onReportCurrencies = { navController.navigate(ReportCurrenciesDestination) },
            onFamily = { navController.navigate(FamilyDestination) },
            onBackup = { navController.navigate(BackupDestination) },
            onImport = { navController.navigate(WalletImportDestination) },
            onAppearance = { navController.navigate(AppearanceDestination) },
            onFeedback = { navController.navigate(FeedbackDestination()) },
            onOpenUpdate = { navController.navigate(UpdateDestination) },
            onStartUpdate = onStartUpdate,
        ),
        state = MoreState(
            versionName = BuildConfig.VERSION_NAME,
            build = BuildConfig.VERSION_CODE,
            availableBuild = availableBuild,
            mainCurrency = currencies.currencies.firstOrNull(),
            appearance = appearanceSummary(mode ?: ThemeMode.SYSTEM, palette ?: ThemePalette.VOLT),
        ),
    )
}
