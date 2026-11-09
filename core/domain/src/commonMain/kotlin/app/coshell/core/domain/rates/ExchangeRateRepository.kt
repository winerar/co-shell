package app.coshell.core.domain.rates

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Local store of exchange rates (docs/agents/database.md §«Курсы»):
 * - **manual** rates are family data — synced (stage 4) and saved in backups; at most one per
 *   currency pair and date, in either direction (saving `ARS → USD` replaces `USD → ARS` of that day);
 * - **downloaded** rates are a cache of [RatesProvider] — local only, not in backups.
 */
interface ExchangeRateRepository {
    /** Every live rate, manual and downloaded, for [RateBook]. */
    fun observeRates(): Flow<List<ExchangeRate>>

    /** Live manual rates, newest date first (for the "Rates" screen). */
    fun observeManualRates(): Flow<List<ExchangeRate>>

    /** Adds or replaces the manual rate of this pair on this date. [rate] must be [app.coshell.core.model.RateOrigin.MANUAL]. */
    suspend fun saveManualRate(rate: ExchangeRate)

    /** Soft-deletes the manual rate of the pair (either direction) on [date]; no-op if there is none. */
    suspend fun deleteManualRate(date: LocalDate, a: CurrencyCode, b: CurrencyCode)

    /** Stores a downloaded snapshot, replacing earlier rows of the same date and pair. */
    suspend fun saveDownloaded(snapshot: RatesSnapshot)

    /** Latest date with downloaded rates, `null` if nothing was downloaded yet. */
    suspend fun latestDownloadedDate(): LocalDate?

    /** Currencies of live accounts, operations (incl. transfer and receipt sides) and budgets. */
    suspend fun usedCurrencies(): Set<CurrencyCode>
}

/**
 * Leaves only rates to the [currencies] the family uses (plus the pivot, so cross rates work):
 * a snapshot has 150+ currencies, storing them all every day would bloat the database.
 */
fun RatesSnapshot.keepOnly(currencies: Set<CurrencyCode>, pivot: CurrencyCode = CurrencyCode("USD")): RatesSnapshot =
    copy(rates = rates.filter { it.to in currencies || it.to == pivot })
