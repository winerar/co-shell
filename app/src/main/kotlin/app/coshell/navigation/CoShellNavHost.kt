package app.coshell.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import app.coshell.core.designsystem.components.CoShellSheet
import app.coshell.core.designsystem.components.CoShellSheetDialogProperties
import app.coshell.accounts.edit.AccountEditRoute
import app.coshell.accounts.list.AccountsRoute
import app.coshell.backup.BackupRoute
import app.coshell.categories.edit.CategoryEditRoute
import app.coshell.categories.list.CategoriesRoute
import app.coshell.currencies.list.ReportCurrenciesRoute
import app.coshell.family.FamilyRoute
import app.coshell.feedback.FeedbackRoute
import app.coshell.home.HomeRoute
import app.coshell.more.MoreActions
import app.coshell.more.MoreScreen
import app.coshell.rates.list.RatesRoute
import app.coshell.settings.SettingsRoute
import app.coshell.shake.ShakeEffect
import app.coshell.shake.ShakeSettingsViewModel
import app.coshell.transactions.edit.TransactionEditRoute
import app.coshell.transactions.list.TransactionsRoute
import app.coshell.undo.UndoSnackbarEffect
import app.coshell.update.UpdateRoute
import app.coshell.update.startup.StartupUpdateDialogs

@Composable
fun CoShellNavHost(
    navController: NavHostController = rememberNavController(),
    shakeSettings: ShakeSettingsViewModel = hiltViewModel(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val onTab = TopLevel.entries.any { backStackEntry?.destination.isOn(it) }
    val snackbar = remember { SnackbarHostState() }
    UndoSnackbarEffect(snackbar)

    StartupUpdateDialogs(onOpenUpdate = { navController.navigate(UpdateDestination) { launchSingleTop = true } })

    // Shake → new transaction, only on the tabs: never interrupts a form or another screen.
    val shakeEnabled by shakeSettings.enabled.collectAsStateWithLifecycle()
    ShakeEffect(
        enabled = shakeEnabled && onTab,
        onShake = { navController.navigate(TransactionEditDestination()) { launchSingleTop = true } },
    )

    MainShell(
        navController = navController,
        snackbar = snackbar,
        onAdd = { navController.navigate(TransactionEditDestination()) { launchSingleTop = true } },
    ) {
        composable<HomeDestination> {
            HomeRoute(
                onAccountClick = { navController.navigate(AccountEditDestination(it)) },
                onAddAccount = { navController.navigate(AccountEditDestination()) },
            )
        }
        composable<TransactionsDestination> {
            TransactionsRoute(
                onTransactionClick = { navController.navigate(TransactionEditDestination(transactionId = it)) },
            )
        }
        dialog<TransactionEditDestination>(dialogProperties = CoShellSheetDialogProperties) {
            CoShellSheet(onClosed = { navController.popBackStack() }) { close ->
                TransactionEditRoute(
                    onDone = close,
                    onAddAccount = { navController.navigate(AccountEditDestination()) },
                )
            }
        }
        composable<AccountsDestination> {
            AccountsRoute(
                onAccountClick = { navController.navigate(AccountEditDestination(it)) },
                onAddAccount = { navController.navigate(AccountEditDestination()) },
            )
        }
        dialog<AccountEditDestination>(dialogProperties = CoShellSheetDialogProperties) {
            CoShellSheet(onClosed = { navController.popBackStack() }) { close -> AccountEditRoute(onDone = close) }
        }
        composable<MoreDestination> {
            MoreScreen(
                MoreActions(
                    onCategories = { navController.navigate(CategoriesDestination) },
                    onRates = { navController.navigate(RatesDestination) },
                    onFamily = { navController.navigate(FamilyDestination) },
                    onBackup = { navController.navigate(BackupDestination) },
                    onFeedback = { navController.navigate(FeedbackDestination) },
                    onSettings = { navController.navigate(SettingsDestination) },
                ),
            )
        }
        composable<SettingsDestination> {
            SettingsRoute(
                onBack = { navController.popBackStack() },
                onReportCurrencies = { navController.navigate(ReportCurrenciesDestination) },
                onUpdate = { navController.navigate(UpdateDestination) },
            )
        }
        composable<UpdateDestination> {
            UpdateRoute(onBack = { navController.popBackStack() })
        }
        composable<FeedbackDestination> {
            FeedbackRoute(onBack = { navController.popBackStack() })
        }
        composable<RatesDestination> {
            RatesRoute(onBack = { navController.popBackStack() })
        }
        composable<ReportCurrenciesDestination> {
            ReportCurrenciesRoute(onBack = { navController.popBackStack() })
        }
        composable<FamilyDestination> {
            FamilyRoute(onBack = { navController.popBackStack() })
        }
        composable<BackupDestination> {
            BackupRoute(onBack = { navController.popBackStack() })
        }
        composable<CategoriesDestination> {
            CategoriesRoute(
                onBack = { navController.popBackStack() },
                onOpen = { navController.navigate(it) },
            )
        }
        dialog<CategoryEditDestination>(dialogProperties = CoShellSheetDialogProperties) {
            CoShellSheet(onClosed = { navController.popBackStack() }) { close -> CategoryEditRoute(onDone = close) }
        }
    }
}
