package app.coshell.rates.list

import app.coshell.ui.format.formatDayMonth
import kotlinx.datetime.LocalDate

/** «9 ноября» — as rate dates read on «Обзоре» (board `Rates`). */
internal fun formatRateDate(date: LocalDate): String = formatDayMonth(date)

/** Human name of a stored source id: `currency-api/jsdelivr` → `currency-api`, `open-er-api` → `ExchangeRate-API`. */
internal fun sourceLabel(source: String): String = when {
    source.startsWith("currency-api") -> "currency-api"
    source == "open-er-api" -> "ExchangeRate-API"
    else -> source
}
