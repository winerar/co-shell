package app.coshell.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import app.coshell.accounts.edit.AccountEditRoute
import app.coshell.appearance.AppearanceRoute
import app.coshell.export.ExportRoute
import app.coshell.settings.AccountsSettingsRoute
import app.coshell.settings.BalanceAccountsRoute
import app.coshell.settings.HomeDetailsRoute
import app.coshell.settings.HomeSettingsRoute
import app.coshell.settings.DataSettingsRoute
import app.coshell.settings.OpsSettingsRoute
import app.coshell.settings.ReportsSettingsRoute
import app.coshell.settings.ScreensSettingsScreen
import app.coshell.settings.InputSettingsRoute
import app.coshell.settings.MoneySettingsRoute
import app.coshell.settings.NoticeSettingsRoute
import app.coshell.settings.SettingsActions
import app.coshell.settings.SettingsScreen
import app.coshell.recurring.notify.PaymentLinks
import app.coshell.accounts.list.AccountsRoute
import app.coshell.backup.BackupRoute
import app.coshell.budgets.detail.BudgetDetailRoute
import app.coshell.budgets.edit.BudgetEditRoute
import app.coshell.budgets.list.BudgetsRoute
import app.coshell.reports.ReportsRoute
import app.coshell.reports.capital.CapitalMonthRoute
import app.coshell.reports.all.ReportCategoriesRoute
import app.coshell.reports.category.ReportCategoryRoute
import app.coshell.reports.review.MonthLimitsRoute
import app.coshell.reports.review.MonthReviewRoute
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
import app.coshell.converter.ConverterRoute
import app.coshell.home.HomeActions
import app.coshell.home.HomeRoute
import app.coshell.importer.wallet.WalletImportRoute
import app.coshell.rates.list.RatesRoute
import app.coshell.shake.ShakeEffect
import app.coshell.shake.ShakeSettingsViewModel
import app.coshell.recurring.list.RecurringListRoute
import app.coshell.templates.edit.TemplateEditRoute
import app.coshell.templates.list.TemplatesRoute
import app.coshell.transactions.edit.TransactionEditRoute
import app.coshell.transactions.list.PRESET_MEMBER
import app.coshell.transactions.list.PRESET_UNCATEGORIZED
import app.coshell.transactions.list.TransactionsRoute
import app.coshell.undo.UndoSnackbarEffect
import app.coshell.update.UpdateRoute
import app.coshell.update.past.PastReleasesRoute
import app.coshell.update.notice.UpdateNoticeEffects
import app.coshell.update.notice.UpdateNoticeViewModel
import app.coshell.greeting.GreetingPill
import app.coshell.greeting.GreetingViewModel
import app.coshell.update.notice.UpdatePill
import app.coshell.wipe.WipeDataRoute
import app.coshell.gate.InputLockViewModel
import app.coshell.gate.rememberInputLock
import app.coshell.home.components.VersionGateBannerState
import app.coshell.update.components.megabytes
import kotlinx.datetime.number

