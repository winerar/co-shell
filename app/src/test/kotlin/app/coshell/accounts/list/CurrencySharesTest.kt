package app.coshell.accounts.list

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class CurrencySharesTest {
    @Test
    fun `percents always add up to one hundred`() {
        assertEquals(listOf(34, 33, 33), percentsOf(listOf(1, 1, 1)))
        assertEquals(100, percentsOf(listOf(7, 13, 29, 51, 3)).sum())
        assertEquals(listOf(0, 0), percentsOf(listOf(0, -5)))
    }

    @Test
    fun `one currency needs no bar`() {
        assertTrue(currencyShares(listOf("USD" to 500L)).isEmpty())
        assertTrue(currencyShares(listOf("USD" to 500L, "ARS" to -10L)).isEmpty())
    }

    @Test
    fun `shares go from the biggest and the tail becomes others`() {
        val parts = listOf("AMD" to 5L, "USD" to 50L, "ARS" to 30L, "EUR" to 10L, "RUB" to 3L, "GEL" to 2L)
        val shares = currencyShares(parts)
        assertEquals(listOf("USD", "ARS", "EUR", "AMD", null), shares.map { it.currency })
        assertEquals(100, shares.sumOf { it.percent })
    }
}
