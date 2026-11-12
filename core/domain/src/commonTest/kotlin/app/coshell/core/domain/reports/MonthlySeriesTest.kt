package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.BudgetFixtures.ars
import app.coshell.core.domain.budgets.BudgetFixtures.november
import app.coshell.core.domain.budgets.BudgetFixtures.today
import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals

class MonthlySeriesTest {
    @Test
    fun `months end with the given one, oldest first, from the family's start day`() {
        val months = reportMonths(november, 3, startDay = 10)

        assertEquals(listOf(YearMonth(2026, 9), YearMonth(2026, 10), november), months.map { it.month })
        assertEquals(LocalDate(2026, 9, 10), months.first().start)
        assertEquals(LocalDate(2026, 12, 10), months.last().endExclusive)
    }

    @Test
    fun `each month has its spending, income and what was saved`() {
        val ledger = ledger(
            day(10, 3, "cafe", ars(100)),
            day(10, 4, "salary", ars(1_000), TransactionType.INCOME),
            day(11, 3, "rent", ars(700)),
            day(11, 4, "cafe", ars(50), TransactionType.INCOME),
            day(11, 5, "salary", ars(500), TransactionType.INCOME),
        )

        val series = monthlySeries(ledger, reportMonths(november, 2))

        assertEquals(listOf(ars(100) to ars(1_000), ars(650) to ars(500)), series.map { it.spent to it.earned })
        assertEquals(listOf(ars(900), ars(-150)), series.map { it.saved })
    }

    @Test
    fun `this month so far against the same days of the last one`() {
        val ledger = ledger(
            day(10, 5, "cafe", ars(500)),
            day(10, 11, "cafe", ars(320)),
            day(10, 12, "cafe", ars(1_000)),
            day(11, 2, "cafe", ars(400)),
            day(11, 11, "cafe", ars(600)),
            day(11, 12, "cafe", ars(9_999)),
        )

        val comparison = periodComparison(ledger, budgetPeriod(november), budgetPeriod(YearMonth(2026, 10)), today)

        assertEquals(ars(1_000), comparison.spent)
        assertEquals(ars(820), comparison.previousSameDays)
        assertEquals(LocalDate(2026, 10, 11), comparison.sameDay)
        assertEquals(ars(1_820), comparison.previousTotal)
        assertEquals(ars(180), comparison.difference)
    }

    @Test
    fun `the previous month is never cut past its end`() {
        val ledger = ledger(day(2, 28, "cafe", ars(10)), day(3, 31, "cafe", ars(20)))

        val comparison = periodComparison(ledger, budgetPeriod(YearMonth(2026, 3)), budgetPeriod(YearMonth(2026, 2)), LocalDate(2026, 3, 31))

        assertEquals(LocalDate(2026, 2, 28), comparison.sameDay)
        assertEquals(ars(10), comparison.previousSameDays)
    }
}
