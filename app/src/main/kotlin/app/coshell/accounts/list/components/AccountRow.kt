package app.coshell.accounts.list.components

import app.coshell.core.designsystem.tokens.Spacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.LocalAmountPalette

/** Card with account name, type (and "не в общей сумме") and balance (red when negative). */
@Composable
internal fun AccountRow(row: AccountsUiState.Row, onClick: (String) -> Unit, modifier: Modifier = Modifier) {
    val palette = LocalAmountPalette.current
    CoShellCard(modifier = modifier.fillMaxWidth(), onClick = { onClick(row.id) }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.l),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(row.name, style = MaterialTheme.typography.bodyLarge)
                val type = stringResource(row.type.labelRes)
                val personal = row.personalOf?.let {
                    if (it.isEmpty()) stringResource(R.string.account_personal_mine) else stringResource(R.string.account_personal_of, it)
                }
                val base = listOfNotNull(type, personal).joinToString(" · ")
                Text(
                    if (row.excludedFromTotal) stringResource(R.string.account_excluded_suffix, base) else base,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AmountText(
                text = row.balance,
                style = AmountTextStyle,
                color = if (row.isNegative) palette.expense else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
