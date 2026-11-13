package app.coshell.transactions.list.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.category.IconBadge
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.tokens.Motion
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.transactions.list.TransactionsUiState.Kind
import app.coshell.transactions.list.TransactionsUiState.Row as RowState
import app.coshell.core.designsystem.R as DesignR

/**
 * Badge, category (or "Перевод") and account on the left, coloured amount on the right. A long press starts
 * the selection mode (19В); a [selected] row is tinted and its badge turns into a check mark.
 */
@Composable
internal fun TransactionRow(
    row: RowState,
    onClick: (String) -> Unit,
    onLongClick: (String) -> Unit = {},
    selected: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val container by animateColorAsState(
        if (selected) colors.primary.copy(alpha = SelectedTint).compositeOver(colors.surface) else colors.surface,
        Motion.effects(),
        label = "rowSelected",
    )
    val refundLabel = stringResource(R.string.transaction_refund_row)
    val income = LocalAmountPalette.current.income
    val subtitle = remember(row, refundLabel, income) {
        buildAnnotatedString {
            if (row.refund) {
                withStyle(SpanStyle(color = income)) { append(refundLabel) }
                append(" · ")
            }
            append(listOfNotNull(row.subtitle, row.author, row.note.ifBlank { null }).joinToString(" · "))
        }
    }
    val title = row.titleText()
    CoShellListRow(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supporting = { Text(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leading = {
            Crossfade(selected, animationSpec = Motion.effects(), label = "rowBadge") { checked ->
                if (checked) CheckBadge() else RowBadge(row)
            }
        },
        trailing = {
            Column(horizontalAlignment = Alignment.End) {
                AmountText(row.amount, style = AmountTextStyle, color = row.amountColor())
                row.secondaryAmount?.let {
                    AmountText(it, style = AmountTextStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        onClick = { onClick(row.id) },
        onLongClick = { onLongClick(row.id) },
        containerColor = container,
    )
}

/** Badge of a chosen row: a solid circle of the accent with a dark check mark, as in mockup 19В. */
@Composable
private fun CheckBadge() {
    Box(
        Modifier.size(Sizes.badge).background(MaterialTheme.colorScheme.primary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painterResource(DesignR.drawable.ic_check),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(Sizes.badge * 0.55f),
        )
    }
}

/** Accent tint over the screen's surface for a chosen row. */
private const val SelectedTint = 0.10f

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
