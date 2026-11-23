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
import app.coshell.core.designsystem.components.ButtonTone
import app.coshell.core.designsystem.components.CoShellButton
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellDivider
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.components.CoShellOutlinedButton
import app.coshell.core.designsystem.components.CoShellSectionLabel
import app.coshell.core.designsystem.components.CoShellSectionLink
import app.coshell.core.designsystem.category.AccountBadge
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.recurring.SOON_DAYS
import app.coshell.recurring.PaymentAmount
import app.coshell.recurring.PaymentWhen
import app.coshell.templates.components.TemplateBadge
import app.coshell.ui.format.formatWeekdayDayMonthShort
import kotlinx.datetime.LocalDate

/**
 * «Скоро · 7 дней» on «Обзоре» (#143, board `Rec143HomeA`): missed and today's payments with a label, the next
 * ones with their day, the amount next to it (on the right it left no room for the name); «Провести» opens the
 * entry sheet on the payment's day, «само» — the phone posts it itself. «Все N ›» — «Регулярные платежи» (#403).
 * Two or more missed with an amount — «Провести оба» / «Пропустить» under the row (#406, board `Rec143Edge`).
 * Not shown when nothing is due.
 */
@Composable
internal fun UpcomingCard(
    rows: List<UpcomingRow>,
    total: Int,
    onPost: (templateId: String, date: LocalDate) -> Unit,
    onAll: () -> Unit,
    modifier: Modifier = Modifier,
    onPostAll: (UpcomingRow) -> Unit = {},
    onSkipAll: (templateId: String) -> Unit = {},
    credit: List<CreditPaymentRow> = emptyList(),
    onPostCredit: (accountId: String) -> Unit = {},
) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth().padding(start = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
            CoShellSectionLabel(stringResource(R.string.recurring_soon_title, SOON_DAYS), Modifier.weight(1f))
            CoShellSectionLink(stringResource(R.string.templates_all, total), onAll)
        }
        CoShellCard(Modifier.fillMaxWidth().padding(top = Spacing.s)) {
            rows.forEachIndexed { index, row ->
                key(row.item.id) {
                    if (index > 0) CoShellDivider()
                    UpcomingRowItem(row, onPost)
                    if (row.bulk) MissedActions(row, { onPostAll(row) }, { onSkipAll(row.item.id) })
                }
            }
            credit.forEachIndexed { index, row ->
                key("credit-" + row.accountId) {
                    if (rows.isNotEmpty() || index > 0) CoShellDivider()
                    CreditPaymentItem(row, onPostCredit)
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
                PaymentWhen(row.state, row.missed, formatWeekdayDayMonthShort(row.date))
                PaymentAmount(row.item)
            }
        },
        leading = { TemplateBadge(row.item) },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (row.bulk) return@Row // «Провести оба» below
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

/** «Провести оба» (all missed by the template's amount) and «Пропустить» (none of them posted). */
@Composable
private fun MissedActions(row: UpcomingRow, onPostAll: () -> Unit, onSkipAll: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(start = Spacing.l, end = Spacing.l, bottom = Spacing.m),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
    ) {
        val label = if (row.missed == 2) stringResource(R.string.recurring_post_both) else stringResource(R.string.recurring_post_all, row.missed)
        CoShellButton(onClick = onPostAll, modifier = Modifier.weight(1f)) { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        CoShellOutlinedButton(onClick = onSkipAll, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.recurring_skip_missed), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** The payment of a credit card (#686): the card's badge, «Платёж по кредитке», its day and the minimum; «Провести» opens the transfer. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreditPaymentItem(row: CreditPaymentRow, onPost: (String) -> Unit) {
    CoShellListRow(
        title = { Text(stringResource(R.string.credit_payment_title), maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supporting = {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.credit_payment_when, row.name, formatWeekdayDayMonthShort(row.due)))
                Text(stringResource(R.string.credit_payment_minimum, row.minimum))
            }
        },
        leading = { AccountBadge(row.name, row.color) },
        trailing = {
            val label = stringResource(R.string.credit_payment_post_a11y, row.name)
            CoShellButton(
                onClick = { onPost(row.accountId) },
                tone = ButtonTone.Tonal,
                contentPadding = PaddingValues(horizontal = Spacing.l),
                modifier = Modifier.semantics { contentDescription = label },
            ) { Text(stringResource(R.string.recurring_post)) }
        },
    )
}
