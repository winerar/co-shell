package app.coshell.accounts.list.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.accounts.AccountLine
import app.coshell.accounts.labelRes
import app.coshell.accounts.list.AccountsUiState

/**
 * Row of the accounts list (owner 2026-11-09: «счетов будет больше десятка») — the shared [AccountLine] with the type,
 * whose it is and «не в общем балансе».
 */
@Composable
internal fun AccountRow(row: AccountsUiState.Row, onClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val type = stringResource(if (row.prepaid) R.string.account_type_prepaid else row.type.labelRes)
    val personal = row.personalOf?.let {
        if (it.isEmpty()) stringResource(R.string.account_personal_mine) else stringResource(R.string.account_personal_of, it)
    }
    val base = listOfNotNull(type, personal).joinToString(" · ")
    AccountLine(
        name = row.name,
        color = row.color,
        supporting = if (row.excludedFromTotal) stringResource(R.string.account_excluded_suffix, base) else base,
        balance = row.balance,
        onClick = { onClick(row.id) },
        modifier = modifier,
        isNegative = row.isNegative,
        converted = row.converted,
        warn = row.excludedFromTotal,
    )
}
