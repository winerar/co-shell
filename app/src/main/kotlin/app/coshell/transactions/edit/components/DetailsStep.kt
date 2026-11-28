package app.coshell.transactions.edit.components

import app.coshell.core.designsystem.components.StatusTone
import app.coshell.core.designsystem.components.CoShellStatusChip
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import app.coshell.core.domain.hints.HintId
import app.coshell.hints.WordHint
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import app.coshell.R
import app.coshell.core.designsystem.tokens.Sizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import app.coshell.core.designsystem.R as DesignR
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.components.keypad.CoShellKeypad
import app.coshell.core.designsystem.motion.CoShellExpandable
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.model.TransactionType
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.ui.format.formatDayMonthShort
import app.coshell.transactions.edit.TransactionEditUiState
import app.coshell.transactions.edit.showsPasteButton
import app.coshell.transactions.edit.TransactionEditUiState.EntryStep
import app.coshell.transactions.edit.TransactionEditUiState.KeypadTarget
import app.coshell.ui.form.DateChips
import app.coshell.ui.format.formatDayMonth

/**
 * Step 2 of the entry sheet — one screen, no scrolling (#290, owner picked А, board `Fb290A`): the chosen account
 * as a chip on top (a tap goes back to the picker), then one card per entity — «Сумма» (with the receipt line),
 * «Категория · частые», «Детали» 2×2 — and the keypad with «Готово» at the bottom, under the thumb.
 * A very short screen still scrolls the cards rather than cut them.
 */
@Composable
internal fun DetailsStep(state: TransactionEditUiState, actions: TransactionEditActions, modifier: Modifier = Modifier) {
    var saveAsOpen by rememberSaveable { mutableStateOf(false) }
    // A short phone (#818, poll «А»: 360×740) — one row of frequent categories and keys of 48 dp: still no scrolling.
    val short = LocalConfiguration.current.screenHeightDp.dp < Sizes.shortScreen
    val gap = if (short) Spacing.xxs else Spacing.s - Spacing.xxs
    Column(modifier, verticalArrangement = Arrangement.spacedBy(gap)) {
        state.unsaved?.takeIf { state.isNew }?.let { UnsavedPlate(it, actions.onUnsavedContinue, actions.onUnsavedDiscard) }
        if (state.showsTypeSwitch) TypeBar(state, actions)
        // An empty header would still take a gap of the column: only when it shows something.
        if (state.hasHeader) Header(state, actions, onSaveAsTemplate = { saveAsOpen = true })
        AccountRibbonBlock(state, actions)
        Column(
            Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.m),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            AmountBlock(state, actions, amountColor(state.type, state.refund))
            state.scan?.let { ScanSourceLine(it) }
            CoShellExpandable(!state.isTransfer) { CategoryBlock(state, actions, rows = if (short) 1 else 2) }
            DetailsBlock(state, actions)
        }
        CoShellKeypad(
            onKey = actions.onKey,
            doneEnabled = state.canPost,
            // A payment from «Скоро» is posted, not just saved (board `Rec143Post`).
            doneLabel = stringResource(if (state.payment != null) R.string.recurring_post else R.string.keypad_done),
            backspaceLabel = stringResource(R.string.keypad_backspace),
            showDecimal = fractionOf(state) > 0,
            compact = short,
            modifier = Modifier.padding(horizontal = Spacing.m).padding(bottom = if (short) Spacing.xs else Spacing.m),
        )
    }
    state.tip?.let { TipSheet(state, it, actions) }
    state.scan?.duplicate?.let { ScanDuplicateDialog(it, actions.onScanDuplicateAccept, actions.onScanDuplicateCancel) }
    if (state.receiptUnread) {
        ReceiptUnreadDialog(onManual = actions.onReceiptUnreadManual, onSendSample = actions.onReceiptUnreadSend)
    }
    state.receiptAccountAsk?.let { ReceiptAccountSheet(state, it, actions) }
    if (saveAsOpen) SaveAsTemplateDialog(state, onSave = actions.onSaveAsTemplate, onDismiss = { saveAsOpen = false })
}

