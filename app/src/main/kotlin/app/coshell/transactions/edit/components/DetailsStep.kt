package app.coshell.transactions.edit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import app.coshell.core.designsystem.tokens.PillShape
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.model.TransactionType
import app.coshell.transactions.edit.TransactionEditActions
import app.coshell.transactions.edit.TransactionEditUiState
import app.coshell.transactions.edit.TransactionEditUiState.EntryStep
import app.coshell.transactions.edit.TransactionEditUiState.KeypadTarget
import app.coshell.ui.form.DateChips
import app.coshell.ui.format.formatDayMonth

/**
 * Step 2 of the entry sheet, built bottom-up for one thumb: the keypad with «Готово» at the bottom, the
 * date/note/receipt chips over it, the categories and the big amount above; the chosen account sits on top
 * as a chip — a tap goes back to the picker.
 */
@Composable
internal fun DetailsStep(state: TransactionEditUiState, actions: TransactionEditActions, modifier: Modifier = Modifier) {
    var noteOpen by rememberSaveable { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        Header(state, actions)
        Column(
            Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(horizontal = Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            AmountSlot(
                label = if (state.hasReceipt) stringResource(R.string.transaction_amount_debited) else null,
                text = state.amount,
                currency = state.account?.currency,
                active = state.keypadTarget == KeypadTarget.AMOUNT,
                error = state.amountError,
                onClick = { actions.onKeypadTarget(KeypadTarget.AMOUNT) },
                big = true,
                hint = state.suggestedRateDate?.takeIf { state.amountSuggested }?.let {
                    stringResource(R.string.transaction_amount_suggested, formatDayMonth(it))
                },
                color = amountColor(state.type),
            )
            CoShellExpandable(state.needsToAmount) {
                AmountSlot(
                    label = stringResource(R.string.transaction_to_amount),
                    text = state.toAmount,
                    currency = state.toAccount?.currency,
                    active = state.keypadTarget == KeypadTarget.TO_AMOUNT,
                    error = state.toAmountError,
                    onClick = { actions.onKeypadTarget(KeypadTarget.TO_AMOUNT) },
                    hint = state.toAccount?.let { stringResource(R.string.transaction_to_amount_hint, it.name) },
                )
            }
            CoShellExpandable(state.hasReceipt) { ReceiptFields(state, actions) }
            CoShellExpandable(!state.isTransfer) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    CategoryGrid(state.categories, state.categoryId, state.openFolderId, actions.onCategorySelect)
                    if (state.categoryError) {
                        Text(
                            stringResource(R.string.transaction_category_required),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
            CoShellExpandable(noteOpen || state.note.isNotEmpty()) {
                CoShellTextField(
                    value = state.note,
                    onValueChange = actions.onNoteChange,
                    label = stringResource(R.string.transaction_note),
                    singleLine = false,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = Spacing.l),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        ) {
            DateChips(state.date, state.today, state.dateMillisUtc, actions.onDateSelect, actions.onDatePicked)
            CoShellFilterChip(
                selected = noteOpen || state.note.isNotEmpty(),
                onClick = { noteOpen = !noteOpen },
                label = { Text(stringResource(R.string.transaction_note_add)) },
            )
            if (!state.isTransfer) {
                CoShellFilterChip(
                    selected = state.receiptEnabled,
                    onClick = { actions.onReceiptToggle(!state.receiptEnabled) },
                    label = { Text(stringResource(R.string.transaction_receipt_chip)) },
                )
            }
        }
        CoShellKeypad(
            onKey = actions.onKey,
            doneLabel = stringResource(R.string.keypad_done),
            backspaceLabel = stringResource(R.string.keypad_backspace),
            showDecimal = fractionOf(state) > 0,
            modifier = Modifier.padding(horizontal = Spacing.m).padding(bottom = Spacing.m),
        )
    }
}

/**
 * The chosen account (→ the target of a transfer) as chips; a tap returns to that picker. On the right:
 * «Дублировать» and «Удалить» for a saved operation, «Копия · сегодня» for a copy (mockup 19В).
 */
@Composable
private fun Header(state: TransactionEditUiState, actions: TransactionEditActions) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = Spacing.l),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        state.account?.let { AccountChip(it) { actions.onStepChange(EntryStep.ACCOUNT) } }
        if (state.isTransfer) {
            Icon(
                painterResource(DesignR.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            state.toAccount?.let { AccountChip(it) { actions.onStepChange(EntryStep.TO_ACCOUNT) } }
        }
        Spacer(Modifier.weight(1f))
        if (!state.isNew) {
            CoShellIconButton(onClick = actions.onDuplicate) {
                Icon(painterResource(DesignR.drawable.ic_content_copy), contentDescription = stringResource(R.string.action_duplicate))
            }
            CoShellIconButton(onClick = actions.onDelete) {
                Icon(painterResource(DesignR.drawable.ic_delete), contentDescription = stringResource(R.string.action_delete))
            }
        }
        if (state.isCopy) CopyLabel()
    }
}

/** «Копия · сегодня»: a quiet violet pill, the colour of view modes (review 2026-11-10). */
@Composable
private fun CopyLabel() {
    val colors = MaterialTheme.colorScheme
    Text(
        stringResource(R.string.transaction_copy_today),
        style = MaterialTheme.typography.labelLarge,
        color = colors.onSecondaryContainer,
        modifier = Modifier.background(colors.secondaryContainer, PillShape).padding(horizontal = Spacing.m, vertical = Spacing.xs + Spacing.xxs),
    )
}

@Composable
private fun amountColor(type: TransactionType) = when (type) {
    TransactionType.EXPENSE -> LocalAmountPalette.current.expense
    TransactionType.INCOME -> LocalAmountPalette.current.income
    TransactionType.TRANSFER -> MaterialTheme.colorScheme.onSurface
}

/** Fraction digits of the amount the keypad types into: no `,` key for AMD-like currencies. */
private fun fractionOf(state: TransactionEditUiState): Int = when (state.keypadTarget) {
    KeypadTarget.AMOUNT -> state.account?.fractionDigits ?: 2
    KeypadTarget.TO_AMOUNT -> state.toAccount?.fractionDigits ?: 2
    KeypadTarget.RECEIPT -> 2
}
