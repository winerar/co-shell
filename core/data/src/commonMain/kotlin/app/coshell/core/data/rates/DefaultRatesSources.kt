package app.coshell.core.data.rates

import app.coshell.core.domain.rates.RatesProvider
import kotlinx.datetime.LocalDate

/** Order of tries: currency-api via jsDelivr → the same via Cloudflare → open.er-api (latest only). */
fun defaultRatesProvider(http: HttpGet): RatesProvider = RatesProvider(
    listOf(
        CurrencyApiSource(http, CurrencyApiSource.Mirror.JSDELIVR),
        CurrencyApiSource(http, CurrencyApiSource.Mirror.CLOUDFLARE),
        OpenErApiSource(http),
    ),
)

/** First day currency-api has history for; older operations use the nearest known rate. */
val RATES_HISTORY_START = LocalDate(2024, 3, 2)
