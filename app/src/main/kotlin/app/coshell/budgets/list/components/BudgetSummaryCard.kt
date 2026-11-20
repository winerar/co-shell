package app.coshell.budgets.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import app.coshell.R
import app.coshell.budgets.list.BudgetsUiState
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.charts.BulletTone
import app.coshell.core.designsystem.charts.CoShellBullet
import app.coshell.core.designsystem.charts.bulletColor
import app.coshell.core.designsystem.components.CoShellFlipCard
import app.coshell.core.designsystem.components.FlipCueClearance
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.LocalHeroPalette
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.ui.format.breakableBeforeCurrency

/**
 * The month at a glance (`BudA`): «Осталось 540 USD» (or «Перерасход»), «потрачено 640 из 1 180 USD · ещё 19 дней»,
 * a bullet with today's mark and the forecast for the end of the month.
 */
@Composable
internal fun BudgetSummaryCard(
    summary: BudgetsUiState.Summary,
    closest: List<BudgetsUiState.Envelope>,
    modifier: Modifier = Modifier,
) {
    var flipped by rememberSaveable { mutableStateOf(false) }
    // A tap turns the card over (#519): the back says where to look — the daily allowance and the limits closest to the end.
    CoShellFlipCard(
        flipped = flipped,
        onFlip = { flipped = !flipped },
        flipLabel = stringResource(R.string.flip_card_label),
        modifier = modifier.fillMaxWidth(),
        front = { SummaryContent(summary) },
        back = { SummaryBack(summary, closest) },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryContent(summary: BudgetsUiState.Summary) {
    // A flow: the title keeps its words whole, «ещё 19 дней» drops under it when the line is too narrow.
    // The end padding keeps «ещё 19 дней» clear of the flip «↻» in the corner.
    FlowRow(Modifier.fillMaxWidth().padding(end = FlipCueClearance), itemVerticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Text(
            stringResource(if (summary.over) R.string.budgets_over else R.string.budgets_left),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        summary.daysLeft?.let { days ->
            Text(
                pluralStringResource(R.plurals.budgets_days_left, days, days),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )
        }
    }
    AmountText(
        summary.left,
        AmountTitleStyle,
        color = if (summary.over) bulletColor(BulletTone.DANGER) else LocalHeroPalette.current.amount,
        modifier = Modifier.padding(top = Spacing.xs),
        minFontSize = 16.sp, // shrinks, never loses the currency code (#432)
    )
    Text(
        stringResource(R.string.budgets_spent_of, summary.spent, summary.planned).breakableBeforeCurrency(),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    CoShellBullet(summary.fraction, summary.tone, Modifier.padding(top = Spacing.s), mark = summary.pace)
    summary.forecast?.let { forecast ->
        val over = forecast.overBy != null
        Text(
            if (over) {
                stringResource(R.string.budgets_forecast_over, forecast.amount, forecast.overBy.orEmpty()).breakableBeforeCurrency()
            } else {
                stringResource(R.string.budgets_forecast_ok, forecast.amount).breakableBeforeCurrency()
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (over) bulletColor(BulletTone.WARNING) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.s),
        )
    }
    if (summary.missing.isNotEmpty()) {
        Text(
            stringResource(R.string.budgets_missing, summary.missing.joinToString(", ")),
            style = MaterialTheme.typography.bodySmall,
            color = bulletColor(BulletTone.WARNING),
            modifier = Modifier.padding(top = Spacing.xs),
        )
    }
}
