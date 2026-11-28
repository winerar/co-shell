package app.coshell.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import app.coshell.appearance.AppearanceRoute
import app.coshell.export.ExportRoute
import app.coshell.privacy.pin.PinSetupRoute
import app.coshell.privacy.settings.PrivacyRoute
import app.coshell.settings.AccountsSettingsRoute
import app.coshell.settings.BalanceAccountsRoute
import app.coshell.settings.DataSettingsRoute
import app.coshell.settings.HelpSettingsRoute
import app.coshell.settings.HomeDetailsRoute
import app.coshell.settings.HomeSettingsRoute
import app.coshell.settings.InputSettingsRoute
import app.coshell.settings.MoneySettingsRoute
import app.coshell.settings.NoticeSettingsRoute
import app.coshell.settings.BarSettingsRoute
import app.coshell.settings.OpsSettingsRoute
import app.coshell.settings.ReportsSettingsRoute
import app.coshell.settings.ScreensSettingsScreen
import app.coshell.settings.SettingsActions
import app.coshell.settings.SettingsRoute
import app.coshell.settings.SettingsTarget
import app.coshell.wipe.WipeDataRoute

/** «Ещё → Настройки» and its pages, export, wipe (split out of `CoShellNavHost`). */
internal fun NavGraphBuilder.settingsGraph(navController: NavHostController) {
    screen<SettingsDestination> {
        SettingsRoute(
            SettingsActions(
                onBack = { navController.popBackStack() },
                onOpen = { target -> navController.openSettings(target) },
            ),
        )
    }
    screen<HelpSettingsDestination> {
        HelpSettingsRoute(
            onBack = { navController.popBackStack() },
            onTour = { navController.navigate(TourDestination) { launchSingleTop = true } },
            onGestures = { navController.navigate(GesturesDestination) { launchSingleTop = true } },
        )
    }
    screen<AppearanceDestination> {
        AppearanceRoute(onBack = { navController.popBackStack() })
    }
    screen<PrivacyDestination> {
        PrivacyRoute(
            onBack = { navController.popBackStack() },
            onPinSetup = { mode -> navController.navigate(PinSetupDestination(mode.name)) { launchSingleTop = true } },
        )
    }
    screen<PinSetupDestination> {
        PinSetupRoute(onDone = { navController.popBackStack() })
    }
    screen<ScreensSettingsDestination> {
        ScreensSettingsScreen(
            onHome = { navController.navigate(HomeSettingsDestination) },
            onBar = { navController.navigate(BarSettingsDestination) },
            onOps = { navController.navigate(OpsSettingsDestination) },
            onReports = { navController.navigate(ReportsSettingsDestination) },
            onAccounts = { navController.navigate(AccountsSettingsDestination) },
            onBack = { navController.popBackStack() },
        )
    }
    screen<HomeSettingsDestination> {
        HomeSettingsRoute(
            onDetails = { navController.navigate(HomeDetailsSettingsDestination) },
            onBalance = { navController.navigate(BalanceAccountsDestination) },
            onBack = { navController.popBackStack() },
        )
    }
    screen<BalanceAccountsDestination> {
        BalanceAccountsRoute(onBack = { navController.popBackStack() })
    }
    screen<HomeDetailsSettingsDestination> { HomeDetailsRoute(onBack = { navController.popBackStack() }) }
    screen<BarSettingsDestination> { BarSettingsRoute(onBack = { navController.popBackStack() }) }
    screen<OpsSettingsDestination> { OpsSettingsRoute(onBack = { navController.popBackStack() }) }
    screen<ReportsSettingsDestination> { ReportsSettingsRoute(onBack = { navController.popBackStack() }) }
    screen<AccountsSettingsDestination> { AccountsSettingsRoute(onBack = { navController.popBackStack() }) }
    screen<MoneySettingsDestination> {
        MoneySettingsRoute(
            onCurrencies = { navController.navigate(ReportCurrenciesDestination) },
            onBack = { navController.popBackStack() },
        )
    }
    screen<InputSettingsDestination> {
        InputSettingsRoute(onBack = { navController.popBackStack() })
    }
    screen<NoticeSettingsDestination> {
        NoticeSettingsRoute(onBack = { navController.popBackStack() })
    }
    screen<DataSettingsDestination> {
        DataSettingsRoute(
            onBackup = { navController.navigate(BackupDestination) },
            onImport = { navController.navigate(WalletImportDestination) },
            onExport = { navController.navigate(ExportDestination) },
            onWipe = { navController.navigate(WipeDataDestination) },
            onBack = { navController.popBackStack() },
        )
    }
    screen<ExportDestination> { ExportRoute(onBack = { navController.popBackStack() }) }
    screen<WipeDataDestination> {
        WipeDataRoute(
            onBack = { navController.popBackStack() },
            onImport = {
                navController.navigate(WalletImportDestination) { popUpTo<WipeDataDestination> { inclusive = true } }
            },
        )
    }
}

/** A place of «Настройки» (a group of the index or a setting the search found) → its screen. */
private fun NavHostController.openSettings(target: SettingsTarget) {
    val destination: Any = when (target) {
        SettingsTarget.LOOK -> AppearanceDestination
        SettingsTarget.INPUT -> InputSettingsDestination
        SettingsTarget.NOTICES -> NoticeSettingsDestination
        SettingsTarget.SCREENS -> ScreensSettingsDestination
        SettingsTarget.PRIVACY -> PrivacyDestination
        SettingsTarget.HELP -> HelpSettingsDestination
        SettingsTarget.MONEY -> MoneySettingsDestination
        SettingsTarget.DATA -> DataSettingsDestination
        SettingsTarget.CURRENCIES -> ReportCurrenciesDestination
        SettingsTarget.HOME -> HomeSettingsDestination
        SettingsTarget.BAR -> BarSettingsDestination
        SettingsTarget.OPS -> OpsSettingsDestination
        SettingsTarget.REPORTS -> ReportsSettingsDestination
        SettingsTarget.ACCOUNTS -> AccountsSettingsDestination
        SettingsTarget.BALANCE -> BalanceAccountsDestination
        SettingsTarget.BACKUP -> BackupDestination
        SettingsTarget.IMPORT -> WalletImportDestination
        SettingsTarget.EXPORT -> ExportDestination
        SettingsTarget.WIPE -> WipeDataDestination
        SettingsTarget.TOUR -> TourDestination
        SettingsTarget.GESTURES -> GesturesDestination
    }
    navigate(destination) { launchSingleTop = true }
}
