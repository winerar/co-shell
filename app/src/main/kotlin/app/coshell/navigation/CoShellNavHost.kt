package app.coshell.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import app.coshell.accounts.edit.AccountEditRoute
import app.coshell.accounts.list.AccountsRoute
import app.coshell.backup.BackupRoute
import app.coshell.budgets.detail.BudgetDetailRoute
import app.coshell.budgets.edit.BudgetEditRoute
import app.coshell.budgets.list.BudgetsRoute
import app.coshell.categories.edit.CategoryEditRoute
import app.coshell.categories.list.CategoriesRoute
import app.coshell.core.designsystem.components.CoShellSheet
import app.coshell.core.designsystem.components.CoShellSheetDialogProperties
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.crash.notice.CrashNotice
import app.coshell.crash.notice.CrashNoticeViewModel
import app.coshell.currencies.list.ReportCurrenciesRoute
import app.coshell.family.FamilyRoute
import app.coshell.feedback.FeedbackRoute
import app.coshell.feedback.detail.FeedbackDetailRoute
import app.coshell.home.HomeActions
import app.coshell.home.HomeRoute
import app.coshell.importer.wallet.WalletImportRoute
import app.coshell.more.MoreActions
import app.coshell.more.MoreScreen
import app.coshell.rates.list.RatesRoute
import app.coshell.settings.SettingsRoute
import app.coshell.shake.ShakeEffect
import app.coshell.shake.ShakeSettingsViewModel
import app.coshell.templates.edit.TemplateEditRoute
import app.coshell.templates.list.TemplatesRoute
import app.coshell.transactions.edit.TransactionEditRoute
import app.coshell.transactions.list.PRESET_UNCATEGORIZED
import app.coshell.transactions.list.TransactionsRoute
import app.coshell.undo.UndoSnackbarEffect
import app.coshell.update.UpdateRoute
import app.coshell.update.notice.UpdateNoticeEffects
import app.coshell.update.notice.UpdateNoticeViewModel
import app.coshell.update.notice.UpdatePill
import app.coshell.wipe.WipeDataRoute
import kotlinx.datetime.number

