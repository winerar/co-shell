package app.coshell.core.domain.rates

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class RatesProviderTest {
    private val day = LocalDate(2026, 11, 8)
    private val usd = CurrencyCode("USD")

    private fun snapshot(source: String, rates: Int = 1) = RatesSnapshot(
        date = day,
        base = usd,
        rates = List(rates) { ExchangeRate(day, usd, CurrencyCode("ARS"), Rate.parse("1049.69"), RateOrigin.DOWNLOADED) },
        source = source,
    )

    private class FakeSource(override val name: String, val answer: (LocalDate?) -> RatesSnapshot?) : RatesSource {
        val requests = mutableListOf<LocalDate?>()
        override suspend fun fetch(date: LocalDate?): RatesSnapshot? {
            requests += date
            return answer(date)
        }
    }

    @Test
    fun firstWorkingSourceWins() = runTest {
        val main = FakeSource("main") { snapshot("main") }
        val backup = FakeSource("backup") { snapshot("backup") }
        assertEquals("main", RatesProvider(listOf(main, backup)).fetch(day).source)
        assertEquals(listOf<LocalDate?>(day), main.requests)
        assertEquals(emptyList(), backup.requests)
    }

    @Test
    fun failedOrEmptySourcesAreSkipped() = runTest {
        val broken = FakeSource("broken") { error("HTTP 503") }
        val empty = FakeSource("empty") { snapshot("empty", rates = 0) }
        val latestOnly = FakeSource("latest-only") { null }
        val backup = FakeSource("backup") { snapshot("backup") }
        val provider = RatesProvider(listOf(broken, empty, latestOnly, backup))
        assertEquals("backup", provider.fetch(null).source)
    }

    @Test
    fun allFailedReportsEveryCause() = runTest {
        val provider = RatesProvider(
            listOf(FakeSource("a") { error("timeout") }, FakeSource("b") { null }),
        )
        val e = assertFailsWith<RatesUnavailableException> { provider.fetch(day) }
        assertEquals(2, e.causes.size)
        assertEquals("a: timeout", e.causes[0].message)
    }

    @Test
    fun cancellationIsNotSwallowed() = runTest {
        val provider = RatesProvider(
            listOf(FakeSource("a") { throw CancellationException("stop") }, FakeSource("b") { snapshot("b") }),
        )
        assertFailsWith<CancellationException> { provider.fetch(day) }
    }
}
