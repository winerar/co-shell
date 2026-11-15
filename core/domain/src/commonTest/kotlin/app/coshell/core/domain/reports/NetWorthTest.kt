package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetFixtures.november
import app.coshell.core.domain.budgets.BudgetFixtures.rates
import app.coshell.core.domain.budgets.BudgetFixtures.today
import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NetWorthTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val book = RateBook(rates) // USD→ARS: 1 000 from 1 Sep, 2 000 from 5 Nov
    private fun usd(units: Long) = Money(units * 100, usd)

    private val cash = Account("cash", "Доллары", usd, initialBalanceMinor = 1_000_00)
    private val peso = Account("peso", "Песо", ars, initialBalanceMinor = 1_000_000_00)
    private val debt = Account("debt", "Долг", usd, initialBalanceMinor = -500_00, excludedFromTotal = true)

    private fun d(month: Int, day: Int, account: String, minor: Long) = BalanceDay(LocalDate(2026, month, day), account, minor)

    private val periods = reportMonths(november, 3)

    @Test
    fun `each month end counts every account at that day's rate, excluded accounts left out`() {
        val worth = netWorth(listOf(cash, peso, debt), listOf(d(10, 3, "cash", 200_00), d(11, 2, "cash", -100_00)), periods, book, usd, today)!!

        // September end: 1 000 + 1 000 000 ARS / 1 000; October: +200; November (the 11th): −100, the peso halved.
        assertEquals(listOf(usd(2_000), usd(2_200), usd(1_600)), worth.points.map { it.amount })
        assertEquals(listOf(LocalDate(2026, 9, 30), LocalDate(2026, 10, 31), today), worth.points.map { it.asOf })
        assertEquals(listOf(false, false, true), worth.points.map { it.running })
        assertEquals(usd(-400), worth.change)
        assertEquals(-20, worth.percent)
        assertEquals(usd(-600), worth.changeOver(1))
        assertTrue(!worth.atMax)
    }

    @Test
    fun `why it changed — what was saved and the rest (rates)`() {
        val saved = mapOf(periods[1] to usd(200), periods[2] to usd(-100))
        val worth = netWorth(listOf(cash, peso), listOf(d(10, 3, "cash", 200_00), d(11, 2, "cash", -100_00)), periods, book, usd, today, saved)!!

        assertEquals(listOf(usd(200) to usd(0), usd(-100) to usd(-500)), worth.changes.map { it.saved to it.other })
        assertEquals(listOf(usd(200), usd(-600)), worth.changes.map { it.total })
        assertEquals(listOf(false, true), worth.changes.map { it.running })
    }

    @Test
    fun `an account without a rate is left out and named`() {
        val amd = Account("amd", "Драмы", CurrencyCode("AMD"), initialBalanceMinor = 10_000_00)
        val worth = netWorth(listOf(cash, amd), emptyList(), periods, book, usd, today)!!

        assertEquals(usd(1_000), worth.now)
        assertEquals(setOf(CurrencyCode("AMD")), worth.missing)
    }

    @Test
    fun `months not started yet are not points, nothing counted — no card`() {
        val ahead = reportMonths(YearMonth(2027, 1), 3)
        assertEquals(listOf(november), netWorth(listOf(cash), emptyList(), ahead, book, usd, today)!!.points.map { it.period.month })
        assertNull(netWorth(listOf(debt), emptyList(), periods, book, usd, today))
        assertNull(netWorth(listOf(cash), emptyList(), listOf(budgetPeriod(YearMonth(2027, 1))), book, usd, today))
    }
}
