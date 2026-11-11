package app.coshell.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MoneyTest {
    private val rub = CurrencyCode("RUB")
    private val eur = CurrencyCode("EUR")
    private val jpy = CurrencyCode("JPY")

    @Test
    fun `adds and subtracts amounts of the same currency`() {
        val a = Money(1050, rub)
        val b = Money(250, rub)
        assertEquals(Money(1300, rub), a + b)
        assertEquals(Money(800, rub), a - b)
        assertEquals(Money(-1050, rub), -a)
    }

    @Test
    fun `refuses to mix currencies`() {
        assertFailsWith<IllegalArgumentException> { Money(1, rub) + Money(1, eur) }
        assertFailsWith<IllegalArgumentException> { Money(1, rub) < Money(1, eur) }
    }

    @Test
    fun `detects overflow instead of wrapping around`() {
        assertFailsWith<ArithmeticException> { Money(Long.MAX_VALUE, rub) + Money(1, rub) }
        assertFailsWith<ArithmeticException> { -Money(Long.MIN_VALUE, rub) }
    }

    @Test
    fun `ofMajor respects currency fraction digits`() {
        assertEquals(Money(1250, rub), Money.ofMajor(12, 50, rub))
        assertEquals(Money(-1250, rub), Money.ofMajor(-12, 50, rub))
        assertEquals(Money(1500, jpy), Money.ofMajor(1500, currency = jpy))
        assertFailsWith<IllegalArgumentException> { Money.ofMajor(1, 5, jpy) }
        assertFailsWith<IllegalArgumentException> { Money.ofMajor(1, 100, rub) }
    }

    @Test
    fun `sums a list`() {
        val total = listOf(Money(100, rub), Money(250, rub), Money(-50, rub)).sum(rub)
        assertEquals(Money(300, rub), total)
        assertTrue(emptyList<Money>().sum(rub).isZero)
    }

    @Test
    fun `scale multiplies by a fraction and rounds half to even`() {
        assertEquals(Money(146_667, rub), Money(400_000, rub).scale(11, 30))
        assertEquals(Money(2, rub), Money(5, rub).scale(1, 2)) // 2.5 → 2
        assertEquals(Money(4, rub), Money(7, rub).scale(1, 2)) // 3.5 → 4
        assertEquals(Money(-4, rub), Money(-7, rub).scale(1, 2))
        assertTrue(Money(100, rub).scale(0, 3).isZero)
    }

    @Test
    fun `scale does not overflow midway and rejects bad fractions`() {
        assertEquals(Money(Long.MAX_VALUE, rub), Money(Long.MAX_VALUE, rub).scale(31, 31))
        assertFailsWith<ArithmeticException> { Money(Long.MAX_VALUE, rub).scale(2, 1) }
        assertFailsWith<IllegalArgumentException> { Money(1, rub).scale(1, 0) }
        assertFailsWith<IllegalArgumentException> { Money(1, rub).scale(-1, 2) }
    }
}
