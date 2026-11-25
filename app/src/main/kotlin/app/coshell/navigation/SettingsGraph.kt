package app.coshell.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import app.coshell.ai.AiAddonRoute
import app.coshell.ai.cloud.CloudAiRoute
import app.coshell.appearance.AppearanceRoute
import app.coshell.export.ExportRoute
import app.coshell.privacy.pin.PinSetupRoute
import app.coshell.privacy.settings.PrivacyRoute
import app.coshell.settings.AccountsSettingsRoute
import app.coshell.settings.BalanceAccountsRoute
import app.coshell.settings.DataSettingsRoute
import app.coshell.settings.HomeDetailsRoute
import app.coshell.settings.HomeSettingsRoute
import app.coshell.settings.InputSettingsRoute
import app.coshell.settings.MoneySettingsRoute
import app.coshell.settings.NoticeSettingsRoute
import app.coshell.settings.OpsSettingsRoute
import app.coshell.settings.ReportsSettingsRoute
import app.coshell.settings.ScreensSettingsScreen
import app.coshell.settings.SettingsActions
import app.coshell.settings.SettingsRoute
import app.coshell.wipe.WipeDataRoute

/** «Ещё → Настройки» and its pages, export, wipe (split out of `CoShellNavHost`). */
internal fun NavGraphBuilder.settingsGraph(navController: NavHostController) {
    screen<SettingsDestination> {
        SettingsRoute(
            SettingsActions(
                onBack = { navController.popBackStack() },
                onLook = { navController.navigate(AppearanceDestination) },
                onScreens = { navController.navigate(ScreensSettingsDestination) },
                onMoney = { navController.navigate(MoneySettingsDestination) },
                onInput = { navController.navigate(InputSettingsDestination) },
                onNotices = { navController.navigate(NoticeSettingsDestination) },
                onData = { navController.navigate(DataSettingsDestination) },
                onCurrencies = { navController.navigate(ReportCurrenciesDestination) },
                onBackup = { navController.navigate(BackupDestination) },
                onAiAddon = { navController.navigate(AiAddonDestination) { launchSingleTop = true } },
                onCloudAi = { navController.navigate(CloudAiDestination) { launchSingleTop = true } },
                onTour = { navController.navigate(TourDestination) { launchSingleTop = true } },
            ),
        )
    }
    screen<AiAddonDestination> {
        AiAddonRoute(onBack = { navController.popBackStack() })
    }
    screen<CloudAiDestination> {
        CloudAiRoute(onBack = { navController.popBackStack() })
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
