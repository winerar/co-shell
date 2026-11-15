package app.coshell.home.upcoming

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.components.ButtonTone
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellDivider
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.components.CoShellStatusChip
import app.coshell.core.designsystem.components.StatusTone
import app.coshell.core.designsystem.theme.AmountCellStyle
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.recurring.PaymentState
import app.coshell.core.domain.recurring.SOON_DAYS
import app.coshell.core.model.TransactionType
import app.coshell.templates.components.TemplateBadge
import app.coshell.ui.format.formatWeekdayDayMonthShort
import kotlinx.datetime.LocalDate

/**
 * «Скоро · 7 дней» on «Обзоре» (#143, board `Rec143HomeA`): missed and today's payments with a label, the next
 * ones with their day, the amount next to it (on the right it left no room for the name); «Провести» opens the
 * entry sheet on the payment's day, «само» — the phone posts it itself.
 * Not shown when nothing is due.
 */
@Composable
internal fun UpcomingCard(
    rows: List<UpcomingRow>,
    onPost: (templateId: String, date: LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        CoShellSectionLabel(stringResource(R.string.recurring_soon_title, SOON_DAYS), Modifier.padding(start = Spacing.xs))
        CoShellCard(Modifier.fillMaxWidth().padding(top = Spacing.s)) {
            rows.forEachIndexed { index, row ->
                key(row.item.id) {
                    if (index > 0) CoShellDivider()
                    UpcomingRowItem(row, onPost)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UpcomingRowItem(row: UpcomingRow, onPost: (String, LocalDate) -> Unit) {
    CoShellListRow(
        title = { Text(row.item.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supporting = {
            // A long label and amount on a narrow phone: the amount moves to the next line instead of losing its code.
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                When(row)
                Amount(row)
            }
        },
        leading = { TemplateBadge(row.item) },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (row.auto) {
                    Text(stringResource(R.string.recurring_auto_short), style = MaterialTheme.typography.bodySmall)
                } else {
                    val label = stringResource(R.string.recurring_post_a11y, row.item.name)
                    CoShellButton(
                        onClick = { onPost(row.item.id, row.date) },
                        tone = ButtonTone.Tonal,
                        contentPadding = PaddingValues(horizontal = Spacing.l),
                        modifier = Modifier.semantics { contentDescription = label },
                    ) { Text(stringResource(R.string.recurring_post)) }
                }
            }
        },
    )
}

/** «пропущен» / «пропущено 2» / «сегодня» as a label; a later day as grey text. */
@Composable
private fun When(row: UpcomingRow) {
    when (row.state) {
        PaymentState.MISSED -> CoShellStatusChip(
            if (row.missed > 1) stringResource(R.string.recurring_state_missed_many, row.missed) else stringResource(R.string.recurring_state_missed),
            StatusTone.Work,
        )
        PaymentState.TODAY -> CoShellStatusChip(stringResource(R.string.recurring_state_today), StatusTone.New)
        PaymentState.SOON -> Text(formatWeekdayDayMonthShort(row.date), style = MaterialTheme.typography.bodySmall)
    }
}

/** «−650 USD», «+1 200 USD» in the income colour, or «при вводе» when the amount is asked every time. */
@Composable
private fun Amount(row: UpcomingRow) {
    val item = row.item
    if (item.amount.isEmpty()) {
        Text(stringResource(R.string.template_amount_on_entry_short), style = MaterialTheme.typography.bodySmall)
        return
    }
    when (item.type) {
        TransactionType.INCOME -> AmountText("+" + item.amount, style = AmountCellStyle, color = LocalAmountPalette.current.income)
        TransactionType.EXPENSE -> AmountText("−" + item.amount, style = AmountCellStyle)
        TransactionType.TRANSFER -> AmountText(item.amount, style = AmountCellStyle)
    }
}
