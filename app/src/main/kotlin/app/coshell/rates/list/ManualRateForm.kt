package app.coshell.rates.list

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import kotlinx.datetime.LocalDate

/** Fields of the "manual rate" dialog: `1 from = rate to` from [date]. */
data class ManualRateForm(
    val from: String,
    val to: String,
    val rate: String,
    val date: LocalDate,
    val today: LocalDate,
    /** The manual rate being edited; `null` for a new one. */
    val original: ExchangeRate? = null,
    val fromError: CurrencyError? = null,
    val toError: CurrencyError? = null,
    val rateError: Boolean = false,
) {
    enum class CurrencyError { INVALID, SAME }
}

/** Checks the form; returns it with errors set, and the rate to save when everything is valid. */
internal fun ManualRateForm.validate(): Pair<ManualRateForm, ExchangeRate?> {
    val from = CurrencyCode.parseOrNull(from.trim().uppercase())
    val to = CurrencyCode.parseOrNull(to.trim().uppercase())
    val rate = parseRate(rate)
    val checked = copy(
        fromError = if (from == null) ManualRateForm.CurrencyError.INVALID else null,
        toError = when {
            to == null -> ManualRateForm.CurrencyError.INVALID
            to == from -> ManualRateForm.CurrencyError.SAME
            else -> null
        },
        rateError = rate == null,
    )
    val valid = checked.fromError == null && checked.toError == null && !checked.rateError
    return checked to if (valid) ExchangeRate(date, from!!, to!!, rate!!, RateOrigin.MANUAL) else null
}

/** "1 450,5" or "1450.5" → rate; `null` for empty, zero or garbage. */
internal fun parseRate(text: String): Rate? =
    Rate.parseOrNull(text.filterNot { it == ' ' || it == ' ' || it == ' ' })

/** Same day and the same pair in either direction — saving over it replaces it in the database. */
internal fun ExchangeRate.sameSlotAs(other: ExchangeRate): Boolean =
    date == other.date && setOf(from, to) == setOf(other.from, other.to)
