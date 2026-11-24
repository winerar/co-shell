package app.coshell.navigation

import app.coshell.hints.SheetCoach
import app.coshell.core.domain.hints.HintId
import app.coshell.R
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
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
import kotlinx.coroutines.flow.MutableStateFlow
import app.coshell.hints.HintBannerHost
import app.coshell.hints.HintTarget
import app.coshell.hints.LocalHintHost
import app.coshell.accounts.edit.AccountEditRoute
import app.coshell.privacy.HiddenPill
import app.coshell.receipt.ReceiptLinks
import app.coshell.receipt.scan.ReceiptScanRoute
import app.coshell.recurring.notify.PaymentLinks
import app.coshell.shortcuts.ShortcutLink
import app.coshell.shortcuts.ShortcutLinks
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
import app.coshell.converter.ConverterRoute
import app.coshell.home.HomeActions
import app.coshell.home.HomeRoute
import app.coshell.importer.wallet.WalletImportRoute
import app.coshell.rates.list.RatesRoute
import app.coshell.shake.ShakeEffect
import app.coshell.shake.ShakeSettingsViewModel
import app.coshell.shake.ShakeUndoViewModel
import app.coshell.core.domain.shake.ShakeIntent
import app.coshell.core.domain.shake.ShakeIntents
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import app.coshell.drafts.list.DraftsRoute
import app.coshell.drafts.notes.NotesRoute
import app.coshell.recurring.list.RecurringListRoute
import app.coshell.templates.edit.TemplateEditRoute
import app.coshell.templates.list.TemplatesRoute
import app.coshell.transactions.edit.TransactionEditRoute
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
import app.coshell.access.AccessLockedDialog
import app.coshell.access.AccessViewModel
import app.coshell.gate.InputLockViewModel
import app.coshell.gate.rememberInputLock
import app.coshell.home.components.VersionGateBannerState
import app.coshell.update.components.megabytes
import kotlinx.datetime.number

/** A short pause after the «Сохранено» snackbar leaves, before the shake card comes (they never share the screen). */
private const val SHAKE_HINT_PAUSE_MS = 600L

