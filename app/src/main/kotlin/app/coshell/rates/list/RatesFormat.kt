package app.coshell.rates.list

import app.coshell.ui.format.formatDayMonth
import kotlinx.datetime.LocalDate

/** «9 ноября» — as rate dates read on «Обзоре» (board `Rates`). */
internal fun formatRateDate(date: LocalDate): String = formatDayMonth(date)

/** `+1,2 %` / `−6,1 %` / `0,0 %` from basis points, in whole numbers (tenths of a percent, rounded half away from zero). */
internal fun formatChangeBp(bp: Int): String {
    val tenths = (kotlin.math.abs(bp) + 5) / 10
    val sign = when {
        tenths == 0 -> ""
        bp < 0 -> "\u2212"
        else -> "+"
    }
    return sign + tenths / 10 + "," + tenths % 10 + "\u00A0%"
}

/** Human name of a stored source id: `currency-api/jsdelivr` → `currency-api`, `open-er-api` → `ExchangeRate-API`. */
internal fun sourceLabel(source: String): String = when {
    source.startsWith("currency-api") -> "currency-api"
    source == "open-er-api" -> "ExchangeRate-API"
    else -> source
}
