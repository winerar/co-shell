package app.coshell.core.domain.settings

import app.coshell.core.model.CurrencyCode
import kotlin.test.Test
import kotlin.test.assertEquals

class ReportCurrenciesTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val amd = CurrencyCode("AMD")

    @Test
    fun `add appends once`() {
        assertEquals(listOf(usd, ars), ReportCurrencies.add(listOf(usd), ars))
        assertEquals(listOf(usd, ars), ReportCurrencies.add(listOf(usd, ars), ars))
    }

    @Test
    fun `remove keeps the last currency`() {
        assertEquals(listOf(ars), ReportCurrencies.remove(listOf(usd, ars), usd))
        assertEquals(listOf(ars), ReportCurrencies.remove(listOf(ars), ars))
    }

    @Test
    fun `make main moves to the front and keeps the rest in order`() {
        assertEquals(listOf(amd, usd, ars), ReportCurrencies.makeMain(listOf(usd, ars, amd), amd))
        assertEquals(listOf(amd, usd), ReportCurrencies.makeMain(listOf(usd), amd))
    }

    @Test
    fun `normalize drops duplicates and falls back to the default`() {
        assertEquals(listOf(usd, ars), ReportCurrencies.normalize(listOf(usd, ars, usd)))
        assertEquals(ReportCurrencySettings.DEFAULT, ReportCurrencies.normalize(emptyList()))
    }

    @Test
    fun `stored text round-trips and broken codes are skipped`() {
        assertEquals("USD,ARS", ReportCurrencies.encode(listOf(usd, ars, usd)))
        assertEquals(listOf(usd, ars), ReportCurrencies.decode("USD, ars,??"))
        assertEquals(ReportCurrencySettings.DEFAULT, ReportCurrencies.decode(null))
        assertEquals(ReportCurrencySettings.DEFAULT, ReportCurrencies.decode(""))
    }
}
