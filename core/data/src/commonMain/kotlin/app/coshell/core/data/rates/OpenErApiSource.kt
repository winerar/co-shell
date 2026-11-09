package app.coshell.core.data.rates

import app.coshell.core.domain.rates.RatesSnapshot
import app.coshell.core.domain.rates.RatesSource
import app.coshell.core.model.CurrencyCode
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

/**
 * Backup: open.er-api.com (ExchangeRate-API open access — free, no key, ~160 fiat currencies
 * including ARS and AMD, no crypto, **latest only**, updated daily; attribution link required on
 * the "Rates" screen). Answer: `{"result": "success", "time_last_update_unix": 1731024151,
 * "base_code": "USD", "rates": {"USD": 1, "ARS": 1049.69, ...}}`.
 */
class OpenErApiSource(
    private val http: HttpGet,
    private val base: CurrencyCode = CurrencyCode("USD"),
) : RatesSource {

    override val name: String = "open-er-api"

    override suspend fun fetch(date: LocalDate?): RatesSnapshot? {
        if (date != null) return null // no history in the free plan
        return parseOpenErApi(http.get("https://open.er-api.com/v6/latest/${base.value}"), base, name)
    }
}

internal fun parseOpenErApi(text: String, base: CurrencyCode, source: String): RatesSnapshot {
    val root = ratesJson.parseToJsonElement(text).jsonObject
    val result = root["result"]?.jsonPrimitive?.content
    check(result == "success") { "Result: $result" }
    val baseCode = root["base_code"]?.jsonPrimitive?.content
    check(baseCode == base.value) { "Base: $baseCode" }
    val updatedAt = requireNotNull(root["time_last_update_unix"]) { "No update time" }.jsonPrimitive.long
    val date = LocalDate.fromEpochDays((updatedAt / SECONDS_PER_DAY).toInt())
    val table = root["rates"] as? JsonObject ?: error("No rates table")
    return RatesSnapshot(date, base, ratesFromObject(table, base, date), source)
}

private const val SECONDS_PER_DAY = 86_400L
