package app.coshell.core.data.rates

import app.coshell.core.domain.rates.RatesProvider

/** Order of tries: currency-api via jsDelivr → the same via Cloudflare → open.er-api (latest only). */
fun defaultRatesProvider(http: HttpGet): RatesProvider = RatesProvider(
    listOf(
        CurrencyApiSource(http, CurrencyApiSource.Mirror.JSDELIVR),
        CurrencyApiSource(http, CurrencyApiSource.Mirror.CLOUDFLARE),
        OpenErApiSource(http),
    ),
)
