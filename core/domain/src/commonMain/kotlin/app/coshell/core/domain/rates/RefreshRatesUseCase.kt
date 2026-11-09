package app.coshell.core.domain.rates

import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** Outcome of one [RefreshRatesUseCase] run. */
data class RatesRefreshResult(
    /** Date of the latest rates saved in this run; `null` if they were already fresh or the download failed. */
    val latestDate: LocalDate?,
    /** `true` when the latest rates were due but no source answered — worth retrying soon. */
    val latestFailed: Boolean,
    /** Past dates of operations whose rates were downloaded in this run. */
    val historyDays: Int,
    /** Past dates that still have no rates (failed, or over [RefreshRatesUseCase.historyLimit]). */
    val historyLeft: Int,
)

/**
 * Background rates update (ARCHITECTURE §6), run daily by WorkManager:
 * 1. the latest rates, unless rates for today are already stored;
 * 2. history for days of operations that have no downloaded rates yet (old entries, imports),
 *    newest first, at most [historyLimit] days per run so a big import is spread over several days.
 * Only currencies the family uses are kept ([keepOnly]).
 */
class RefreshRatesUseCase(
    private val provider: RatesProvider,
    private val repository: ExchangeRateRepository,
    private val clock: Clock,
    /** Earliest day the history sources know; older operations use the nearest known rate. */
    private val historyStart: LocalDate,
    private val timeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
    val historyLimit: Int = 30,
) {
    suspend operator fun invoke(): RatesRefreshResult {
        val zone = timeZone()
        val today = clock.todayIn(zone)
        val used = repository.usedCurrencies()

        var latestDate: LocalDate? = null
        var latestFailed = false
        val stored = repository.latestDownloadedDate()
        if (stored == null || stored < today) {
            try {
                val snapshot = provider.fetch(null)
                repository.saveDownloaded(snapshot.keepOnly(used))
                latestDate = snapshot.date
            } catch (e: CancellationException) {
                throw e
            } catch (_: RatesUnavailableException) {
                latestFailed = true
            }
        }

        val newest = repository.latestDownloadedDate() ?: today
        val missing = (repository.operationDates(zone) - repository.downloadedDates())
            .filter { it >= historyStart && it < newest }
            .sortedDescending()
        var done = 0
        var failedInRow = 0
        for (date in missing.take(historyLimit)) {
            try {
                repository.saveDownloaded(provider.fetch(date).keepOnly(used))
                done++
                failedInRow = 0
            } catch (e: CancellationException) {
                throw e
            } catch (_: RatesUnavailableException) {
                if (++failedInRow >= MAX_FAILURES_IN_ROW) break // offline: try again next run
            }
        }
        return RatesRefreshResult(latestDate, latestFailed, done, missing.size - done)
    }

    private companion object {
        const val MAX_FAILURES_IN_ROW = 3
    }
}
