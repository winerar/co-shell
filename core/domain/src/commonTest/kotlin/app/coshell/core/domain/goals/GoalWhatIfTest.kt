package app.coshell.core.domain.goals

import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.BalanceDay
import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.domain.reports.ReportLedger
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GoalWhatIfTest {
    private val usd = CurrencyCode("USD")
    private val today = LocalDate(2026, 11, 27)
    private fun usd(units: Long) = Money(units * 100, usd)

    @Test
    fun `the pace is the account's average monthly gain over ninety days`() {
        val days = listOf(
            BalanceDay(LocalDate(2026, 9, 1), "savings", 300_00),
            BalanceDay(LocalDate(2026, 10, 1), "savings", 300_00),
            BalanceDay(LocalDate(2026, 11, 1), "savings", 300_00),
            BalanceDay(LocalDate(2026, 11, 2), "card", 5_000_00),
            BalanceDay(LocalDate(2026, 6, 1), "savings", 9_000_00),
        )
        assertEquals(usd(300), savingPace(days, "savings", usd, today))
    }

    @Test
    fun `spending less on cafe brings the goal months closer`() {
        // 1 760 left at 160 a month is 11 months; 20 % of 450 more makes 250 a month: 8 months.
        val result = goalWhatIf(usd(1_760), usd(160), WhatIfCategory("cafe", usd(450)), 20, today)!!
        assertEquals(usd(90), result.saved)
        assertEquals(11, result.monthsNow)
        assertEquals(8, result.monthsWith)
        assertEquals(3, result.monthsEarlier)
        assertEquals(LocalDate(2027, 7, 27), result.dateWith)
    }

    @Test
    fun `a goal that does not get closer now still gets a date with the saving`() {
        val result = goalWhatIf(usd(900), usd(-20), WhatIfCategory("cafe", usd(1_000)), 30, today)!!
        assertNull(result.monthsNow)
        assertEquals(4, result.monthsWith)
    }

    @Test
    fun `nothing left, a tiny saving or another currency give nothing`() {
        assertNull(goalWhatIf(usd(0), usd(100), WhatIfCategory("cafe", usd(100)), 20, today))
        assertNull(goalWhatIf(usd(1_000), usd(500), WhatIfCategory("cafe", usd(10)), 10, today))
        assertNull(goalWhatIf(usd(1_000), usd(100), WhatIfCategory("cafe", Money(10_000_00, CurrencyCode("ARS"))), 20, today))
    }

    @Test
    fun `categories to save on leave out regular payments, biggest first`() {
        val categories = listOf(
            Category("cafe", "Кафе", CategoryType.EXPENSE),
            Category("home", "Дом", CategoryType.EXPENSE),
            Category("rent", "Аренда", CategoryType.EXPENSE, parentId = "home"),
            Category("taxi", "Такси", CategoryType.EXPENSE),
        )
        val months = (8..10).map { budgetPeriod(YearMonth(2026, it)) }
        val days = (8..10).flatMap { m ->
            listOf(
                ReportDay(LocalDate(2026, m, 3), "cafe", TransactionType.EXPENSE, usd(150)),
                ReportDay(LocalDate(2026, m, 4), "rent", TransactionType.EXPENSE, usd(600)),
                ReportDay(LocalDate(2026, m, 5), "taxi", TransactionType.EXPENSE, usd(60)),
            )
        }
        val found = whatIfCategories(ReportLedger(days, categories, RateBook(emptyList()), usd), categories, months, scheduled = setOf("rent"))
        assertEquals(listOf("cafe" to usd(150), "taxi" to usd(60)), found.map { it.categoryId to it.monthly })
    }
}
