package app.coshell.reports.components

import app.coshell.ui.motion.SwapText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import app.coshell.R
import app.coshell.core.designsystem.category.CategoryBadge
import app.coshell.core.designsystem.charts.CoShellLine
import app.coshell.core.designsystem.charts.LinePoint
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.Inflation
import app.coshell.reports.categoryName
import app.coshell.ui.format.formatMonthShort

/**
 * «Траты в ARS и в USD» (#788, poll 2026-11-27 «А», board `Smart788Chart`): one phrase — did spending grow in each currency —
 * two lines (the first half's average = 100) and the folders that grew most in the hard currency.
 */
@Composable
internal fun InflationCard(inflation: Inflation, modifier: Modifier = Modifier) {
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val phrase = stringResource(R.string.reports_inflation_phrase, change(inflation.local, inflation.localPercent), change(inflation.hard, inflation.hardPercent))
    CoShellCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Text(
                stringResource(R.string.reports_inflation_title, inflation.local, inflation.hard),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.semantics { heading() },
            )
            SwapText(phrase, style = MaterialTheme.typography.bodyMedium)
            if (inflation.pricesNotSpending) Text(stringResource(R.string.reports_inflation_prices), style = MaterialTheme.typography.bodySmall, color = muted)
            CoShellLine(
                points = inflation.months.mapIndexed { i, m -> LinePoint(formatMonthShort(m), inflation.localIndex[i]) },
                description = phrase,
                second = inflation.hardIndex,
                legend = stringResource(R.string.reports_inflation_in, inflation.local) to stringResource(R.string.reports_inflation_in, inflation.hard),
                modifier = Modifier.padding(top = Spacing.s),
            )
            Text(stringResource(R.string.reports_inflation_note), style = MaterialTheme.typography.bodySmall, color = muted)
        }
    }
}

/** «Быстрее всего росли» (#788): up to three folders, growth in the hard currency. */
@Composable
internal fun InflationGrowthCard(inflation: Inflation, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(vertical = Spacing.s)) {
            Text(
                stringResource(R.string.reports_inflation_fastest, inflation.hard),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = Spacing.l, vertical = Spacing.s).semantics { heading() },
            )
            inflation.fastest.forEach { g ->
                CoShellListRow(
                    title = categoryName(g.name),
                    leading = { CategoryBadge(g.icon, g.color, size = Sizes.badgeSmall) },
                    trailing = { SwapText(stringResource(R.string.reports_inflation_percent, g.percent), style = AmountTextStyle) },
                    dense = true,
                )
            }
        }
    }
}

/** «в ARS выросли на 38 %», «в USD снизились на 2 %», «в USD почти не изменились». */
@Composable
private fun change(currency: String, percent: Int): String = when {
    percent >= 1 -> stringResource(R.string.reports_inflation_up, currency, percent)
    percent <= -1 -> stringResource(R.string.reports_inflation_down, currency, -percent)
    else -> stringResource(R.string.reports_inflation_same, currency)
}
