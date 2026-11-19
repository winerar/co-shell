package app.coshell.transactions.edit.components

import app.coshell.core.designsystem.components.StatusTone
import app.coshell.core.designsystem.components.CoShellStatusChip
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.s - Spacing.xxs)) {
        if (state.showsTypeSwitch) TypeBar(state, actions)
        Header(state, actions)
        AccountRibbonBlock(state, actions)
        Column(
            Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.m),
            verticalArrangement = Arrangement.spacedBy(Spacing.s - Spacing.xxs),
        ) {
            AmountBlock(state, actions, amountColor(state.type, state.refund))
            state.scan?.let { ScanSourceLine(it) }
            CoShellExpandable(!state.isTransfer) { CategoryBlock(state, actions) }
            DetailsBlock(state, actions, onSaveAsTemplate = { saveAsOpen = true })
        }
        CoShellKeypad(
            onKey = actions.onKey,
            doneEnabled = state.canPost,
            // A payment from «Скоро» is posted, not just saved (board `Rec143Post`).
            doneLabel = stringResource(if (state.payment != null) R.string.recurring_post else R.string.keypad_done),
            backspaceLabel = stringResource(R.string.keypad_backspace),
            showDecimal = fractionOf(state) > 0,
            modifier = Modifier.padding(horizontal = Spacing.m).padding(bottom = Spacing.m),
        )
    }
    state.tip?.let { TipSheet(state, it, actions) }
    state.scan?.duplicate?.let { ScanDuplicateDialog(it, actions.onScanDuplicateAccept, actions.onScanDuplicateCancel) }
    if (saveAsOpen) SaveAsTemplateDialog(state, onSave = actions.onSaveAsTemplate, onDismiss = { saveAsOpen = false })
}

/**
 * The chosen account (→ the target of a transfer) as chips; a tap returns to that picker. On the right:
 * «Расход»/«Доход» for a new one, «Дублировать» and «Удалить» for a saved operation, «Копия · сегодня» for a copy (mockup 19В).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Header(state: TransactionEditUiState, actions: TransactionEditActions) {
    // A flow: when the chips and the words on the right do not fit one line (narrow screen, big font), the words drop
    // to the next line whole — «Расход» is never broken into letters.
    FlowRow(
        Modifier.fillMaxWidth().padding(horizontal = Spacing.l),
        itemVerticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        Spacer(Modifier.weight(1f))
        // Which kind of operation this is, in its colour (board `Main`, step 2): the type switch stays on step 1.
        if (state.refund) {
            Text(
                stringResource(R.string.transaction_refund_label),
                style = MaterialTheme.typography.labelLarge,
                color = amountColor(state.type, state.refund),
                maxLines = 1,
                softWrap = false,
            )
        }
        if (!state.isNew) {
            CoShellIconButton(onClick = actions.onDuplicate) {
                Icon(painterResource(DesignR.drawable.ic_content_copy), contentDescription = stringResource(R.string.action_duplicate))
            }
            CoShellIconButton(onClick = actions.onDelete) {
                Icon(painterResource(DesignR.drawable.ic_delete), contentDescription = stringResource(R.string.action_delete))
            }
        }
        if (state.isCopy) CopyLabel()
        val payment = state.payment
        if (payment != null) {
            TemplatePill(stringResource(R.string.payment_pill, payment.name, formatDayMonthShort(payment.date)), onClear = null, icon = DesignR.drawable.ic_refresh)
        } else {
            state.templateName?.let { TemplatePill(it, onClear = actions.onTemplateClear) }
        }
        state.savedTemplateName?.let { TemplatePill(it, onClear = null) }
    }
}

/** A new operation picks its kind here; the ☰ at the end opens all templates (the template row left the first step, #505). */
@Composable
private fun TypeBar(state: TransactionEditUiState, actions: TransactionEditActions) {
    Row(Modifier.fillMaxWidth().padding(horizontal = Spacing.l), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        TypeSelector(state.type, actions.onTypeChange, Modifier.weight(1f))
        CoShellIconButton(onClick = actions.onAllTemplates) {
            Icon(painterResource(DesignR.drawable.ic_format_list_bulleted), contentDescription = stringResource(R.string.templates_title))
        }
    }
}

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
