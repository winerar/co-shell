package app.coshell.home

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The back of the hero card on «Обзор» (#519): income − expenses and the daily average. */
class MonthResultTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val format = MoneyFormat.RU
    private val today = LocalDate(2026, 11, 8)

    private fun usd(major: Long) = Money.ofMajor(major, currency = usd)

    @Test
    fun `plus month shows a signed result and the average per day so far`() {
        val result = monthResult(usd(2_100), usd(800), today, format)!!
        assertEquals("+" + format.format(usd(1_300)), result.result)
        assertTrue(result.positive)
        assertEquals(format.format(usd(800).scale(1, 8)), result.perDay)
    }

    @Test
    fun `minus month keeps the minus of the formatter and is not positive`() {
        val result = monthResult(usd(100), usd(250), today, format)!!
        assertEquals(format.format(usd(-150)), result.result)
        assertFalse(result.positive)
    }

    @Test
    fun `nothing to show without both sums or when their currencies differ`() {
        assertNull(monthResult(null, usd(10), today, format))
        assertNull(monthResult(usd(10), null, today, format))
        assertNull(monthResult(usd(10), Money.ofMajor(10, currency = ars), today, format))
    }
}