@Composable
fun CoShellNavHost(
    navController: NavHostController = rememberNavController(),
    shakeSettings: ShakeSettingsViewModel = hiltViewModel(),
    updates: UpdateNoticeViewModel = hiltViewModel(),
    crashes: CrashNoticeViewModel = hiltViewModel(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val onTab = TopLevel.entries.any { backStackEntry?.destination.isOn(it) }
    val snackbar = remember { SnackbarHostState() }
    UndoSnackbarEffect(snackbar)

    UpdateNoticeEffects(updates)
    CrashNotice(crashes)
    val updatePill by updates.pill.collectAsStateWithLifecycle()
    val updateAvailable by updates.available.collectAsStateWithLifecycle()

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
        moreBadge = updateAvailable != null,
        overBar = { onBar ->
            UpdatePill(
                release = updatePill,
                visible = onBar,
                onOpen = { release ->
                    updates.startUpdate(release)
                    navController.navigate(UpdateDestination) { launchSingleTop = true }
                },
                onDismiss = updates::dismissPill,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = Spacing.m),
            )
        },
    ) {
        composable<HomeDestination> {
            HomeRoute(
                HomeActions(
                    onTransactions = { navController.navigateToTab(TopLevel.TRANSACTIONS) },
                    onAccounts = { navController.navigateToTab(TopLevel.ACCOUNTS) },
                    onRates = { navController.navigate(RatesDestination) { launchSingleTop = true } },
                    onUncategorized = {
                        navController.navigateToTab(TopLevel.TRANSACTIONS)
                        navController.getBackStackEntry<TransactionsDestination>()
                            .savedStateHandle[PRESET_UNCATEGORIZED] = true
                    },
                    onTransactionClick = { navController.navigate(TransactionEditDestination(transactionId = it)) },
                    onAddAccount = { navController.navigate(AccountEditDestination()) },
                    onFamily = { navController.navigate(FamilyDestination) { launchSingleTop = true } },
                    onFeedback = { navController.navigate(FeedbackDestination(prefill = it)) { launchSingleTop = true } },
                ),
            )
        }
        composable<TransactionsDestination> { entry ->
            TransactionsRoute(
                preset = entry.savedStateHandle,
                onTransactionClick = { navController.navigate(TransactionEditDestination(transactionId = it)) },
                onDuplicate = { navController.navigate(TransactionEditDestination(copyFrom = it)) { launchSingleTop = true } },
            )
        }
        dialog<TransactionEditDestination>(dialogProperties = CoShellSheetDialogProperties) {
            CoShellSheet(onClosed = { navController.popBackStack() }) { close ->
                TransactionEditRoute(
                    onDone = close,
                    onAddAccount = { navController.navigate(AccountEditDestination()) },
                    // The copy replaces the open sheet: back from it returns to the list.
                    onDuplicate = { id ->
                        navController.navigate(TransactionEditDestination(copyFrom = id)) {
                            popUpTo<TransactionEditDestination> { inclusive = true }
                        }
                    },
                    // Full screens: navigating to them closes the sheet (a dialog destination) by itself.
                    onAllTemplates = { navController.navigate(TemplatesDestination) { launchSingleTop = true } },
                    onNewTemplate = { navController.navigate(TemplateEditDestination()) { launchSingleTop = true } },
                )
            }
        }
        composable<TemplatesDestination> {
            TemplatesRoute(
                onBack = { navController.popBackStack() },
                onUse = { navController.navigate(TransactionEditDestination(templateId = it)) { launchSingleTop = true } },
                onEdit = { navController.navigate(TemplateEditDestination(it)) { launchSingleTop = true } },
                onNew = { navController.navigate(TemplateEditDestination()) { launchSingleTop = true } },
            )
        }
        composable<TemplateEditDestination> {
            TemplateEditRoute(onDone = { navController.popBackStack() })
        }
        composable<BudgetsDestination> {
            BudgetsRoute(
                onBack = { navController.popBackStack() },
                onEdit = { id, month ->
                    navController.navigate(BudgetEditDestination(id, month.year, month.month.number)) { launchSingleTop = true }
                },
                onOpen = { id, month ->
                    navController.navigate(BudgetDetailDestination(id, month.year, month.month.number)) { launchSingleTop = true }
                },
            )
        }
        composable<BudgetDetailDestination> {
            BudgetDetailRoute(
                onBack = { navController.popBackStack() },
                onEditLimit = { id, month -> navController.navigate(BudgetEditDestination(id, month.year, month.month.number)) },
                // A limit inside the folder: its own envelope on top; back returns to the folder.
                onOpenEnvelope = { id, month -> navController.navigate(BudgetDetailDestination(id, month.year, month.month.number)) },
                onTransactionClick = { navController.navigate(TransactionEditDestination(transactionId = it)) },
            )
        }
        composable<BudgetEditDestination> {
            BudgetEditRoute(
                onDone = { navController.popBackStack() },
                // A limit inside the folder: its own editor on top; back returns to the folder.
                onOpenCategory = { id, month -> navController.navigate(BudgetEditDestination(id, month.year, month.month.number)) },
            )
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
                    onBudgets = { navController.navigate(BudgetsDestination) },
                    onRates = { navController.navigate(RatesDestination) },
                    onFamily = { navController.navigate(FamilyDestination) },
                    onBackup = { navController.navigate(BackupDestination) },
                    onImport = { navController.navigate(WalletImportDestination) },
                    onFeedback = { navController.navigate(FeedbackDestination()) },
                    onSettings = { navController.navigate(SettingsDestination) },
                ),
                availableUpdate = updateAvailable?.versionCode,
            )
        }
        composable<SettingsDestination> {
            SettingsRoute(
                onBack = { navController.popBackStack() },
                onReportCurrencies = { navController.navigate(ReportCurrenciesDestination) },
                onUpdate = { navController.navigate(UpdateDestination) },
                onWipeData = { navController.navigate(WipeDataDestination) },
                availableUpdate = updateAvailable?.versionCode,
            )
        }
        composable<WipeDataDestination> {
            WipeDataRoute(
                onBack = { navController.popBackStack() },
                onImport = {
                    navController.navigate(WalletImportDestination) { popUpTo<WipeDataDestination> { inclusive = true } }
                },
            )
        }
        composable<UpdateDestination> {
            UpdateRoute(onBack = { navController.popBackStack() })
        }
        composable<FeedbackDestination> { entry ->
            FeedbackRoute(
                onBack = { navController.popBackStack() },
                onOpen = { navController.navigate(FeedbackDetailDestination(it)) { launchSingleTop = true } },
                prefill = entry.toRoute<FeedbackDestination>().prefill,
            )
        }
        composable<FeedbackDetailDestination> { entry ->
            FeedbackDetailRoute(entry.toRoute<FeedbackDetailDestination>().sentAtMillis, onBack = { navController.popBackStack() })
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
        composable<WalletImportDestination> {
            WalletImportRoute(onBack = { navController.popBackStack() })
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
