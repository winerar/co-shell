package app.coshell.accounts.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.accounts.edit.AccountEditActions
import app.coshell.accounts.edit.AccountEditUiState
import app.coshell.core.designsystem.components.CoShellHint
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.form.AmountSheetField
import app.coshell.ui.form.DaySheetField
import app.coshell.ui.form.NumberSheetField
import app.coshell.ui.beta.BetaMark

/**
 * «Условия карты» (#686, boards `CreditTerms`, `CreditTermsSum`): the limit, the day of the month the payment is due and the minimum
 * payment — a percent of the debt or a sum, as the contract says. Interest and the grace period are not asked yet.
 */
@Composable
internal fun CreditTermsFields(state: AccountEditUiState, actions: AccountEditActions) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        // New and not yet checked against a real statement: «бета» until the owner's criteria are met (#686).
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s), verticalAlignment = Alignment.CenterVertically) {
            BetaMark()
            CoShellHint(stringResource(R.string.account_credit_beta_hint), Modifier.weight(1f))
        }
        AmountSheetField(
            value = state.creditLimit,
            onValueChange = actions.onCreditLimitChange,
            label = stringResource(R.string.account_credit_limit),
            currency = state.currency,
        )
        DaySheetField(
            value = state.creditPayDay,
            onValueChange = actions.onCreditPayDayChange,
            label = stringResource(R.string.account_credit_pay_day),
        )
        CoShellSegmented(
            labels = listOf(stringResource(R.string.account_credit_min_percent_tab), stringResource(R.string.account_credit_min_sum_tab)),
            selected = if (state.creditMinSum) 1 else 0,
            onSelect = { actions.onCreditMinSumChange(it == 1) },
        )
        if (state.creditMinSum) {
            AmountSheetField(
                value = state.creditMin,
                onValueChange = actions.onCreditMinChange,
                label = stringResource(R.string.account_credit_min_label),
                currency = state.currency,
            )
        } else {
            NumberSheetField(
                value = state.creditMin,
                onValueChange = actions.onCreditMinChange,
                label = stringResource(R.string.account_credit_min_label),
                maxWhole = PERCENT_WHOLE,
                fractionDigits = PERCENT_FRACTION,
                suffix = "%",
            )
        }
        CoShellHint(stringResource(R.string.account_credit_min_hint))
        // Grace period (#634, board `CreditTermsGrace`): a reminder of the interest-free day; both fields or none.
        DaySheetField(
            value = state.creditStatementDay,
            onValueChange = actions.onCreditStatementDayChange,
            label = stringResource(R.string.account_credit_statement_day),
            hint = stringResource(R.string.account_credit_optional),
            clearable = true,
        )
        NumberSheetField(
            value = state.creditGraceDays,
            onValueChange = actions.onCreditGraceDaysChange,
            label = stringResource(R.string.account_credit_grace_days),
            maxWhole = GRACE_DAYS_WHOLE,
            hint = stringResource(R.string.account_credit_grace_hint),
        )
    }
}

/** «Лимит 500 000 · до 15-го · от 5 %» for the tile; «Не заданы» until the limit is typed. */
@Composable
internal fun creditSummary(state: AccountEditUiState): String {
    if (state.creditLimit.isBlank()) return stringResource(R.string.account_credit_empty)
    val min = stringResource(
        if (state.creditMinSum) R.string.account_credit_min_sum else R.string.account_credit_min_percent,
        state.creditMin.ifBlank { "—" },
    )
    return stringResource(R.string.account_credit_summary, state.creditLimit, state.creditPayDay.ifBlank { "—" }, min)
}

/** «100,00 %» at most — what `parsePercentBasisPoints` reads. */
internal const val PERCENT_WHOLE = 3
internal const val PERCENT_FRACTION = 2
private const val GRACE_DAYS_WHOLE = 3
