package app.coshell.core.domain.hints

import app.coshell.core.domain.budgets.BudgetPeriod
import app.coshell.core.domain.budgets.Spent
import app.coshell.core.domain.budgets.budgetProgress
import app.coshell.core.model.Budget
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

class LimitWatchTest {
    private val usd = CurrencyCode("USD")
    private val today = LocalDate(2026, 11, 20)
    private val period = BudgetPeriod(YearMonth(2026, 11), LocalDate(2026, 11, 1), LocalDate(2026, 12, 1))

    private fun progress(category: String, limit: Long, spent: Long) = budgetProgress(
        Budget(category, category, YearMonth(2026, 11), Money(limit, usd)),
        period, today, Spent(Money(spent, usd)), Money(0, usd),
    )

    @Test fun `nothing below eighty percent`() = assertNull(LimitWatch.nearest(listOf(progress("a", 1000, 799))))

    @Test fun `eighty percent counts`() = assertEquals("a", LimitWatch.nearest(listOf(progress("a", 1000, 800)))?.budget?.categoryId)

    @Test fun `over the limit does not count`() = assertNull(LimitWatch.nearest(listOf(progress("a", 1000, 1000), progress("b", 1000, 1200))))

    @Test fun `the one with least left wins`() =
        assertEquals("b", LimitWatch.nearest(listOf(progress("a", 1000, 810), progress("b", 1000, 950)))?.budget?.categoryId)
}