/**
 * The chosen account (→ the target of a transfer) as chips; a tap returns to that picker. On the right:
 * «Расход»/«Доход» for a new one, the type switch, «Дублировать» and «Удалить» for a saved operation, «Копия · сегодня» for a copy (mockup 19В).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Header(state: TransactionEditUiState, actions: TransactionEditActions, onSaveAsTemplate: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        // A saved operation changes its type here, next to «В шаблон», «Дублировать» and «Удалить» (#509, poll «А», board
        // `EntryEdit`): one row — the switch shows icons when the words do not fit. Amount, account, date and note stay.
        if (!state.isNew) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                if (state.showsEditTypeSwitch) TypeSelector(state.type, actions.onTypeChange, Modifier.weight(1f)) else Spacer(Modifier.weight(1f))
                // «В шаблон» (#818): was a link over the «Детали» tiles, which are one line of chips now.
                if (state.savedTemplateName == null) {
                    CoShellIconButton(onClick = onSaveAsTemplate) {
                        Icon(painterResource(DesignR.drawable.ic_bookmark_add), contentDescription = stringResource(R.string.template_save_as_title))
                    }
                }
                CoShellIconButton(onClick = actions.onDuplicate) {
                    Icon(painterResource(DesignR.drawable.ic_content_copy), contentDescription = stringResource(R.string.action_duplicate))
                }
                CoShellIconButton(onClick = actions.onDelete) {
                    Icon(painterResource(DesignR.drawable.ic_delete), contentDescription = stringResource(R.string.action_delete))
                }
            }
        }
        val payment = state.payment
        if (state.isCopy || payment != null || state.templateName != null || state.savedTemplateName != null) {
            // A flow: on a narrow screen or a big font the labels drop to the next line whole — never broken into letters.
            FlowRow(
                Modifier.fillMaxWidth(),
                itemVerticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.End),
            ) {
                if (state.isCopy) CopyLabel()
                if (payment != null) {
                    TemplatePill(stringResource(R.string.payment_pill, payment.name, formatDayMonthShort(payment.date)), onClear = null, icon = DesignR.drawable.ic_refresh)
                } else {
                    state.templateName?.let { TemplatePill(it, onClear = actions.onTemplateClear) }
                }
                state.savedTemplateName?.let { TemplatePill(it, onClear = null) }
            }
        }
    }
}

/** A new operation picks its kind here; the ☰ at the end opens all templates (the template row left the first step, #505). */
@Composable
private fun TypeBar(state: TransactionEditUiState, actions: TransactionEditActions) {
    Column(Modifier.fillMaxWidth()) {
        if (state.scan == null) WordHint(HintId.SCAN_RECEIPT, R.string.hint_scan_receipt, Modifier.padding(horizontal = Spacing.l))
        Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.l), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
            TypeSelector(state.type, actions.onTypeChange, Modifier.weight(1f))
            // «Вставить из буфера» (#321): an icon like the QR and «Шаблоны», not a row of its own.
            if (state.showsPasteButton) PasteButton(actions)
            // The camera for a receipt QR (#526, board «QrBtnA»): amount and day of the receipt fill the sheet.
            CoShellIconButton(onClick = actions.onScanReceipt) {
                Icon(painterResource(DesignR.drawable.ic_qr_code_scanner), contentDescription = stringResource(R.string.scan_title))
            }
            CoShellIconButton(onClick = actions.onAllTemplates) {
                Icon(painterResource(DesignR.drawable.ic_format_list_bulleted), contentDescription = stringResource(R.string.templates_title))
            }
        }
        ClipboardNonePill(state, actions)
    }
}

private val TransactionEditUiState.showsEditTypeSwitch: Boolean
    get() = !isNew && payment == null

private val TransactionEditUiState.showsTypeSwitch: Boolean
    get() = isNew && !isCopy && payment == null && templateName == null && savedTemplateName == null

/** «Копия · сегодня»: the label of a copy (poll #367 А) — violet in the dot, like every mode. */
@Composable
private fun CopyLabel() {
    CoShellStatusChip(stringResource(R.string.transaction_copy_today), StatusTone.Manual)
}

/** A refund is money coming in: the income colour, though the sheet is on «Расход» (#208). */
@Composable
private fun amountColor(type: TransactionType, refund: Boolean = false) = when (type) {
    TransactionType.EXPENSE -> if (refund) LocalAmountPalette.current.income else LocalAmountPalette.current.expense
    TransactionType.INCOME -> LocalAmountPalette.current.income
    TransactionType.TRANSFER -> MaterialTheme.colorScheme.onSurface
}

/** The header row has something: the switch and buttons of a saved one, a copy, a payment or a template. */
private val TransactionEditUiState.hasHeader: Boolean
    get() = !isNew || isCopy || payment != null || templateName != null || savedTemplateName != null

/** «Готово» is quiet until the amount being typed is not empty (#505); other fields keep it on. */
internal val TransactionEditUiState.canPost: Boolean
    get() = when (keypadTarget) {
        KeypadTarget.AMOUNT -> amount.isNotBlank()
        KeypadTarget.TO_AMOUNT -> toAmount.isNotBlank()
        KeypadTarget.RECEIPT -> true
    }

/** Fraction digits of the amount the keypad types into: no `,` key for AMD-like currencies. */
private fun fractionOf(state: TransactionEditUiState): Int = when (state.keypadTarget) {
    KeypadTarget.AMOUNT -> state.account?.fractionDigits ?: 2
    KeypadTarget.TO_AMOUNT -> state.toAccount?.fractionDigits ?: 2
    KeypadTarget.RECEIPT -> 2
}
