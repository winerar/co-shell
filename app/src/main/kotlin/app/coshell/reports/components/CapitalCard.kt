package app.coshell.reports.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import app.coshell.R
import app.coshell.core.designsystem.charts.CoShellLine
import app.coshell.core.designsystem.charts.LinePoint
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.ReportsUiState.Capital
import app.coshell.reports.ReportsUiState.WhyRow
import app.coshell.ui.format.formatMonth
import app.coshell.ui.format.formatMonthShort
import app.coshell.ui.format.formatMonthYear
import kotlinx.datetime.YearMonth

/**
 * «Капитал» (#189, board `Rp189NwB`): the money on all accounts now and its change over the year, a line of month ends,
 * and «Почему изменился» — what was saved and what rates (and the rest) added in the last months. A tap on the line —
 * the accounts at that month end ([onPoint], #373).
 */
@Composable
internal fun CapitalCard(capital: Capital, currency: String, modifier: Modifier = Modifier, onPoint: ((YearMonth) -> Unit)? = null) {
    CoShellCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.l)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.reports_capital_title),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
                Text(stringResource(R.string.reports_capital_caption), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Head(capital, currency)
            val description = capital.points.joinToString(", ") { "${formatMonthYear(it.month)} ${it.value}" }
            CoShellLine(
                points = capital.points.map { LinePoint(formatMonthShort(it.month).take(1), it.minor) },
                description = stringResource(R.string.reports_capital_description, description),
                modifier = Modifier.padding(top = Spacing.m),
                onSelect = onPoint?.let { open -> { i: Int -> capital.points.getOrNull(i)?.let { open(it.month) } } },
            )
            if (capital.why.isNotEmpty()) Why(capital.why)
        }
    }
}

@Composable
private fun Head(capital: Capital, currency: String) {
    val change = capital.change + (capital.percent?.let { " · " + (if (it > 0) "+" else "") + "$it%" } ?: "")
    val line = if (capital.months >= YEAR) stringResource(R.string.reports_capital_year, change)
    else stringResource(R.string.reports_capital_months, change, capital.months)
    Row(Modifier.padding(top = Spacing.xs), verticalAlignment = Alignment.Bottom) {
        Text(
            capital.value,
            style = RateTextStyle.copy(fontSize = MaterialTheme.typography.headlineSmall.fontSize, fontWeight = FontWeight.Bold),
            maxLines = 1,
        )
        Text(
            " $currency",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = Spacing.s, bottom = Spacing.xxs),
        )
        Text(
            line,
            style = MaterialTheme.typography.bodySmall,
            color = signColor(capital.falling),
            maxLines = 1,
            modifier = Modifier.padding(bottom = Spacing.xxs),
        )
    }
}

@Composable
private fun Why(rows: List<WhyRow>) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    HorizontalDivider(Modifier.padding(top = Spacing.m), color = MaterialTheme.colorScheme.outlineVariant)
    Row(Modifier.padding(top = Spacing.s), verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.reports_capital_why), style = MaterialTheme.typography.labelSmall, color = muted, modifier = Modifier.weight(MONTH_WEIGHT))
        Cell(stringResource(R.string.reports_capital_saved), muted, label = true)
        Cell(stringResource(R.string.reports_capital_rates), muted, label = true)
        Cell(stringResource(R.string.reports_capital_total), muted, label = true)
    }
    rows.forEachIndexed { i, row ->
        if (i > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Row(Modifier.padding(vertical = Spacing.s).semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.CenterVertically) {
            val month = formatMonth(row.month.firstDay).replaceFirstChar { it.uppercase() } +
                if (row.running) " · " + stringResource(R.string.reports_income_so_far) else ""
            Text(month, style = MaterialTheme.typography.bodyMedium, maxLines = 1, modifier = Modifier.weight(MONTH_WEIGHT))
            Cell(row.saved, signColor(row.savedNegative))
            Cell(row.rates, muted)
            Cell(row.total, signColor(row.totalNegative), bold = true)
        }
    }
    Text(
        stringResource(R.string.reports_capital_rates_note),
        style = MaterialTheme.typography.bodySmall,
        color = muted,
        modifier = Modifier.padding(top = Spacing.xs),
    )
}

@Composable
private fun RowScope.Cell(text: String, color: Color, label: Boolean = false, bold: Boolean = false) {
    Text(
        text,
        style = if (label) MaterialTheme.typography.labelSmall
        else RateTextStyle.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize, fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal),
        color = color,
        textAlign = TextAlign.End,
        maxLines = 1,
        modifier = Modifier.weight(1f),
    )
}

@Composable
private fun signColor(negative: Boolean): Color = if (negative) LocalAmountPalette.current.expense else LocalStatusPalette.current.success

private const val YEAR = 12
/** The month column against each number column. */
private const val MONTH_WEIGHT = 1.6f
