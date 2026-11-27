package app.coshell.reports.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.coshell.R
import app.coshell.core.designsystem.amount.AmountText
import app.coshell.core.designsystem.components.CoShellListRow
import app.coshell.core.designsystem.theme.AmountTextStyle
import app.coshell.reports.Conversion
import app.coshell.ui.format.formatDayMonth
import app.coshell.ui.sheet.ModalSheet

/**
 * «Подробнее» of «Оплата в другой валюте» (#786): every purchase of the period, newest first — the day and the card, what was
 * paid, the price, the rate and how far it was from the official one.
 */
@Composable
internal fun ConversionSheet(conversion: Conversion, onClosed: () -> Unit) {
    ModalSheet(title = stringResource(R.string.reports_conversion_title), onClosed = onClosed, subtitle = stringResource(R.string.reports_conversion_sheet_subtitle)) {
        Column {
            conversion.purchases.forEach { p ->
                val account = p.account ?: stringResource(R.string.reports_conversion_gone)
                val percent = stringResource(if (p.worse) R.string.reports_conversion_worse else R.string.reports_conversion_better_rate, p.percent)
                CoShellListRow(
                    title = stringResource(R.string.reports_conversion_row_title, formatDayMonth(p.day), account),
                    subtitle = listOf(p.price, p.rate, percent).joinToString(SEPARATOR),
                    subtitleMaxLines = 2,
                    trailing = { AmountText(p.paid, AmountTextStyle) },
                    dense = true,
                )
            }
        }
    }
}

private const val SEPARATOR = " · "
