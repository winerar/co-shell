package app.coshell.core.domain.deposit

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.DepositPayout
import app.coshell.core.model.DepositTerms
import app.coshell.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class DepositIncomeTest {
    private val usd = CurrencyCode("USD")
    private fun money(major: Long) = Money(major * 100, usd)
    private val today = LocalDate(2026, 3, 15)
    private fun terms(payout: DepositPayout, end: LocalDate = LocalDate(2027, 3, 15)) = DepositTerms(800, end, payout)

    @Test
    fun `interest paid at the end is simple interest for the days left`() {
        // 10 000 at 8 % for 365 days.
        assertEquals(money(800), DepositIncome.expected(terms(DepositPayout.AT_END), money(10_000), today))
        // 15 March to 14 September is 183 days of 365: 401.10.
        assertEquals(401_10L, DepositIncome.expected(terms(DepositPayout.MONTHLY, LocalDate(2026, 9, 14)), money(10_000), today)?.amountMinor)
    }

    @Test
    fun `adding the interest every month gives a bit more than simple interest`() {
        val simple = DepositIncome.expected(terms(DepositPayout.AT_END), money(10_000), today)!!
        val compound = DepositIncome.expected(terms(DepositPayout.CAPITALIZED), money(10_000), today)!!
        assertTrue(compound.amountMinor > simple.amountMinor)
        // 12 months of 8 %/12: 10 000 × (1 + 0.08/12)^12 − 10 000 = 830.01 (rounded each month).
        assertEquals(830_01L, compound.amountMinor, "got ${compound.amountMinor}")
    }

    @Test
    fun `no balance or a closed deposit has no income`() {
        assertNull(DepositIncome.expected(terms(DepositPayout.AT_END), money(0), today))
        assertNull(DepositIncome.expected(terms(DepositPayout.AT_END, LocalDate(2026, 3, 14)), money(10_000), today))
        assertEquals(money(0), DepositIncome.expected(terms(DepositPayout.AT_END, today), money(10_000), today))
    }

    @Test
    fun `the closing day is in the soon window including today`() {
        val t = terms(DepositPayout.AT_END, LocalDate(2026, 3, 21))
        assertTrue(DepositIncome.endsWithin(t, today, 7))
        assertFalse(DepositIncome.endsWithin(t, today, 6))
        assertTrue(DepositIncome.endsWithin(terms(DepositPayout.AT_END, today), today, 7))
        assertFalse(DepositIncome.endsWithin(terms(DepositPayout.AT_END, LocalDate(2026, 3, 14)), today, 7))
    }

    @Test
    fun `the rate must be a real percent`() {
        assertFailsWith<IllegalArgumentException> { DepositTerms(0, today, DepositPayout.AT_END) }
        assertFailsWith<IllegalArgumentException> { DepositTerms(10_001, today, DepositPayout.AT_END) }
    }

    @Test
    fun `the opening day makes the end-of-term income cover the whole term`() {
        // Opened half a year ago (15 Sep 2025), closes 15 Mar 2027: 546 days, 8 % of 10 000.
        val withStart = DepositTerms(800, LocalDate(2027, 3, 15), DepositPayout.AT_END, LocalDate(2025, 9, 15))
        assertEquals(119_671L, DepositIncome.expected(withStart, money(10_000), today)?.amountMinor)
        // Paid every month: what was paid is not expected again, only what is left.
        assertEquals(money(800), DepositIncome.expected(withStart.copy(payout = DepositPayout.MONTHLY), money(10_000), today))
    }

    @Test
    fun `a deposit cannot open after it closes`() {
        assertFailsWith<IllegalArgumentException> { DepositTerms(800, today, DepositPayout.AT_END, LocalDate(2026, 3, 16)) }
    }
}
