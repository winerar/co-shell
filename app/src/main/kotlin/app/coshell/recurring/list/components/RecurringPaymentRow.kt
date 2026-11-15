package app.coshell.recurring.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.model.TransactionType
import app.coshell.recurring.PaymentAmount
import app.coshell.recurring.PaymentWhen
import app.coshell.recurring.list.RecurringRow
import app.coshell.recurring.recurrenceText
import app.coshell.templates.components.TemplateBadge
import app.coshell.ui.format.formatDayMonthShort

/**
 * A row of «Регулярные платежи» (board `Rec143List`): the template's badge, name, «каждый месяц, 15-го · Galicia · само»;
 * on the right the amount over the next payment («пропущен», «сегодня» or «18 нояб.»). A tap opens the template.
 */
@Composable
internal fun RecurringPaymentRow(row: RecurringRow, onClick: () -> Unit) {
    CoShellListRow(
        title = { Text(row.item.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supporting = { Text(subtitle(row), maxLines = 2, overflow = TextOverflow.Ellipsis) },
        leading = { TemplateBadge(row.item) },
        trailing = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                PaymentAmount(row.item)
                PaymentWhen(row.state, row.missed, formatDayMonthShort(row.date))
            }
        },
        onClick = onClick,
    )
}

/** «каждый месяц, 15-го · Galicia · само»; a transfer — «Galicia → Копилка». */
@Composable
private fun subtitle(row: RecurringRow): String {
    val item = row.item
    val gone = stringResource(R.string.template_account_gone)
    val account = if (item.type == TransactionType.TRANSFER) {
        stringResource(R.string.template_row_transfer, item.accountName.ifEmpty { gone }, item.toAccountName.ifEmpty { gone })
    } else {
        item.accountName.ifEmpty { gone }
    }
    val rule = recurrenceText(row.recurrence).replaceFirstChar { it.lowercase() }
    val parts = listOfNotNull(rule, account, stringResource(R.string.recurring_auto_short).takeIf { row.auto })
    return parts.joinToString(SEPARATOR)
}

private const val SEPARATOR = " · "
