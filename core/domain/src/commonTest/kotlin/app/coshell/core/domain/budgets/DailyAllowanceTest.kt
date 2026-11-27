package app.coshell.core.domain.budgets

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DailyAllowanceTest {
    private val ars = CurrencyCode("ARS")
    private fun ars(major: Long) = Money(major * 100, ars)
    private val zero = ars(0)

    @Test
    fun `splits what is left after spending, regular payments and goals over the days`() {
        val a = DailyAllowance.of(base = ars(600_000), spent = ars(451_000), spentToday = zero, upcoming = ars(65_000), goals = ars(35_000), days = 5)
        assertEquals(ars(49_000), a.left)
        assertEquals(ars(9_800), a.perDay)
        assertFalse(a.over)
    }

    @Test
    fun `today's spending does not shrink today's allowance`() {
        val morning = DailyAllowance.of(ars(600_000), ars(451_000), zero, ars(65_000), ars(35_000), 5)
        val evening = DailyAllowance.of(ars(600_000), ars(453_450), ars(2_450), ars(65_000), ars(35_000), 5)
        assertEquals(morning.perDay, evening.perDay)
        assertEquals(ars(2_450), evening.spentToday)
        assertEquals(ars(46_550), evening.left)
    }

    @Test
    fun `rounds down so the days never get more than there is`() {
        val a = DailyAllowance.of(Money(1_000, ars), zero, zero, zero, zero, 3)
        assertEquals(Money(333, ars), a.perDay)
    }

    @Test
    fun `a spent plan gives zero, never a negative allowance`() {
        val a = DailyAllowance.of(ars(100_000), ars(112_000), ars(1_000), zero, zero, 5)
        assertEquals(zero, a.perDay)
        assertTrue(a.over)
        assertEquals(ars(-12_000), a.left)
    }

    @Test
    fun `needs at least today`() {
        assertFailsWith<IllegalArgumentException> { DailyAllowance.of(ars(1), zero, zero, zero, zero, 0) }
    }
}
