package app.coshell.rates.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.coshell.R
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.theme.LocalStatusPalette
import app.coshell.core.designsystem.theme.RateTextStyle
import app.coshell.core.designsystem.tokens.Radii
import app.coshell.core.designsystem.tokens.Sizes
import app.coshell.rates.list.CurrentRateItem
import app.coshell.rates.list.formatRateDate

/** Currency chip, «1 USD = 1 049,69 ARS» and «на 9 ноября · вручную»; tap sets the rate by hand (board `Rates`). */
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
        leading = { CurrencyChip(item.currency) },
        onClick = onClick,
    )
}

/** The currency's code in a small rounded tile, amber like the codes of the keypad. */
@Composable
private fun CurrencyChip(code: String) {
    Box(
        Modifier.size(width = Sizes.currencyChipWidth, height = Sizes.currencyChipHeight)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(Radii.medium)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            code,
            style = RateTextStyle.copy(fontSize = ChipText, fontWeight = FontWeight.Bold),
            color = LocalStatusPalette.current.warning,
            maxLines = 1,
        )
    }
}

private val ChipText = 11.sp