@Composable
fun CoShellNavHost(
    navController: NavHostController = rememberNavController(),
    shakeSettings: ShakeSettingsViewModel = hiltViewModel(),
    updates: UpdateNoticeViewModel = hiltViewModel(),
    greeting: GreetingViewModel = hiltViewModel(),
    crashes: CrashNoticeViewModel = hiltViewModel(),
    inputLock: InputLockViewModel = hiltViewModel(),
    paymentLinks: PaymentLinks? = null,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val onTab = TopLevel.entries.any { backStackEntry?.destination.isOn(it) }
    val snackbar = remember { SnackbarHostState() }
    UndoSnackbarEffect(snackbar)

    UpdateNoticeEffects(updates)
    CrashNotice(crashes)
    val updatePill by updates.pill.collectAsStateWithLifecycle()
    val updateAvailable by updates.available.collectAsStateWithLifecycle()
    val greetingVisible by greeting.pillVisible.collectAsStateWithLifecycle()
    val onHome = backStackEntry?.destination.isOn(TopLevel.HOME)
    val locked by inputLock.locked.collectAsStateWithLifecycle()
    // «Обновить» on the gate banner and its note (#334): the download starts if the release is known.
    val openUpdate = {
        updateAvailable?.let(updates::startUpdate)
        navController.navigate(UpdateDestination) { launchSingleTop = true }
    }
    val lock = rememberInputLock(locked, openUpdate)

    // Shake → new transaction, only on the tabs: never interrupts a form or another screen.
    val shakeEnabled by shakeSettings.enabled.collectAsStateWithLifecycle()
    ShakeEffect(
        enabled = shakeEnabled && onTab,
        onShake = lock.guard { navController.navigate(TransactionEditDestination()) { launchSingleTop = true } },
    )

    MainShell(
        navController = navController,
        snackbar = snackbar,
        onAdd = lock.guard { navController.navigate(TransactionEditDestination()) { launchSingleTop = true } },
        moreBadge = updateAvailable != null,
        inputLock = lock,
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
            // The 1.0 greeting: only on «Обзор», and the update pill goes first.
            GreetingPill(
                visible = onBar && onHome && greetingVisible && updatePill == null,
                onDismiss = greeting::dismiss,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = Spacing.m),
            )
        },
    ) {
        screen<HomeDestination> {
            HomeRoute(
                HomeActions(
                    onTransactions = { navController.navigateToTab(TopLevel.TRANSACTIONS) },
                    onRates = { navController.navigate(RatesDestination) { launchSingleTop = true } },
                    onConverter = { navController.navigate(ConverterDestination) { launchSingleTop = true } },
                    onUncategorized = {
                        navController.navigateToTab(TopLevel.TRANSACTIONS)
                        navController.getBackStackEntry<TransactionsDestination>()
                            .savedStateHandle[PRESET_UNCATEGORIZED] = true
                    },
                    onTransactionClick = { navController.navigate(TransactionEditDestination(transactionId = it)) },
                    onAddAccount = { navController.navigate(AccountEditDestination()) },
                    onAccounts = { navController.navigate(AccountsDestination) { launchSingleTop = true } },
                    onFamily = { navController.navigate(FamilyDestination) { launchSingleTop = true } },
                    onFeedback = { navController.navigate(FeedbackDestination(prefill = it)) { launchSingleTop = true } },
                    onBudgets = { navController.navigate(BudgetsDestination) { launchSingleTop = true } },
                    onBudget = { id, month ->
                        navController.navigate(BudgetDetailDestination(id, month.year, month.month.number)) { launchSingleTop = true }
                    },
                    onReview = { month -> navController.navigate(MonthReviewDestination(month.year, month.month.number)) { launchSingleTop = true } },
                    onPostPayment = { templateId, date ->
                        navController.navigate(TransactionEditDestination(templateId = templateId, paymentDate = date.toString())) { launchSingleTop = true }
                    },
                    onRecurring = { navController.navigate(RecurringListDestination) { launchSingleTop = true } },
                ),
                gate = if (locked) {
                    VersionGateBannerState(updateAvailable?.versionName, updateAvailable?.sizeBytes?.let(::megabytes))
                } else {
                    null
                },
                onUpdate = openUpdate,
            )
        }
        screen<TransactionsDestination> { entry ->
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
        screen<TemplatesDestination> {
            TemplatesRoute(
                onBack = { navController.popBackStack() },
                onUse = { navController.navigate(TransactionEditDestination(templateId = it)) { launchSingleTop = true } },
                onEdit = { navController.navigate(TemplateEditDestination(it)) { launchSingleTop = true } },
                onNew = { navController.navigate(TemplateEditDestination()) { launchSingleTop = true } },
            )
        }
        screen<RecurringListDestination> {
            RecurringListRoute(
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(TemplateEditDestination(it)) { launchSingleTop = true } },
                onNew = { navController.navigate(TemplateEditDestination()) { launchSingleTop = true } },
            )
        }
        screen<TemplateEditDestination> {
            TemplateEditRoute(onDone = { navController.popBackStack() })
        }
        screen<BudgetsDestination> {
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
        screen<BudgetDetailDestination> {
            BudgetDetailRoute(
                onBack = { navController.popBackStack() },
                onEditLimit = { id, month -> navController.navigate(BudgetEditDestination(id, month.year, month.month.number)) },
                // A limit inside the folder: its own envelope on top; back returns to the folder.
                onOpenEnvelope = { id, month -> navController.navigate(BudgetDetailDestination(id, month.year, month.month.number)) },
                onTransactionClick = { navController.navigate(TransactionEditDestination(transactionId = it)) },
            )
        }
        screen<BudgetEditDestination> {
            BudgetEditRoute(
                onDone = { navController.popBackStack() },
                // A limit inside the folder: its own editor on top; back returns to the folder.
                onOpenCategory = { id, month -> navController.navigate(BudgetEditDestination(id, month.year, month.month.number)) },
            )
        }
        screen<AccountsDestination> {
            AccountsRoute(
                onBack = { navController.popBackStack() },
                onAccountClick = { navController.navigate(AccountEditDestination(it)) },
                onAddAccount = { navController.navigate(AccountEditDestination()) },
            )
        }
        screen<ReportsDestination> {
            ReportsRoute(
                onAllCategories = { navController.navigate(ReportCategoriesDestination) { launchSingleTop = true } },
                onCategory = { navController.navigate(it) { launchSingleTop = true } },
                onMember = { preset ->
                    navController.navigateToTab(TopLevel.TRANSACTIONS)
                    navController.getBackStackEntry<TransactionsDestination>()
                        .savedStateHandle[PRESET_MEMBER] = preset.encode()
                },
                onCapital = { navController.navigate(it) { launchSingleTop = true } },
            )
        }
        screen<CapitalMonthDestination> {
            CapitalMonthRoute(
                onBack = { navController.popBackStack() },
                onAccount = { navController.navigate(AccountEditDestination(it)) },
            )
        }
        screen<ReportCategoriesDestination> { entry ->
            // The tab's entry sits under this one: «Все категории» shows its period, currency and data.
            val tab = remember(entry) { navController.getBackStackEntry<ReportsDestination>() }
            ReportCategoriesRoute(
                viewModel = hiltViewModel(tab),
                onBack = { navController.popBackStack() },
                onCategory = { navController.navigate(it) { launchSingleTop = true } },
            )
        }
        screen<MonthReviewDestination> { entry ->
            val route = entry.toRoute<MonthReviewDestination>()
            MonthReviewRoute(
                onBack = { navController.popBackStack() },
                onCategory = { navController.navigate(it) { launchSingleTop = true } },
                onLimit = { id -> navController.navigate(BudgetDetailDestination(id, route.year, route.month)) { launchSingleTop = true } },
                onAllLimits = { navController.navigate(MonthReviewLimitsDestination) { launchSingleTop = true } },
            )
        }
        screen<MonthReviewLimitsDestination> { entry ->
            // The review's entry sits under this one: the limits are of its month, from its data.
            val review = remember(entry) { navController.getBackStackEntry<MonthReviewDestination>() }
            val route = review.toRoute<MonthReviewDestination>()
            MonthLimitsRoute(
                viewModel = hiltViewModel(review),
                onBack = { navController.popBackStack() },
                onLimit = { id -> navController.navigate(BudgetDetailDestination(id, route.year, route.month)) { launchSingleTop = true } },
            )
        }
        screen<ReportCategoryDestination> {
            ReportCategoryRoute(
                onBack = { navController.popBackStack() },
                onTransactionClick = { navController.navigate(TransactionEditDestination(transactionId = it)) },
            )
        }
        dialog<ConverterDestination>(dialogProperties = CoShellSheetDialogProperties) {
            CoShellSheet(onClosed = { navController.popBackStack() }) { close -> ConverterRoute(onClose = close) }
        }
        dialog<AccountEditDestination>(dialogProperties = CoShellSheetDialogProperties) {
            CoShellSheet(onClosed = { navController.popBackStack() }) { close -> AccountEditRoute(onDone = close) }
        }
        screen<MoreDestination> {
            MoreRoute(
                navController = navController,
                availableVersion = updateAvailable?.versionName,
                onStartUpdate = {
                    updateAvailable?.let(updates::startUpdate)
                    navController.navigate(UpdateDestination)
                },
            )
        }
        screen<SettingsDestination> {
            SettingsScreen(
                SettingsActions(
                    onBack = { navController.popBackStack() },
                    onLook = { navController.navigate(AppearanceDestination) },
                    onScreens = { navController.navigate(ScreensSettingsDestination) },
                    onMoney = { navController.navigate(MoneySettingsDestination) },
                    onInput = { navController.navigate(InputSettingsDestination) },
                    onNotices = { navController.navigate(NoticeSettingsDestination) },
                    onData = { navController.navigate(DataSettingsDestination) },
                ),
            )
        }
        screen<AppearanceDestination> {
            AppearanceRoute(onBack = { navController.popBackStack() })
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
        screen<UpdateDestination> {
            UpdateRoute(
                onBack = { navController.popBackStack() },
                onPastReleases = { navController.navigate(PastReleasesDestination) { launchSingleTop = true } },
            )
        }
        screen<PastReleasesDestination> {
            PastReleasesRoute(onBack = { navController.popBackStack() })
        }
        screen<FeedbackDestination> { entry ->
            FeedbackRoute(
                onBack = { navController.popBackStack() },
                onOpen = { navController.navigate(FeedbackDetailDestination(it)) { launchSingleTop = true } },
                prefill = entry.toRoute<FeedbackDestination>().prefill,
            )
        }
        screen<FeedbackDetailDestination> { entry ->
            FeedbackDetailRoute(entry.toRoute<FeedbackDetailDestination>().sentAtMillis, onBack = { navController.popBackStack() })
        }
        screen<RatesDestination> {
            RatesRoute(
                onBack = { navController.popBackStack() },
                onConverter = { navController.navigate(ConverterDestination) { launchSingleTop = true } },
            )
        }
        screen<ReportCurrenciesDestination> {
            ReportCurrenciesRoute(onBack = { navController.popBackStack() })
        }
        screen<FamilyDestination> {
            FamilyRoute(onBack = { navController.popBackStack() })
        }
        screen<BackupDestination> {
            BackupRoute(
                onBack = { navController.popBackStack() },
                onWipeData = { navController.navigate(WipeDataDestination) },
            )
        }
        screen<WalletImportDestination> {
            WalletImportRoute(onBack = { navController.popBackStack() })
        }
        screen<CategoriesDestination> {
            CategoriesRoute(
                onBack = { navController.popBackStack() },
                onOpen = { navController.navigate(it) },
            )
        }
        dialog<CategoryEditDestination>(dialogProperties = CoShellSheetDialogProperties) {
            CoShellSheet(onClosed = { navController.popBackStack() }) { close -> CategoryEditRoute(onDone = close) }
        }
    }

    // «Изменить сумму» / «Провести» on a payment reminder (#405): the «Провести» sheet on that payment.
    if (paymentLinks != null) {
        val link by paymentLinks.link.collectAsStateWithLifecycle()
        LaunchedEffect(link) {
            val payment = link ?: return@LaunchedEffect
            navController.navigate(TransactionEditDestination(templateId = payment.templateId, paymentDate = payment.date.toString())) {
                launchSingleTop = true
            }
            paymentLinks.consumed()
        }
    }
}
