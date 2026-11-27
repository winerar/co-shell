package app.coshell.rates.list.components

import app.coshell.core.designsystem.components.StatusTone
import app.coshell.core.designsystem.components.CoShellStatusChip
import app.coshell.core.designsystem.theme.RateChipStyle
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import app.coshell.core.designsystem.charts.CoShellSparkline
import app.coshell.core.designsystem.theme.LocalAmountPalette
import app.coshell.core.designsystem.tokens.Spacing
import app.coshell.rates.list.RateTrendUi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.rates.list.CurrentRateItem
import app.coshell.rates.list.formatRateDate

/**
 * Currency chip, «1 USD = 1 049,69 ARS» and «на 9 ноября · вручную»; tap sets the rate by hand (board `Rates`).
 */
@Composable
internal fun CurrentRateRow(item: CurrentRateItem, base: String, onClick: () -> Unit) {
    val details = buildList {
        if (item.isManual) add(stringResource(R.string.rates_origin_manual))
        item.date?.takeIf { !item.isManual }?.let { add(stringResource(R.string.rates_from_date, formatRateDate(it))) }
        if (item.viaPivot) add(stringResource(R.string.rates_origin_cross))
    }.joinToString(" · ")
    CoShellListRow(
        title = {
            if (item.rateText != null) {
                Text(stringResource(R.string.rates_line, base, item.rateText, item.currency), style = RateTextStyle)
            } else {
                Text(item.currency, style = RateTextStyle)
            }
        },
        supporting = {
            if (item.rateText == null) {
                Text(stringResource(R.string.rates_no_rate), color = LocalStatusPalette.current.warning)
            } else {
                Text(details)
            }
        },
        leading = { CoShellStatusChip(item.currency, StatusTone.Work) },
        trailing = item.trend?.let { trend -> { TrendBlock(trend) } },
        onClick = onClick,
    )
}


/** Sparkline and «−6,1 % за 30 дн.» at the end of the row (#195, board `Idea10A`): a fall in the expense colour, a rise in the income colour. */
@Composable
private fun TrendBlock(trend: RateTrendUi) {
    val palette = LocalAmountPalette.current
    val color = when {
        trend.direction < 0 -> palette.expense
        trend.direction > 0 -> palette.income
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val words = stringResource(R.string.rates_change_30d, trend.change)
    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        CoShellSparkline(trend.points, color, words)
        Text(words, style = MaterialTheme.typography.labelSmall, color = color, maxLines = 1)
    }
}
