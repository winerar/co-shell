package app.coshell.rates.list

import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate

/** "9 нояб. 2026 г." in the phone's locale. */
internal fun formatRateDate(date: LocalDate): String =
    date.toJavaLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

/** Human name of a stored source id: `currency-api/jsdelivr` → `currency-api`, `open-er-api` → `ExchangeRate-API`. */
internal fun sourceLabel(source: String): String = when {
    source.startsWith("currency-api") -> "currency-api"
    source == "open-er-api" -> "ExchangeRate-API"
    else -> source
}