@Composable
fun CoShellNavHost(
    navController: NavHostController = rememberNavController(),
    shakeSettings: ShakeSettingsViewModel = hiltViewModel(),
    shakeUndo: ShakeUndoViewModel = hiltViewModel(),
    updates: UpdateNoticeViewModel = hiltViewModel(),
    greeting: GreetingViewModel = hiltViewModel(),
    crashes: CrashNoticeViewModel = hiltViewModel(),
    inputLock: InputLockViewModel = hiltViewModel(),
    access: AccessViewModel = hiltViewModel(),
    paymentLinks: PaymentLinks? = null,
    receiptLinks: ReceiptLinks? = null,
    shortcutLinks: ShortcutLinks? = null,
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
    val accessLocked by inputLock.accessLocked.collectAsStateWithLifecycle()
    val accessNote by access.note.collectAsStateWithLifecycle()
    val lock = rememberInputLock(locked, openUpdate, accessLocked, access::showNote)
    // «Доступ закрыт» (#666): on opening a closed app and after a blocked tap; the data can be saved from here.
    if (accessLocked && accessNote) {
        AccessLockedDialog(
            onEnterCode = access::changeCode,
            onBackup = {
                access.dismissNote()
                navController.navigate(BackupDestination) { launchSingleTop = true }
            },
            onDismiss = access::dismissNote,
        )
    }

    // «Месяц закончился» leaves a dot on «Отчёты» until that tab is opened (#513).
    val hintHost = LocalHintHost.current
    val reportsDot by (hintHost?.reportsDot ?: remember { MutableStateFlow(false) }).collectAsStateWithLifecycle()
    val onReports = backStackEntry?.destination.isOn(TopLevel.REPORTS)
    LaunchedEffect(onReports) { if (onReports) hintHost?.reportsOpened() }

    // Shake: within 4 s after a new operation was saved it takes the save back (#325, «Отменено · Вернуть»); otherwise → the
    // receipt QR scanner (#526; «Ввести вручную» there opens the empty sheet). Only on the tabs: never interrupts a form or another screen.
    val shakeEnabled by shakeSettings.enabled.collectAsStateWithLifecycle()
    ShakeEffect(
        enabled = shakeEnabled && onTab,
        onShake = lock.guard {
            if (shakeUndo.intent() == ShakeIntent.UNDO_LAST_SAVE) {
                shakeUndo.undoLast()
            } else {
                navController.navigate(ReceiptScanDestination) { launchSingleTop = true }
            }
        },
    )
    // «Ошиблись? Встряхните…» — one card after the first saved operation, when its snackbar is gone (#325, `hints.md`).
    LaunchedEffect(shakeEnabled, hintHost) {
        if (!shakeEnabled || hintHost == null) return@LaunchedEffect
        shakeUndo.saves.collectLatest {
            delay(ShakeIntents.UNDO_WINDOW.inWholeMilliseconds + SHAKE_HINT_PAUSE_MS)
            hintHost.offer(mapOf(HintId.SHAKE_UNDO to R.string.hint_shake_undo))
        }
    }

    MainShell(
        navController = navController,
        snackbar = snackbar,
        onAdd = lock.guard { navController.navigate(TransactionEditDestination()) { launchSingleTop = true } },
        onFan = lock.guard1 { target ->
            when (target) {
                FanTarget.RECEIPT -> navController.navigate(ReceiptScanDestination) { launchSingleTop = true }
                FanTarget.NOTES -> navController.navigate(NotesDestination) { launchSingleTop = true }
                FanTarget.TEMPLATE -> navController.navigate(TemplatesDestination) { launchSingleTop = true }
            }
        },
        moreBadge = updateAvailable != null,
        reportsBadge = reportsDot && !onReports,
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
            // Hint banner (#513): gesture card or warm pill; the update pill and the greeting go first.
            LocalHintHost.current?.let { host ->
                HintBannerHost(
                    host = host,
                    visible = onBar && updatePill == null && !(onHome && greetingVisible),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = Spacing.m),
                    onOpen = { target ->
                        when (target) {
                            HintTarget.BACKUP -> navController.navigate(BackupDestination) { launchSingleTop = true }
                            HintTarget.APPEARANCE -> navController.navigate(AppearanceDestination) { launchSingleTop = true }
                            HintTarget.MONTH_RECAP -> {
                                // «month_end» shows in the first days of a month: the month that just ended.
                                val last = java.time.YearMonth.now().minusMonths(1)
                                navController.navigate(MonthRecapDestination(last.year, last.monthValue)) { launchSingleTop = true }
                            }
                        }
                    },
                )
            }
            // Sums hidden by laying the phone down (#145): says how to show them; goes first of the quiet pills.
            HiddenPill(visible = onBar && updatePill == null, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = Spacing.m))
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
                    onReview = { month -> navController.navigate(MonthRecapDestination(month.year, month.month.number)) { launchSingleTop = true } },
                    onPostPayment = { templateId, date ->
                        navController.navigate(TransactionEditDestination(templateId = templateId, paymentDate = date.toString())) { launchSingleTop = true }
                    },
                    onPostCredit = { cardId ->
                        navController.navigate(TransactionEditDestination(type = "TRANSFER", creditCardId = cardId)) { launchSingleTop = true }
                    },
                    onRecurring = { navController.navigate(RecurringListDestination) { launchSingleTop = true } },
                    onDrafts = { navController.navigate(DraftsDestination) { launchSingleTop = true } },
                    onNotes = { navController.navigate(NotesDestination) { launchSingleTop = true } },
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
        dialog<TransactionEditDestination>(dialogProperties = CoShellSheetDialogProperties) { entry ->
            Box(Modifier.fillMaxSize()) {
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
                        onScanReceipt = lock.guard { navController.navigate(ReceiptScanDestination) { launchSingleTop = true } },
                        onNewTemplate = { navController.navigate(TemplateEditDestination()) { launchSingleTop = true } },
                        onSendReceiptSample = { navController.navigate(FeedbackDestination(receiptSample = true)) { launchSingleTop = true } },
                    )
                }
                // New entries only: the words about the sheet sit in the dimmed strip above it (#513, `SheetCoach`).
                if (entry.toRoute<TransactionEditDestination>().transactionId == null) {
                    SheetCoach(
                        mapOf(
                            HintId.ENTRY_MINIMAL to R.string.hint_entry_minimal,
                            HintId.KEYPAD_MATH to R.string.hint_keypad_math,
                            HintId.ENTRY_REFUND to R.string.hint_entry_refund,
                        ),
                        Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(horizontal = Spacing.l),
                    )
                }
            }
        }
        screen<ReceiptScanDestination> {
            ReceiptScanRoute(
                onReceipt = { text ->
                    navController.navigate(TransactionEditDestination(receiptQr = text)) {
                        popUpTo<ReceiptScanDestination> { inclusive = true }
                    }
                },
                onManual = {
                    navController.navigate(TransactionEditDestination()) { popUpTo<ReceiptScanDestination> { inclusive = true } }
                },
                onClose = { navController.popBackStack() },
            )
        }
        screen<TemplatesDestination> {
            TemplatesRoute(
                onBack = { navController.popBackStack() },
                onUse = { navController.navigate(TransactionEditDestination(templateId = it)) { launchSingleTop = true } },
                onEdit = { navController.navigate(TemplateEditDestination(it)) { launchSingleTop = true } },
                onNew = { navController.navigate(TemplateEditDestination()) { launchSingleTop = true } },
            )
        }
        screen<NotesDestination> {
            NotesRoute(
                onBack = { navController.popBackStack() },
                // Two steps: the note, then its drafts; «назад» from the drafts goes past the note.
                onDone = {
                    navController.navigate(DraftsDestination) {
                        popUpTo<NotesDestination> { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        screen<DraftsDestination> {
            DraftsRoute(
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(TransactionEditDestination(draftId = it)) { launchSingleTop = true } },
                onNotes = { navController.navigate(NotesDestination) { popUpTo<DraftsDestination> { inclusive = true }; launchSingleTop = true } },
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
        reportsGraph(navController)
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
            )
        }
        settingsGraph(navController)
        screen<UpdateDestination> {
            UpdateRoute(
                onBack = { navController.popBackStack() },
                onPastReleases = { navController.navigate(PastReleasesDestination) { launchSingleTop = true } },
                onFeedback = { navController.navigate(FeedbackDestination()) },
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
                receiptSample = entry.toRoute<FeedbackDestination>().receiptSample,
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

    // A receipt QR opened from a scanner or the camera (#526), or a receipt shared from another app: the entry sheet with
    // its amount and date; a share nothing could read opens the sheet blank.
    if (receiptLinks != null) {
        val link by receiptLinks.link.collectAsStateWithLifecycle()
        LaunchedEffect(link) {
            val shared = link ?: return@LaunchedEffect
            val destination = TransactionEditDestination(receiptQr = shared.text.ifBlank { null }?.takeIf { shared.readable }, receiptUnread = !shared.readable)
            navController.navigate(destination) { launchSingleTop = true }
            receiptLinks.consumed()
        }
    }

    // A launcher shortcut (#555): the same entry sheet «+» opens, of the chosen type or filled from a template.
    if (shortcutLinks != null) {
        val link by shortcutLinks.link.collectAsStateWithLifecycle()
        LaunchedEffect(link) {
            val shortcut = link ?: return@LaunchedEffect
            val destination = when (shortcut) {
                is ShortcutLink.NewOperation -> TransactionEditDestination(type = shortcut.type.name)
                is ShortcutLink.FromTemplate -> TransactionEditDestination(templateId = shortcut.templateId)
            }
            navController.navigate(destination) { launchSingleTop = true }
            shortcutLinks.consumed()
        }
    }
}
