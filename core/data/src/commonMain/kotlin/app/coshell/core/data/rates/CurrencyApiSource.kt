package app.coshell.core.data.rates

import app.coshell.core.domain.rates.RatesSnapshot
import app.coshell.core.domain.rates.RatesSource
import app.coshell.core.model.CurrencyCode
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * `fawazahmed0/exchange-api` (free, no key, 150+ fiat currencies and crypto, history by date).
 * The same files are served by jsDelivr and by Cloudflare Pages ([Mirror]); both are tried.
 * Answer: `{"date": "2026-11-08", "usd": {"ars": 1049.69, "amd": 386.1, "usdt": 0.9998, ...}}`.
 */
class CurrencyApiSource(
    private val http: HttpGet,
    private val mirror: Mirror,
    private val base: CurrencyCode = CurrencyCode("USD"),
) : RatesSource {

    enum class Mirror { JSDELIVR, CLOUDFLARE }

    override val name: String = "currency-api/" + mirror.name.lowercase()

    override suspend fun fetch(date: LocalDate?): RatesSnapshot =
        parseCurrencyApi(http.get(url(date)), base, name)

    internal fun url(date: LocalDate?): String {
        val version = date?.toString() ?: "latest"
        val file = "v1/currencies/${base.value.lowercase()}.json"
        return when (mirror) {
            Mirror.JSDELIVR -> "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@$version/$file"
            Mirror.CLOUDFLARE -> "https://$version.currency-api.pages.dev/$file"
        }
    }
}

internal fun parseCurrencyApi(text: String, base: CurrencyCode, source: String): RatesSnapshot {
    val root = ratesJson.parseToJsonElement(text).jsonObject
    val date = LocalDate.parse(requireNotNull(root["date"]) { "No date" }.jsonPrimitive.content)
    val table = root[base.value.lowercase()] as? JsonObject ?: error("No ${base.value.lowercase()} table")
    return RatesSnapshot(date, base, ratesFromObject(table, base, date), source)
}
