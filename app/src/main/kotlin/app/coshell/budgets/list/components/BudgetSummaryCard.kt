package app.coshell.budgets.list.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.budgets.list.BudgetsUiState
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.components.BulletTone
import app.coshell.core.designsystem.components.CoShellBullet
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.bulletColor
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.theme.LocalHeroPalette
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.core.designsystem.tokens.Spacing

/**
 * The month at a glance (`BudA`): «Осталось 540 USD» (or «Перерасход»), «потрачено 640 из 1 180 USD · ещё 19 дней»,
 * a bullet with today's mark and the forecast for the end of the month.
 */
@Composable
internal fun BudgetSummaryCard(summary: BudgetsUiState.Summary, modifier: Modifier = Modifier) {
    CoShellCard(modifier.fillMaxWidth(), accent = true) {
        Column(Modifier.padding(Spacing.l)) { SummaryContent(summary) }
    }
}

@Composable
private fun SummaryContent(summary: BudgetsUiState.Summary) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            stringResource(if (summary.over) R.string.budgets_over else R.string.budgets_left),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        summary.daysLeft?.let { days ->
            Text(
                pluralStringResource(R.plurals.budgets_days_left, days, days),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    AmountText(
        summary.left,
        AmountTitleStyle,
        color = if (summary.over) bulletColor(BulletTone.DANGER) else LocalHeroPalette.current.amount,
        modifier = Modifier.padding(top = Spacing.xs),
    )
    Text(
        stringResource(R.string.budgets_spent_of, summary.spent, summary.planned),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    CoShellBullet(summary.fraction, summary.tone, Modifier.padding(top = Spacing.s), mark = summary.pace, height = Sizes.bulletLarge)
    summary.forecast?.let { forecast ->
        val over = forecast.overBy != null
        Text(
            if (over) {
                stringResource(R.string.budgets_forecast_over, forecast.amount, forecast.overBy.orEmpty())
            } else {
                stringResource(R.string.budgets_forecast_ok, forecast.amount)
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
