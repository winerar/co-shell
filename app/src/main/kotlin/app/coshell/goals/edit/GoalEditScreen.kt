package app.coshell.goals.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.charts.CoShellMeter
import app.coshell.core.designsystem.components.CoShellAccountRibbon
import app.coshell.core.designsystem.components.CoShellDatePicker
import app.coshell.core.designsystem.components.CoShellDetailTile
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellIconButton
import app.coshell.core.designsystem.components.CoShellOutlinedButton
import app.coshell.core.designsystem.components.CoShellSectionLink
import app.coshell.core.designsystem.components.CoShellSwitchLine
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.components.rememberDatePick
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.form.AmountSheetField
import app.coshell.ui.form.ChoiceSheet
import app.coshell.ui.form.ConfirmDeleteDialog
import app.coshell.ui.sheet.EditSheetLayout
import app.coshell.ui.sheet.ModalSheet
import app.coshell.core.designsystem.R as DesignR

/** What the sheet can ask for. */
data class GoalEditActions(
    val onName: (String) -> Unit = {},
    val onAmount: (String) -> Unit = {},
    val onAccount: (String) -> Unit = {},
    val onDeadline: (Long?) -> Unit = {},
    val onYearly: (Boolean) -> Unit = {},
    val onSave: () -> Unit = {},
    val onDelete: () -> Unit = {},
    val onTopUp: () -> Unit = {},
)

/**
 * «Новая цель» / «Цель» (#768, boards `GoalNew`, `GoalDetail`): an existing goal shows where it stands and «Пополнить» (a transfer
 * into its account); the form below — name, target, deadline, account (ribbon), «Каждый год» and the plan a month.
 */
@Composable
fun GoalEditScreen(state: GoalEditUiState, actions: GoalEditActions) {
    var accountsOpen by rememberSaveable { mutableStateOf(false) }
    var deadlineOpen by rememberSaveable { mutableStateOf(false) }
    var deleteAsk by rememberSaveable { mutableStateOf(false) }
    EditSheetLayout(
        title = stringResource(if (state.isNew) R.string.goals_new_title else R.string.goals_edit_title),
        onSave = actions.onSave,
        titleAction = if (state.isNew) null else {
            {
                CoShellIconButton(onClick = { deleteAsk = true }) {
                    Icon(painterResource(DesignR.drawable.ic_delete), contentDescription = stringResource(R.string.goals_delete))
                }
            }
        },
    ) {
        if (state.isLoading) return@EditSheetLayout
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
            state.progress?.let { ProgressBlock(it, actions.onTopUp) }
            CoShellTextField(
                value = state.name,
                onValueChange = actions.onName,
                label = stringResource(R.string.goals_field_name),
                error = if (GoalError.NAME in state.errors) stringResource(R.string.goals_error_name) else null,
                modifier = Modifier.fillMaxWidth(),
            )
            AmountSheetField(
                value = state.amount,
                onValueChange = actions.onAmount,
                label = stringResource(R.string.goals_field_target),
                currency = state.currency,
                error = if (GoalError.TARGET in state.errors) stringResource(R.string.goals_error_target) else null,
                modifier = Modifier.fillMaxWidth(),
            )
            CoShellDetailTile(
                icon = painterResource(DesignR.drawable.ic_calendar_today),
                label = stringResource(R.string.goals_field_deadline),
                value = state.deadlineText ?: stringResource(R.string.goals_deadline_none),
                onClick = { deadlineOpen = true },
                empty = state.deadlineText == null,
            )
            if (state.deadlineText != null) CoShellSectionLink(stringResource(R.string.goals_deadline_none), onClick = { actions.onDeadline(null) })
            Text(stringResource(R.string.goals_field_account), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            CoShellAccountRibbon(
                accounts = state.ribbon,
                selectedId = state.accountId,
                onSelect = actions.onAccount,
                onAll = { accountsOpen = true },
                allLabel = stringResource(R.string.picker_all),
            )
            if (GoalError.ACCOUNT in state.errors) CoShellHint(stringResource(R.string.goals_error_account))
            CoShellSwitchLine(
                title = stringResource(R.string.goals_yearly),
                subtitle = if (GoalError.YEARLY in state.errors) stringResource(R.string.goals_error_yearly) else stringResource(R.string.goals_yearly_hint),
                checked = state.yearly,
                onCheckedChange = actions.onYearly,
            )
            PlanHint(state)
        }
    }
    if (accountsOpen) {
        ChoiceSheet(
            title = stringResource(R.string.goals_field_account),
            options = state.accounts,
            selected = state.accountId,
            onPick = { accountsOpen = false; actions.onAccount(it) },
            onDismiss = { accountsOpen = false },
        )
    }
    if (deadlineOpen) {
        val pick = rememberDatePick(state.deadlineMillisUtc ?: System.currentTimeMillis())
        ModalSheet(
            title = stringResource(R.string.goals_field_deadline),
            actionLabel = stringResource(R.string.keypad_done),
            onAction = { close -> pick.millis?.let(actions.onDeadline); close() },
            onClosed = { deadlineOpen = false },
        ) { CoShellDatePicker(pick) }
    }
    if (deleteAsk) {
        ConfirmDeleteDialog(
            title = R.string.goals_delete_ask,
            text = R.string.goals_delete_text,
            onConfirm = { deleteAsk = false; actions.onDelete() },
            onDismiss = { deleteAsk = false },
        )
    }
}

/** Saved of target with the meter and «Пополнить» (board `GoalDetail`). */
@Composable
private fun ProgressBlock(p: GoalProgressUi, onTopUp: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        AmountText(p.saved, AmountTitleStyle)
        Text(
            if (p.reached) stringResource(R.string.goals_reached) else stringResource(R.string.goals_progress, p.saved, p.target, p.left),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CoShellMeter(p.fraction, MaterialTheme.colorScheme.primary, Modifier.fillMaxWidth())
        if (!p.reached) {
            CoShellOutlinedButton(onClick = onTopUp, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.goals_top_up)) }
        }
    }
}

/** «Откладывайте ≈ 125 USD в месяц, чтобы успеть к 1 июля», or why there is no plan — or the account hint. */
@Composable
private fun PlanHint(state: GoalEditUiState) {
    val text = when {
        state.planReached -> stringResource(R.string.goals_plan_reached)
        state.planPerMonth.isNotEmpty() -> stringResource(R.string.goals_plan, state.planPerMonth, state.planDeadline)
        else -> stringResource(R.string.goals_account_hint)
    }
    CoShellHint(text)
}
