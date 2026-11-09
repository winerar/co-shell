package app.coshell.accounts.list.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.accounts.labelRes
import app.coshell.accounts.list.AccountsUiState
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.category.AccountBadge
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.theme.RateTextStyle

/**
 * Compact row of the accounts list (owner 2026-11-09: «счетов будет больше десятка»): badge in the account's
 * colour, name, type (personal, «не в общем балансе»), balance and its value in the main currency.
 */
@Composable
internal fun AccountRow(row: AccountsUiState.Row, onClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalAmountPalette.current
    val type = stringResource(row.type.labelRes)
    val personal = row.personalOf?.let {
        if (it.isEmpty()) stringResource(R.string.account_personal_mine) else stringResource(R.string.account_personal_of, it)
    }
    val base = listOfNotNull(type, personal).joinToString(" · ")
    CoShellListRow(
        title = { Text(row.name, maxLines = 1) },
        modifier = modifier,
        supporting = {
            Text(
                if (row.excludedFromTotal) stringResource(R.string.account_excluded_suffix, base) else base,
                color = if (row.excludedFromTotal) LocalStatusPalette.current.warning else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        },
        leading = { AccountBadge(row.name, row.color) },
        trailing = {
            Column(horizontalAlignment = Alignment.End) {
                AmountText(
                    text = row.balance,
                    style = AmountTextStyle,
                    color = if (row.isNegative) palette.expense else MaterialTheme.colorScheme.onSurface,
                )
                row.converted?.let { Text(it, style = RateTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        },
        onClick = { onClick(row.id) },
    )
}
