package app.coshell.budgets.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import app.coshell.R
import app.coshell.budgets.list.BudgetsUiState
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.charts.CoShellBullet
import app.coshell.core.designsystem.components.CoShellDivider
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.LocalHeroPalette
import app.coshell.core.designsystem.components.FlipCueClearance
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.model.MoneyFormat

/**
 * The back of the month card (#519, poll 2026-11-19, variant А «Куда смотреть»): what can be spent per day until the month ends
 * (a guide, not a rule) and the limits closest to the end — a name, the line under it and a bullet each.
 */
@Composable
internal fun ColumnScope.SummaryBack(summary: BudgetsUiState.Summary, closest: List<BudgetsUiState.Envelope>) {
    val hero = LocalHeroPalette.current
    summary.perDay?.let { perDay ->
        // The top line keeps clear of the flip «↻» in the corner.
        Text(
            stringResource(R.string.budgets_back_per_day_title),
            style = MaterialTheme.typography.labelLarge,
            color = hero.hint,
            modifier = Modifier.padding(end = FlipCueClearance),
        )
        // «≈» never wraps away from the amount (#431).
        AmountText("≈" + MoneyFormat.NBSP + perDay, AmountTitleStyle, color = hero.amount, minFontSize = 16.sp)
        Text(stringResource(R.string.budgets_back_per_day_hint), style = MaterialTheme.typography.bodySmall, color = hero.hint)
    }
    if (closest.isEmpty()) return
    if (summary.perDay != null) CoShellDivider(Modifier.padding(vertical = Spacing.m))
    Text(
        stringResource(R.string.budgets_back_closest),
        style = MaterialTheme.typography.bodySmall,
        color = hero.hint,
        modifier = if (summary.perDay == null) Modifier.padding(end = FlipCueClearance) else Modifier,
    )
    Column(Modifier.padding(top = Spacing.xs), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        closest.forEach { Closest(it) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Closest(envelope: BudgetsUiState.Envelope) {
    // The name keeps its words whole; the caption drops under it on a narrow line.
    FlowRow(Modifier.fillMaxWidth(), itemVerticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Text(envelope.name, style = MaterialTheme.typography.bodyMedium)
        Text(
            captionText(envelope.caption),
            style = MaterialTheme.typography.bodySmall,
            color = LocalHeroPalette.current.hint,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
    CoShellBullet(envelope.fraction, envelope.tone, Modifier.padding(top = Spacing.xs), mark = envelope.pace)
}
