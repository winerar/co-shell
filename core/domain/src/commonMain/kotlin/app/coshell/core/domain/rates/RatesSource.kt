package app.coshell.core.domain.rates

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import kotlinx.datetime.LocalDate

/** Downloaded rates of one day, all quoted as `1 base = rate X`. */
data class RatesSnapshot(
    /** Date the source publishes the rates for (may be earlier than requested, e.g. weekends). */
    val date: LocalDate,
    val base: CurrencyCode,
    /** [app.coshell.core.model.RateOrigin.DOWNLOADED] rates `base → X`, one per currency. */
    val rates: List<ExchangeRate>,
    /** Short source name for logs and the future "Rates" screen, e.g. `currency-api`. */
    val source: String,
)

/** One place rates can be downloaded from (an HTTP API). */
interface RatesSource {
    val name: String

    /**
     * Rates for [date], or the latest ones when [date] is `null`.
     * Returns `null` when the source has no data of that kind (e.g. a latest-only API asked for history).
     * Throws on network or format errors.
     */
    suspend fun fetch(date: LocalDate?): RatesSnapshot?
}

/** None of the sources gave rates; [causes] has one error per failed source. */
class RatesUnavailableException(val causes: List<Throwable>) :
    Exception("No rates source answered: " + causes.joinToString { it.message ?: it::class.simpleName.orEmpty() })
