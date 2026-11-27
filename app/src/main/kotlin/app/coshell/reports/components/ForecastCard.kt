package app.coshell.reports.components

import app.coshell.ui.motion.SwapText
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import app.coshell.R
import app.coshell.core.designsystem.amount.shownAmount
import app.coshell.core.designsystem.charts.CoShellForecastLine
import app.coshell.core.designsystem.components.CoShellCard
import app.coshell.core.designsystem.components.CoShellSegmented
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.reports.ForecastHorizons
import app.coshell.reports.ReportsUiState.Forecast
import app.coshell.ui.format.formatDayMonth

/**
 * «Прогноз остатка» (#770, poll 2026-11-27 «А», boards `Forecast770`, `Forecast770Low`): the balance of the accounts in the total
 * 30 or 60 days ahead. One calm line under the title says the lowest day, or — when the money runs out — from which day and by
 * how much, in the attention colour (never red, never «превышено»).
 */
@Composable
internal fun ForecastCard(forecast: Forecast, modifier: Modifier = Modifier) {
    var chosen by rememberSaveable { mutableIntStateOf(0) }
    val view = forecast.views[chosen.coerceIn(0, forecast.views.lastIndex)]
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    CoShellCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.l)) {
            Text(stringResource(R.string.reports_forecast_title), style = MaterialTheme.typography.titleSmall, modifier = Modifier.semantics { heading() })
            Text(stringResource(R.string.reports_forecast_legend), style = MaterialTheme.typography.bodySmall, color = muted, modifier = Modifier.padding(top = Spacing.xs))
            val verdict = if (view.warning && view.belowFrom != null && view.shortfall != null) {
                stringResource(R.string.reports_forecast_short, formatDayMonth(view.belowFrom), shownAmount(view.shortfall))
            } else {
                stringResource(R.string.reports_forecast_low, formatDayMonth(view.lowDay), shownAmount(view.lowAmount))
            }
            SwapText(
                verdict,
                style = MaterialTheme.typography.bodyMedium,
                color = if (view.warning) LocalStatusPalette.current.warning else muted,
                modifier = Modifier.padding(top = Spacing.xs),
            )
            CoShellForecastLine(
                past = view.past,
                future = view.future,
                lowest = view.lowest,
                warning = view.warning,
                description = stringResource(R.string.reports_forecast_description, ForecastHorizons[chosen.coerceIn(0, ForecastHorizons.lastIndex)], verdict),
                startLabel = formatDayMonth(view.firstDay),
                todayLabel = stringResource(R.string.reports_forecast_today),
                endLabel = formatDayMonth(view.lastDay),
                modifier = Modifier.padding(top = Spacing.m),
            )
            CoShellSegmented(
                labels = ForecastHorizons.map { stringResource(R.string.reports_forecast_days, it) },
                selected = chosen,
                onSelect = { chosen = it },
                modifier = Modifier.padding(top = Spacing.m),
            )
        }
    }
}
