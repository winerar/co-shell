package app.coshell.accounts.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import app.coshell.R
import app.coshell.accounts.edit.AccountEditActions
import app.coshell.accounts.edit.AccountEditUiState
import app.coshell.accounts.edit.DepositPayoutOrder
import app.coshell.core.designsystem.components.CoShellDatePicker
import app.coshell.core.designsystem.components.CoShellDetailTile
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellSectionLink
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.designsystem.components.CoShellTextField
import app.coshell.core.designsystem.components.rememberDatePick
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.model.DepositPayout
import app.coshell.ui.ai.BetaMark
import app.coshell.ui.format.formatDayMonthYear
import app.coshell.ui.sheet.ModalSheet
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import app.coshell.core.designsystem.R as DesignR

/**
 * «Условия вклада» (#634, step 3; boards `DepositEdit`, `DepositTerms`): the yearly rate, the opening day (optional), the closing day and how the interest is paid.
 * Only the expected income is counted from them; nothing is written by itself.
 */
@Composable
internal fun DepositTermsFields(state: AccountEditUiState, actions: AccountEditActions) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalAlignment = Alignment.CenterVertically) {
            BetaMark()
            CoShellHint(stringResource(R.string.account_deposit_beta_hint), Modifier.weight(1f))
        }
        CoShellTextField(
            value = state.depositRate,
            onValueChange = actions.onDepositRateChange,
            label = stringResource(R.string.account_deposit_rate),
            suffix = "%",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
        )
        DepositDateTile(R.string.account_deposit_start, state.depositStart, R.string.account_deposit_start_empty, actions.onDepositStartChange, clearable = true)
        DepositDateTile(R.string.account_deposit_end, state.depositEnd, R.string.account_deposit_empty, actions.onDepositEndChange, clearable = false)
        CoShellHint(stringResource(R.string.account_deposit_payout_title))
        CoShellSegmented(
            labels = DepositPayoutOrder.map { stringResource(depositPayoutLabel(it)) },
            selected = DepositPayoutOrder.indexOf(state.depositPayout),
            onSelect = { actions.onDepositPayoutChange(DepositPayoutOrder[it]) },
        )
        CoShellHint(stringResource(R.string.account_deposit_payout_hint))
    }
}


/** A date tile that opens the calendar; [clearable] adds «Убрать» for an optional day (the opening day). */
@Composable
private fun DepositDateTile(label: Int, value: String, empty: Int, onChange: (String) -> Unit, clearable: Boolean) {
    var open by rememberSaveable { mutableStateOf(false) }
    val day = runCatching { LocalDate.parse(value) }.getOrNull()
    CoShellDetailTile(
        icon = painterResource(DesignR.drawable.ic_calendar_today),
        label = stringResource(label),
        value = day?.let(::formatDayMonthYear) ?: stringResource(empty),
        onClick = { open = true },
        empty = day == null,
    )
    if (clearable && day != null) CoShellSectionLink(stringResource(R.string.account_deposit_clear), onClick = { onChange("") })
    if (open) {
        val pick = rememberDatePick(day?.toEpochMillisUtc() ?: System.currentTimeMillis())
        ModalSheet(
            title = stringResource(label),
            actionLabel = stringResource(R.string.keypad_done),
            onAction = { close ->
                pick.millis?.let { onChange(Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date.toString()) }
                close()
            },
            onClosed = { open = false },
        ) { CoShellDatePicker(pick) }
    }
}

internal fun depositPayoutLabel(payout: DepositPayout): Int = when (payout) {
    DepositPayout.AT_END -> R.string.account_deposit_payout_end
    DepositPayout.MONTHLY -> R.string.account_deposit_payout_monthly
    DepositPayout.CAPITALIZED -> R.string.account_deposit_payout_added
}

private fun LocalDate.toEpochMillisUtc(): Long = toEpochDays() * 86_400_000L

/** «8 % · до 15 марта 2027 · в конце срока» for the tile; «Не заданы» until the rate and the day are typed. */
@Composable
internal fun depositSummary(state: AccountEditUiState): String {
    val end = runCatching { LocalDate.parse(state.depositEnd) }.getOrNull()
    if (state.depositRate.isBlank() || end == null) return stringResource(R.string.account_deposit_none)
    return stringResource(R.string.account_deposit_summary, state.depositRate, formatDayMonthYear(end), stringResource(depositPayoutLabel(state.depositPayout)))
}
