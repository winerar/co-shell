package app.coshell.transactions.list.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.category.IconBadge
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.Row as RowState
import app.coshell.core.designsystem.R as DesignR

/** Badge, category (or "Перевод") and account on the left, coloured amount on the right. */
@Composable
internal fun TransactionRow(row: RowState, onClick: (String) -> Unit) {
    CoShellListRow(
        title = row.titleText(),
        subtitle = listOfNotNull(row.subtitle, row.author, row.note.ifBlank { null }).joinToString(" · "),
        subtitleMaxLines = 1,
        leading = { RowBadge(row) },
        trailing = {
            Column(horizontalAlignment = Alignment.End) {
                AmountText(row.amount, style = AmountTextStyle, color = row.amountColor())
                row.secondaryAmount?.let {
                    AmountText(it, style = AmountTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        onClick = { onClick(row.id) },
        containerColor = MaterialTheme.colorScheme.surface, // covers the swipe-to-delete strip
    )
}

/** Category badge; transfers get a neutral arrows icon. */
@Composable
private fun RowBadge(row: RowState) {
    if (row.kind == Kind.TRANSFER) IconBadge(DesignR.drawable.ic_sync_alt, color = null)
    else CategoryBadge(row.icon, row.color)
}

@Composable
private fun RowState.titleText(): String = title ?: stringResource(
    if (kind == Kind.TRANSFER) R.string.transaction_type_transfer else R.string.transactions_no_category,
)

@Composable
private fun RowState.amountColor() = when (kind) {
    Kind.EXPENSE -> LocalAmountPalette.current.expense
    Kind.INCOME -> LocalAmountPalette.current.income
    Kind.TRANSFER -> LocalAmountPalette.current.transfer
}
