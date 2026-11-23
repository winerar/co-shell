package app.coshell.reports.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import app.coshell.R
import app.coshell.core.designsystem.amount.shownAmount
import app.coshell.core.designsystem.charts.ChartPair
import app.coshell.core.designsystem.charts.CoShellPairedColumns
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellDivider
import app.coshell.core.designsystem.components.CoShellDot
import app.coshell.core.designsystem.theme.AmountStatStyle
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.ReportsUiState.Income
import app.coshell.ui.format.formatMonth
import app.coshell.ui.format.formatMonthShort
import app.coshell.ui.format.formatMonthYear
import kotlinx.datetime.YearMonth

/**
 * «Доходы и расходы» (#189, board `Rp189IeA`): income next to spending for each month, the shown month on a plate;
 * under the chart — that month's «пришло · ушло · остаток» and the savings rate against the average of finished
 * months. A tap on a month — that month.
 */
@Composable
internal fun IncomeCard(income: Income, onMonth: (YearMonth) -> Unit, modifier: Modifier = Modifier) {
    val amounts = LocalAmountPalette.current
    CoShellCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.l)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.reports_income_title),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f).semantics { heading() },
                )
                Text(stringResource(R.string.reports_income_caption), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val pairs = income.columns.map {
                ChartPair(
                    label = formatMonthShort(it.month),
                    first = it.earnedFraction,
                    second = it.spentFraction,
                    description = stringResource(R.string.reports_income_description, formatMonthYear(it.month), shownAmount(it.earned), shownAmount(it.spent)),
                )
            }
            CoShellPairedColumns(
                pairs,
                Modifier.padding(top = Spacing.m),
                selected = income.selected,
                onSelect = { onMonth(income.columns[it].month) },
            )
            Row(Modifier.padding(top = Spacing.s), horizontalArrangement = Arrangement.spacedBy(Spacing.l)) {
                Legend(amounts.income, stringResource(R.string.reports_income_in))
                Legend(amounts.expense, stringResource(R.string.reports_income_out))
            }
            CoShellDivider(Modifier.padding(top = Spacing.m))
            Stats(income)
            RateLine(income)
        }
    }
}

@Composable
private fun Stats(income: Income) {
    val amounts = LocalAmountPalette.current
    val month = formatMonth(income.columns[income.selected].month.firstDay).replaceFirstChar { it.uppercase() }
    val leftColor = if (income.overspent) amounts.expense else LocalStatusPalette.current.success
    val left = stringResource(R.string.reports_income_left) + if (income.running) " · " + stringResource(R.string.reports_income_so_far) else ""
    Row(Modifier.fillMaxWidth().padding(top = Spacing.m), horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Stat(stringResource(R.string.reports_income_month_in, month), income.earned, amounts.income, Modifier.weight(1f))
        Stat(stringResource(R.string.reports_income_out), income.spent, amounts.expense, Modifier.weight(1f))
        Stat(left, income.saved, leftColor, Modifier.weight(1f))
    }
}

@Composable
private fun Stat(label: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier.semantics(mergeDescendants = true) {}) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        Text(shownAmount(value), style = AmountStatStyle, color = color, maxLines = 1)
    }
}

@Composable
private fun RateLine(income: Income) {
    val rate = income.rate
    val text = when {
        rate == null -> stringResource(R.string.review_no_income)
        rate < 0 -> stringResource(R.string.review_over_income).replaceFirstChar { it.uppercase() }
        else -> stringResource(R.string.reports_income_rate, rate)
    }
    val average = income.averageRate?.let { stringResource(R.string.reports_income_average, income.averageMonths, it) }
    Text(
        listOfNotNull(text, average).joinToString(" · "),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = Spacing.s),
    )
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        CoShellDot(color, size = Spacing.s)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

