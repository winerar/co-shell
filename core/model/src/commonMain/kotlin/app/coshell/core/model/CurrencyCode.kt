package app.coshell.core.model

import kotlin.jvm.JvmInline

/**
 * Currency identifier: an ISO 4217 code (`RUB`, `EUR`) or a custom code for
 * non-ISO assets (`USDT`, `BTC`). Always upper case, 3–10 latin letters or digits.
 */
@JvmInline
value class CurrencyCode(val value: String) {
    init {
        require(PATTERN.matches(value)) { "Invalid currency code: '$value'" }
    }

    override fun toString(): String = value

    companion object {
        private val PATTERN = Regex("[A-Z0-9]{3,10}")

        fun parseOrNull(raw: String): CurrencyCode? =
            raw.trim().uppercase().takeIf(PATTERN::matches)?.let(::CurrencyCode)
    }
}
