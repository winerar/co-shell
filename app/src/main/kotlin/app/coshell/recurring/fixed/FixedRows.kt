package app.coshell.recurring.fixed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.charts.CoShellMeter
import app.coshell.core.designsystem.theme.AmountCellStyle
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.progressColors
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.format.sentenceAmounts
import app.coshell.recurring.recurrenceText
import app.coshell.templates.components.TemplateBadge

/** A payment of «Постоянные траты» (board `Fixed771Year`): badge, name, «Каждый месяц, 1-го»; on the right «−7 800 USD» and «в год · 650 в месяц». */
@Composable
internal fun FixedRow(row: FixedRowUi, onClick: () -> Unit) {
    CoShellListRow(
        title = { Text(row.item.name, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        supporting = {
            Text(recurrenceText(row.recurrence), maxLines = 2, overflow = TextOverflow.Ellipsis)
        },
        leading = { TemplateBadge(row.item) },
        trailing = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                AmountText("−" + row.yearly, style = AmountCellStyle)
                Text(stringResource(R.string.fixed_row_sub, row.monthly.sentenceAmounts()), style = MaterialTheme.typography.bodySmall)
            }
        },
        onClick = onClick,
    )
}

/**
 * The sum of «Постоянные траты»: «9 336 USD в год», «778 USD в месяц · 41 % ваших трат», the share as a meter and the
 * currencies left out. [amountColor] — the hero's own amount colour where it sits on one.
 */
@Composable
internal fun FixedTotal(fixed: FixedUi, amountColor: Color = MaterialTheme.colorScheme.onSurface) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        Row(verticalAlignment = Alignment.Bottom) {
            AmountText(fixed.yearly, style = AmountTitleStyle, color = amountColor, modifier = Modifier.weight(1f, fill = false))
            Text(" " + stringResource(R.string.fixed_per_year), style = MaterialTheme.typography.titleSmall, color = muted, modifier = Modifier.padding(bottom = Spacing.xxs))
        }
        val share = fixed.sharePercent
        Text(
            if (share == null) {
                stringResource(R.string.fixed_monthly, fixed.monthly.sentenceAmounts())
            } else {
                stringResource(R.string.fixed_monthly_share, fixed.monthly.sentenceAmounts(), share)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = muted,
        )
        if (share != null) CoShellMeter(share / PERCENT, progressColors().indicator, Modifier.padding(top = Spacing.s), grow = true)
        if (fixed.missing.isNotEmpty()) {
            Text(stringResource(R.string.recurring_ahead_missing, fixed.missing.joinToString()), style = MaterialTheme.typography.bodySmall, color = muted)
        }
    }
}

private const val PERCENT = 100f

/** «7 платежей» — the count under the title of the year view. */
@Composable
internal fun fixedCount(count: Int): String = pluralStringResource(R.plurals.fixed_count, count, count)
