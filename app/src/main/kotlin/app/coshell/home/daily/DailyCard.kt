package app.coshell.home.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.charts.CoShellMeter
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellDivider
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.core.designsystem.theme.AmountTitleStyle
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.core.domain.budgets.DailyBase
import app.coshell.ui.sheet.ModalSheet

/**
 * «Сегодня можно» (#769, board `DailyHome`): the allowance for today, days and money left in the month, the meter of what is spent today.
 * A spent plan shows «0» without red (`DailyOver`). A tap opens «Как посчитано» (`DailyWhy`).
 */
@Composable
internal fun DailyCard(daily: DailyUi, modifier: Modifier = Modifier) {
    var open by rememberSaveable { mutableStateOf(false) }
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    CoShellCard(modifier.fillMaxWidth(), onClick = { open = true }) {
        Column(Modifier.padding(Spacing.l), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(stringResource(R.string.daily_title), style = MaterialTheme.typography.bodyMedium, color = muted)
            AmountText(stringResource(R.string.daily_amount, daily.perDay), AmountTitleStyle, color = if (daily.over) muted else MaterialTheme.colorScheme.onSurface)
            Text(
                if (daily.over) stringResource(R.string.daily_over, daily.left) else pluralStringResource(R.plurals.daily_left, daily.days, daily.days, daily.left),
                style = MaterialTheme.typography.bodySmall,
                color = muted,
            )
            if (!daily.over) {
                CoShellMeter(daily.todayFraction, MaterialTheme.colorScheme.primary, Modifier.fillMaxWidth().padding(top = Spacing.s))
                Text(stringResource(R.string.daily_today, daily.spentToday), style = MaterialTheme.typography.bodySmall, color = muted)
            }
        }
    }
    if (open) DailyWhySheet(daily, onClosed = { open = false })
}

/** «Как посчитано»: the lines of the sum; lines with nothing in them are left out. */
@Composable
private fun DailyWhySheet(daily: DailyUi, onClosed: () -> Unit) {
    ModalSheet(title = stringResource(R.string.daily_open), subtitle = stringResource(R.string.daily_why_period, daily.month), onClosed = onClosed) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
            Line(stringResource(if (daily.base == DailyBase.LIMITS) R.string.daily_why_limits else R.string.daily_why_income), daily.baseAmount)
            Line(stringResource(R.string.daily_why_spent), stringResource(R.string.daily_minus, daily.spent))
            if (daily.upcoming.isNotEmpty()) Line(stringResource(R.string.daily_why_upcoming), stringResource(R.string.daily_minus, daily.upcoming))
            if (daily.goals.isNotEmpty()) Line(stringResource(R.string.daily_why_goals), stringResource(R.string.daily_minus, daily.goals))
            CoShellDivider()
            Line(stringResource(if (daily.over) R.string.daily_why_over else R.string.daily_why_left), daily.left, strong = true)
            Line(stringResource(R.string.daily_why_days), daily.days.toString())
            Line(stringResource(R.string.daily_why_per_day), stringResource(R.string.daily_amount, daily.perDay), strong = true)
            val muted = MaterialTheme.colorScheme.onSurfaceVariant
            if (daily.base == DailyBase.INCOME) Text(stringResource(R.string.daily_why_income_note), style = MaterialTheme.typography.bodySmall, color = muted)
            if (daily.missing.isNotEmpty()) Text(stringResource(R.string.daily_why_missing, daily.missing), style = MaterialTheme.typography.bodySmall, color = muted)
            Text(stringResource(R.string.daily_why_note), style = MaterialTheme.typography.bodySmall, color = muted)
        }
    }
}

@Composable
private fun Line(label: String, value: String, strong: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = if (strong) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        AmountText(value, AmountTextStyle)
    }
}
