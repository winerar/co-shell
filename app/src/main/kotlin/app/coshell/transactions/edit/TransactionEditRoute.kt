package app.coshell.transactions.edit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.coshell.core.designsystem.motion.LocalAddConfirm
import app.coshell.core.designsystem.components.keypad.KeypadKey
import app.coshell.core.model.TransactionType
import app.coshell.templates.strip.TemplateStripViewModel
import app.coshell.transactions.edit.tip.TipUi
import kotlinx.datetime.LocalDate
import app.coshell.gate.LocalInputLock

/** Connects [TransactionEditScreen] to its ViewModel. */
@Composable
fun TransactionEditRoute(
    onDone: () -> Unit,
    onAddAccount: () -> Unit,
    onDuplicate: (String) -> Unit = {},
    onAllTemplates: () -> Unit = {},
    onScanReceipt: () -> Unit = {},
    onNewTemplate: () -> Unit = {},
    onSendReceiptSample: () -> Unit = {},
    templateStrip: TemplateStripViewModel = hiltViewModel(),
    viewModel: TransactionEditViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lock = LocalInputLock.current
    val haptics = LocalHapticFeedback.current
    val addConfirm = LocalAddConfirm.current
    var saving by remember { mutableStateOf(false) }
    LaunchedEffect(state.isDone) {
        if (!state.isDone) return@LaunchedEffect
        // A light tick confirms the saved operation (design-questions §6); delete has its snackbar.
        // The «+» answers with a check mark (design-refactor Step 4).
        if (saving) {
            haptics.performHapticFeedback(HapticFeedbackType.Confirm)
            addConfirm.fire()
        }
        onDone()
    }
    val strip by templateStrip.state.collectAsStateWithLifecycle()
    TransactionEditScreen(
        state = state,
        templates = strip,
        actions = TransactionEditActions(
            onAddAccount = onAddAccount,
            onTypeChange = viewModel::onTypeChange,
            onAmountChange = viewModel::onAmountChange,
            onToAmountChange = viewModel::onToAmountChange,
            onAccountSelect = viewModel::onAccountSelect,
            onToAccountSelect = viewModel::onToAccountSelect,
            onCategorySelect = viewModel::onCategorySelect,
            onDateSelect = viewModel::onDateSelect,
            onDatePicked = viewModel::onDatePicked,
            onNoteChange = viewModel::onNoteChange,
            onReceiptToggle = viewModel::onReceiptToggle,
            onRefundToggle = viewModel::onRefundToggle,
            onReceiptCurrencyChange = viewModel::onReceiptCurrencyChange,
            onReceiptAmountChange = viewModel::onReceiptAmountChange,
            onSave = lock.guard {
                saving = true
                viewModel.save()
            },
            onDelete = lock.guard(viewModel::delete),
            onDuplicate = { viewModel.transactionId?.let(onDuplicate) },
            onStepChange = viewModel::onStepChange,
            onKeypadTarget = viewModel::onKeypadTarget,
            onTemplatePick = viewModel::onTemplatePick,
            onTemplateClear = viewModel::onTemplateClear,
            onPaymentSkip = lock.guard(viewModel::onPaymentSkip),
            onAllTemplates = onAllTemplates,
            onScanReceipt = onScanReceipt,
            onNewTemplate = onNewTemplate,
            onSaveAsTemplate = lock.guard2(viewModel::saveAsTemplate),
            onTagToggle = viewModel::onTagToggle,
            onTagCreate = lock.guard2(viewModel::onTagCreate),
            onTipOpen = viewModel::onTipOpen,
            onTipChange = viewModel::onTipChange,
            onTipClose = viewModel::onTipClose,
            onTipKey = { key, note -> if (key == KeypadKey.Done && lock.locked) lock.onBlocked() else viewModel.onTipKey(key, note) },
            onTipCategory = viewModel::onTipCategory,
            onTipSave = lock.guard1(viewModel::onTipSave),
            onScanDuplicateAccept = viewModel::onScanDuplicateAccept,
            onScanDuplicateCancel = viewModel::onScanDuplicateCancel,
            onReceiptUnreadManual = viewModel::onReceiptUnreadDismiss,
            onReceiptUnreadSend = {
                viewModel.onReceiptUnreadDismiss()
                onSendReceiptSample()
            },
            onKey = { key ->
                if (key == KeypadKey.Done && lock.locked) {
                    lock.onBlocked()
                } else {
                    if (key == KeypadKey.Done) saving = true
                    viewModel.onKey(key)
                }
            },
        ),
    )
}

/** Everything the screen can ask for; defaults make previews short. */
data class TransactionEditActions(
    val onAddAccount: () -> Unit = {},
    val onTypeChange: (TransactionType) -> Unit = {},
    val onAmountChange: (String) -> Unit = {},
    val onToAmountChange: (String) -> Unit = {},
    val onAccountSelect: (String) -> Unit = {},
    val onToAccountSelect: (String) -> Unit = {},
    val onCategorySelect: (String) -> Unit = {},
    val onDateSelect: (LocalDate) -> Unit = {},
    val onDatePicked: (Long) -> Unit = {},
    val onNoteChange: (String) -> Unit = {},
    val onReceiptToggle: (Boolean) -> Unit = {},
    /** «↩ Возврат» on an expense (#208). */
    val onRefundToggle: (Boolean) -> Unit = {},
    val onReceiptCurrencyChange: (String) -> Unit = {},
    val onReceiptAmountChange: (String) -> Unit = {},
    val onSave: () -> Unit = {},
    val onDelete: () -> Unit = {},
    val onDuplicate: () -> Unit = {},
    val onStepChange: (TransactionEditUiState.EntryStep) -> Unit = {},
    val onKeypadTarget: (TransactionEditUiState.KeypadTarget) -> Unit = {},
    val onKey: (KeypadKey) -> Unit = {},
    val onTemplatePick: (String) -> Unit = {},
    val onTemplateClear: () -> Unit = {},
    /** «Пропустить этот раз» of a payment from «Скоро» (#404). */
    val onPaymentSkip: () -> Unit = {},
    val onAllTemplates: () -> Unit = {},
    val onScanReceipt: () -> Unit = {},
    val onNewTemplate: () -> Unit = {},
    val onSaveAsTemplate: (name: String, rememberAmount: Boolean) -> Unit = { _, _ -> },
    /** A tag pill or a row of the «Теги» sheet (#293). */
    val onTagToggle: (String) -> Unit = {},
    val onTagCreate: (name: String, color: Long?) -> Unit = { _, _ -> },
    /** «＋ Чаевые» (#322): the sheet opens once the purchase is valid. */
    val onTipOpen: () -> Unit = {},
    val onTipChange: ((TipUi) -> TipUi) -> Unit = {},
    val onTipClose: () -> Unit = {},
    val onTipKey: (KeypadKey, note: String) -> Unit = { _, _ -> },
    val onTipCategory: (choice: String, newName: String) -> Unit = { _, _ -> },
    val onTipSave: (note: String) -> Unit = {},
    /** The «Этот чек уже записан» window of a scanned receipt (#526). */
    val onScanDuplicateAccept: () -> Unit = {},
    val onScanDuplicateCancel: () -> Unit = {},
    /** «Не удалось прочитать чек» of a shared receipt (#561): enter by hand, or send the file as a sample. */
    val onReceiptUnreadManual: () -> Unit = {},
    val onReceiptUnreadSend: () -> Unit = {},
)
