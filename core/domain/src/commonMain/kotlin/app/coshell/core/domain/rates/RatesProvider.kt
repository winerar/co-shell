package app.coshell.core.domain.rates

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.datetime.LocalDate

/**
 * Downloads rates from the first source that answers (ARCHITECTURE §6): the main API, its mirror,
 * then backups. A source that fails or has no data for the request is skipped.
 */
class RatesProvider(private val sources: List<RatesSource>) {

    init {
        require(sources.isNotEmpty()) { "At least one rates source is needed" }
    }

    /** Rates for [date] (`null` = latest). Throws [RatesUnavailableException] if every source failed. */
    suspend fun fetch(date: LocalDate?): RatesSnapshot {
        val errors = mutableListOf<Throwable>()
        for (source in sources) {
            try {
                val snapshot = source.fetch(date)
                if (snapshot != null && snapshot.rates.isNotEmpty()) return snapshot
                errors += NoSuchElementException("${source.name}: no rates for ${date ?: "latest"}")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errors += IllegalStateException("${source.name}: ${e.message}", e)
            }
        }
        throw RatesUnavailableException(errors)
    }
}
