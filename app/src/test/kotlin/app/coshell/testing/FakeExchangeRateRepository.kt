package app.coshell.testing

import app.coshell.core.domain.rates.ExchangeRateRepository
import app.coshell.core.domain.rates.RatesSnapshot
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

/** Keeps rates in memory; [rates] can be set directly in tests. */
class FakeExchangeRateRepository : ExchangeRateRepository {
    val rates = MutableStateFlow<List<ExchangeRate>>(emptyList())

    override fun observeRates(): Flow<List<ExchangeRate>> = rates
    override fun observeManualRates(): Flow<List<ExchangeRate>> =
        rates.map { list -> list.filter { it.origin == app.coshell.core.model.RateOrigin.MANUAL } }
    override suspend fun saveManualRate(rate: ExchangeRate) { rates.value += rate }
    override suspend fun deleteManualRate(date: LocalDate, a: CurrencyCode, b: CurrencyCode) {
        rates.value = rates.value.filterNot { it.date == date && setOf(it.from, it.to) == setOf(a, b) }
    }
    override suspend fun saveDownloaded(snapshot: RatesSnapshot) { rates.value += snapshot.rates }
    override suspend fun latestDownloadedDate(): LocalDate? = rates.value.maxOfOrNull { it.date }
    override suspend fun downloadedDates(): Set<LocalDate> = rates.value.map { it.date }.toSet()
    override suspend fun operationDates(timeZone: TimeZone): Set<LocalDate> = emptySet()
    override suspend fun usedCurrencies(): Set<CurrencyCode> = emptySet()
}
