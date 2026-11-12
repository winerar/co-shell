package app.coshell.budgets.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import app.coshell.R
import app.coshell.budgets.edit.components.BudgetPickers
import app.coshell.budgets.edit.components.BudgetPicker
import app.coshell.budgets.edit.components.InsideFolder
import app.coshell.core.designsystem.components.CoShellAlertDialog
import app.coshell.core.designsystem.components.CoShellAmountField
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellFilterChip
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellMessage
import app.coshell.core.designsystem.components.CoShellSegmentedButton
import app.coshell.core.designsystem.components.CoShellSwitchRow
import app.coshell.core.designsystem.components.CoShellTextButton
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.form.ChoiceField
import app.coshell.ui.format.formatMonthYearFrom
import app.coshell.ui.list.SectionTitle
import app.coshell.ui.top.ScreenTopBar

/**
 * Limit editor (mockup `BudEdit`): «Папка | Категория», what the limit is on, «В месяц» with the currency and the
 * 3-month average, the folder's inner limits, «Переносить остаток», a note, «Сохранить» and «Убрать лимит».
 */
@Composable
fun BudgetEditScreen(state: BudgetEditUiState, actions: BudgetEditActions) {
    var picker by rememberSaveable { mutableStateOf<BudgetPicker?>(null) }
    var confirmRemove by rememberSaveable { mutableStateOf(false) }
    val month = state.month
    Scaffold(
        topBar = {
            ScreenTopBar(
                stringResource(if (state.hasLimit || state.locked) R.string.budget_edit_title else R.string.budgets_new),
                onBack = actions.onBack,
                subtitle = state.chosen?.label,
            )
        },
    ) { padding ->
        if (state.isLoading || month == null) return@Scaffold
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
        ) {
            if (!state.locked) KindSelector(state.kind, actions.onKindChange)
            if (state.choices.isEmpty() && state.kind == BudgetKind.FOLDER) {
                CoShellHint(stringResource(R.string.budget_no_folders))
            } else {
                ChoiceField(
                    label = stringResource(R.string.budget_what),
                    value = state.chosen?.label,
                    placeholder = stringResource(if (state.kind == BudgetKind.FOLDER) R.string.budget_choose_folder else R.string.budget_choose_category),
                    isError = state.categoryError,
                    onClick = { if (!state.locked) picker = BudgetPicker.CATEGORY },
                )
            }
            CoShellAmountField(
                value = state.amount,
                onValueChange = actions.onAmountChange,
                label = stringResource(R.string.budget_amount),
                currency = state.currency,
                error = stringResource(R.string.budget_amount_error).takeIf { state.amountError },
                hint = state.average?.let { stringResource(R.string.budget_average, it) },
            )
            if (state.currencies.size > 1) {
                CoShellFilterChip(selected = false, onClick = { picker = BudgetPicker.CURRENCY }, label = {
                    Text(stringResource(R.string.budget_currency) + " · " + state.currency)
                })
            }
            if (state.inside.isNotEmpty()) {
                SectionTitle(stringResource(R.string.budget_inside))
                InsideFolder(state.inside, actions.onOpenInside)
            }
            CoShellSwitchRow(
                title = stringResource(R.string.budget_rollover),
                subtitle = stringResource(R.string.budget_rollover_hint),
                checked = state.rollover,
                onCheckedChange = actions.onRolloverChange,
            )
            CoShellTextField(
                value = state.note,
                onValueChange = actions.onNoteChange,
                label = stringResource(R.string.budget_note),
                singleLine = false,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
            CoShellHint(stringResource(R.string.budget_from_month, formatMonthYearFrom(month)))
            if (state.saveFailed) CoShellMessage(stringResource(R.string.budget_save_failed), isError = true)
            CoShellButton(onClick = actions.onSave, modifier = Modifier.fillMaxWidth().padding(top = Spacing.s)) {
                Text(stringResource(R.string.action_save))
            }
            if (state.hasLimit) {
                CoShellTextButton(onClick = { confirmRemove = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.budget_remove), color = MaterialTheme.colorScheme.error)
                }
            }
        }
        if (confirmRemove) {
            CoShellAlertDialog(
                title = stringResource(R.string.budget_remove_confirm_title),
                text = stringResource(R.string.budget_remove_confirm_text, formatMonthYearFrom(month)),
                confirmLabel = stringResource(R.string.budget_remove),
                onConfirm = {
                    confirmRemove = false
                    actions.onRemove()
                },
                onDismiss = { confirmRemove = false },
                dismissLabel = stringResource(R.string.action_cancel),
                destructive = true,
            )
        }
    }
    BudgetPickers(picker, state, actions, onClose = { picker = null })
}

@Composable
private fun KindSelector(selected: BudgetKind, onSelect: (BudgetKind) -> Unit) {
    val kinds = BudgetKind.entries
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        kinds.forEachIndexed { index, kind ->
            CoShellSegmentedButton(
                selected = kind == selected,
                onClick = { onSelect(kind) },
                shape = SegmentedButtonDefaults.itemShape(index, kinds.size),
                label = { Text(stringResource(if (kind == BudgetKind.FOLDER) R.string.budget_kind_folder else R.string.budget_kind_category)) },
            )
        }
    }
}
