package app.coshell.accounts.list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.accounts.labelRes
import app.coshell.accounts.list.AccountsUiState
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.LocalAmountPalette

/** Card with account name, type (and "не в общей сумме") and balance (red when negative). */
@Composable
internal fun AccountRow(row: AccountsUiState.Row, onClick: (String) -> Unit) {
    val palette = LocalAmountPalette.current
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick(row.id) }) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(row.name, style = MaterialTheme.typography.bodyLarge)
                val type = stringResource(row.type.labelRes)
                Text(
                    if (row.excludedFromTotal) stringResource(R.string.account_excluded_suffix, type) else type,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = row.balance,
                style = AmountTextStyle,
                color = if (row.isNegative) palette.expense else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
