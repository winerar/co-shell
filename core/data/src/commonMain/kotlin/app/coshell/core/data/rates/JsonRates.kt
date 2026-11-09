package app.coshell.core.data.rates

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal val ratesJson = Json { ignoreUnknownKeys = true }

/**
 * Reads a JSON number **as written** (never through `Double`): "1049.69", "1.2e-05", "5E+3".
 * Returns `null` for non-numbers, zero, negatives and values outside [Rate]'s range.
 */
internal fun rateFromJsonLiteral(literal: String): Rate? {
    val match = NUMBER.matchEntire(literal.trim()) ?: return null
    val (intPart, fraction, exponentText) = match.destructured
    val exponent = if (exponentText.isEmpty()) 0 else exponentText.toIntOrNull() ?: return null
    if (exponent !in -40..40) return null
    val digits = intPart + fraction
    val point = intPart.length + exponent // position of the decimal point inside `digits`
    val plain = when {
        point <= 0 -> "0." + "0".repeat(-point) + digits
        point >= digits.length -> digits + "0".repeat(point - digits.length)
        else -> digits.substring(0, point) + "." + digits.substring(point)
    }
    return Rate.parseOrNull(plain)
}

/** `{"ars": 1049.69, "amd": 386.1, ...}` → rates `base → X`; skips the base, bad codes and bad numbers. */
internal fun ratesFromObject(obj: JsonObject, base: CurrencyCode, date: LocalDate): List<ExchangeRate> =
    obj.mapNotNull { (key, value) ->
        val code = CurrencyCode.parseOrNull(key) ?: return@mapNotNull null
        if (code == base) return@mapNotNull null
        val primitive = value as? JsonPrimitive ?: return@mapNotNull null
        if (primitive.isString) return@mapNotNull null
        val rate = rateFromJsonLiteral(primitive.content) ?: return@mapNotNull null
        ExchangeRate(date, base, code, rate, RateOrigin.DOWNLOADED)
    }

private val NUMBER = Regex("""(\d+)(?:\.(\d+))?(?:[eE]([+-]?\d+))?""")
