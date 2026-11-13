package app.coshell.accounts.edit.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import app.coshell.accounts.edit.AccountEditActions
import app.coshell.accounts.edit.AccountEditUiState
import app.coshell.accounts.edit.AccountEditUiState.BalanceError
import app.coshell.accounts.edit.AccountEditUiState.CurrencyError
import app.coshell.core.designsystem.components.CoShellDetailTile
import app.coshell.core.designsystem.components.CoShellFormSection
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.sheet.FormSheet
import app.coshell.core.designsystem.R as DesignR

/**
 * «Деньги» (#290, board `Fb290FAAcc`): «Валюта» and «Нач. баланс» as two tiles, each opening its own small sheet.
 * A locked currency says so on the tile; an error is said under the tiles, so a bad value is seen without opening.
 */
@Composable
internal fun MoneySection(state: AccountEditUiState, actions: AccountEditActions) {
    var currencyOpen by rememberSaveable { mutableStateOf(false) }
    var balanceOpen by rememberSaveable { mutableStateOf(false) }
    CoShellFormSection(stringResource(R.string.account_section_money)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s - Spacing.xxs)) {
            CoShellDetailTile(
                icon = painterResource(DesignR.drawable.ic_payments),
                label = stringResource(R.string.account_currency),
                value = when {
                    state.currency.isBlank() -> stringResource(R.string.account_currency_none)
                    state.currencyLocked -> stringResource(R.string.account_currency_locked_short, state.currency)
                    else -> state.currency
                },
                onClick = { currencyOpen = true },
                modifier = Modifier.weight(1f),
                empty = state.currency.isBlank() || state.currencyLocked,
            )
            CoShellDetailTile(
                icon = painterResource(DesignR.drawable.ic_account_balance_wallet),
                label = stringResource(R.string.account_initial_balance_short),
                value = state.initialBalance.ifBlank { "0" },
                onClick = { balanceOpen = true },
                modifier = Modifier.weight(1f),
                empty = state.initialBalance.isBlank(),
            )
        }
        moneyError(state)?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
    }
    if (currencyOpen) {
        FormSheet(stringResource(R.string.account_currency), onClosed = { currencyOpen = false }) {
            CurrencyFields(state, actions.onCurrencyChange)
        }
    }
    if (balanceOpen) {
        FormSheet(stringResource(R.string.account_initial_balance), onClosed = { balanceOpen = false }) {
            InitialBalanceField(state.initialBalance, state.balanceError, actions.onInitialBalanceChange)
        }
    }
}

@Composable
private fun moneyError(state: AccountEditUiState): String? = when {
    state.currencyError == CurrencyError.INVALID -> stringResource(R.string.account_currency_error)
    state.currencyError == CurrencyError.LOCKED -> stringResource(R.string.account_currency_locked)
    state.balanceError == BalanceError.INVALID -> stringResource(R.string.account_initial_balance_error)
    state.balanceError == BalanceError.TOO_MANY_FRACTION_DIGITS -> stringResource(R.string.account_initial_balance_fraction_error)
    else -> null
}
