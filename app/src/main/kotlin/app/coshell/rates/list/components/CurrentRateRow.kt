package app.coshell.rates.list.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.rates.list.CurrentRateItem
import app.coshell.rates.list.formatRateDate

/** "1 USD = 1 049,69 ARS" with "на 9 нояб. · вручную"; tap sets the rate by hand. */
@Composable
internal fun CurrentRateRow(item: CurrentRateItem, base: String, onClick: () -> Unit) {
    val details = buildList {
        item.date?.let { add(stringResource(R.string.rates_from_date, formatRateDate(it))) }
        if (item.isManual) add(stringResource(R.string.rates_origin_manual))
        if (item.viaPivot) add(stringResource(R.string.rates_origin_cross))
    }.joinToString(" · ")
    CoShellListRow(
        title = {
            if (item.rateText != null) {
                Text(stringResource(R.string.rates_line, base, item.rateText, item.currency), style = RateTextStyle)
            } else {
                Text(item.currency)
            }
        },
        supporting = {
            if (item.rateText == null) {
                Text(stringResource(R.string.rates_no_rate), color = LocalStatusPalette.current.warning)
            } else {
                Text(details)
            }
        },
        onClick = onClick,
    )
}
