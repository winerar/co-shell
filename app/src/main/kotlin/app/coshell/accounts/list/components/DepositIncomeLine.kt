package app.coshell.accounts.list.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.accounts.list.AccountsUiState
import app.coshell.ui.format.formatDayMonthShort

/** Under a deposit in «Счета» (#634, step 3; board `AccountsDeposit`): the expected income and the closing day, quietly. */
@Composable
internal fun DepositIncomeLine(deposit: AccountsUiState.Row.Deposit) {
    Text(
        stringResource(R.string.account_deposit_income, deposit.income.orEmpty(), formatDayMonthShort(deposit.until)),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}
