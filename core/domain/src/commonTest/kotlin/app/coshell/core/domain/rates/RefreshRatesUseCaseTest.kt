package app.coshell.core.domain.rates

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

class RefreshRatesUseCaseTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val today = LocalDate(2026, 11, 9)
    private val clock = object : Clock {
        override fun now() = Instant.parse("2026-11-09T15:00:00Z")
    }
    private val repo = FakeRatesRepository()
    private val failing = mutableSetOf<LocalDate?>()
    private val requests = mutableListOf<LocalDate?>()
    private val source = object : RatesSource {
        override val name = "fake"
        override suspend fun fetch(date: LocalDate?): RatesSnapshot? {
            requests += date
            if (date in failing) error("offline")
            val day = date ?: today.minusDays(1)
            return RatesSnapshot(
                day, usd,
                listOf(rate(day, ars), rate(day, CurrencyCode("EUR"))),
                "fake",
            )
        }
    }

    private fun refresh(limit: Int = 30) = RefreshRatesUseCase(
        RatesProvider(listOf(source)), repo, clock,
        historyStart = LocalDate(2024, 3, 2), timeZone = { TimeZone.UTC }, historyLimit = limit,
    )

    @Test
    fun `downloads latest rates and keeps only used currencies`() = runTest {
        repo.used = setOf(ars)
        val result = refresh()()
        assertEquals(today.minusDays(1), result.latestDate)
        assertEquals(listOf<LocalDate?>(null), requests)
        assertEquals(setOf(ars), repo.saved.single().rates.map { it.to }.toSet())
    }

    @Test
    fun `skips latest when today is already stored`() = runTest {
        repo.saved += snapshot(today)
        val result = refresh()()
        assertEquals(null, result.latestDate)
        assertEquals(emptyList(), requests)
    }

    @Test
    fun `downloads history for operation days without rates, newest first, within the limit`() = runTest {
        repo.operationDays = setOf(
            LocalDate(2026, 1, 1), LocalDate(2026, 5, 1), LocalDate(2026, 10, 1),
            LocalDate(2023, 1, 1), // before the history start
            today, // covered by the latest rates
        )
        repo.saved += snapshot(LocalDate(2026, 5, 1))
        val result = refresh(limit = 1)()
        assertEquals(listOf<LocalDate?>(null, LocalDate(2026, 10, 1)), requests)
        assertEquals(1, result.historyDays)
        assertEquals(1, result.historyLeft)
    }

    @Test
    fun `stops history after three failures in a row`() = runTest {
        repo.operationDays = (1..10).map { LocalDate(2026, 10, it) }.toSet()
        failing += repo.operationDays
        val result = refresh()()
        assertEquals(4, requests.size) // latest + 3 failed days
        assertEquals(0, result.historyDays)
        assertEquals(10, result.historyLeft)
    }

    @Test
    fun `failed latest download is reported`() = runTest {
        failing += null
        val result = refresh()()
        assertEquals(true, result.latestFailed)
        assertEquals(null, result.latestDate)
    }

    private fun rate(day: LocalDate, to: CurrencyCode) = ExchangeRate(day, usd, to, Rate.parse("2"), RateOrigin.DOWNLOADED)

    private fun snapshot(day: LocalDate) = RatesSnapshot(day, usd, listOf(rate(day, ars)), "fake")

    private fun LocalDate.minusDays(days: Int) = LocalDate.fromEpochDays(toEpochDays() - days)
}

private class FakeRatesRepository : ExchangeRateRepository {
    var used: Set<CurrencyCode> = emptySet()
    var operationDays: Set<LocalDate> = emptySet()
    val saved = mutableListOf<RatesSnapshot>()

    override fun observeRates(): Flow<List<ExchangeRate>> = emptyFlow()
    override fun observeManualRates(): Flow<List<ExchangeRate>> = emptyFlow()
    override suspend fun saveManualRate(rate: ExchangeRate) = Unit
    override suspend fun deleteManualRate(date: LocalDate, a: CurrencyCode, b: CurrencyCode) = Unit
    override suspend fun saveDownloaded(snapshot: RatesSnapshot) { saved += snapshot }
    override fun observeLatestDownload(): Flow<DownloadInfo?> = emptyFlow()
    override suspend fun latestDownloadedDate() = saved.maxOfOrNull { it.date }
    override suspend fun downloadedDates() = saved.map { it.date }.toSet()
    override suspend fun operationDates(timeZone: TimeZone) = operationDays
    override suspend fun usedCurrencies() = used
}
