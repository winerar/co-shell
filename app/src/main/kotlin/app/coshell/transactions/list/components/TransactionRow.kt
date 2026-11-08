package app.coshell.transactions.list.components

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.coshell.R
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.Row as RowState

/** Category (or "Перевод") and account on the left, coloured amount on the right. */
@Composable
internal fun TransactionRow(row: RowState, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(row.id) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(row.titleText(), style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = if (row.note.isBlank()) row.subtitle else "${row.subtitle} · ${row.note}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(row.amount, style = AmountTextStyle, color = row.amountColor())
            row.secondaryAmount?.let {
                Text(it, style = AmountTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RowState.titleText(): String = title ?: stringResource(
    if (kind == Kind.TRANSFER) R.string.transaction_type_transfer else R.string.transactions_no_category,
)

@Composable
private fun RowState.amountColor() = when (kind) {
    Kind.EXPENSE -> LocalAmountPalette.current.expense
    Kind.INCOME -> LocalAmountPalette.current.income
    Kind.TRANSFER -> MaterialTheme.colorScheme.onSurface
}
