package app.coshell.core.domain.budgets

import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

class DailyPlannerTest {
    private val ars = CurrencyCode("ARS")
    private fun ars(major: Long) = Money(major * 100, ars)
    private val food = Category("food", "Продукты", CategoryType.EXPENSE)
    private val cafe = Category("cafe", "Кафе", CategoryType.EXPENSE)
    private val salary = Category("salary", "Зарплата", CategoryType.INCOME)
    private val cats = listOf(food, cafe, salary)
    private val today = LocalDate(2026, 11, 26)
    private val book = RateBook.of(emptyList())

    private fun spend(day: Int, cat: String, major: Long) = ReportDay(LocalDate(2026, 11, day), cat, TransactionType.EXPENSE, ars(major))
    private val rent = TransactionTemplate("rent", "Аренда", TransactionType.EXPENSE, "cash", ars, ars(30_000),
        schedule = RecurringSchedule(Recurrence.Monthly(28), LocalDate(2026, 10, 28)))

    private fun plan(budgets: List<Budget>, days: List<ReportDay>, goals: Money? = null, templates: List<TransactionTemplate> = emptyList()) =
        DailyPlanner.plan(budgets, cats, days, book, templates, goals, ars, 1, today)

    @Test
    fun `with limits it counts the limits against spending in limited categories`() {
        val days = listOf(spend(3, "food", 100_000), spend(26, "food", 2_000), spend(10, "cafe", 50_000))
        val p = plan(listOf(Budget("b", "food", YearMonth(2026, 11), ars(150_000))), days)!!
        assertEquals(DailyBase.LIMITS, p.base)
        assertEquals(ars(150_000), p.baseAmount)
        // Cafe has no limit: only food counts.
        assertEquals(ars(102_000), p.spent)
        // 26–30 November: 5 days; from the morning 50 000 left → 10 000 a day.
        assertEquals(5, p.allowance.days)
        assertEquals(ars(10_000), p.allowance.perDay)
        assertEquals(ars(2_000), p.allowance.spentToday)
    }

    @Test
    fun `without limits it counts the month's income against all spending`() {
        val days = listOf(ReportDay(LocalDate(2026, 11, 5), "salary", TransactionType.INCOME, ars(200_000)), spend(6, "food", 80_000), spend(7, "cafe", 20_000))
        val p = plan(emptyList(), days, goals = ars(25_000), templates = listOf(rent))!!
        assertEquals(DailyBase.INCOME, p.base)
        assertEquals(ars(200_000), p.baseAmount)
        assertEquals(ars(100_000), p.spent)
        assertEquals(ars(30_000), p.upcoming)
        assertEquals(ars(25_000), p.goals)
        assertEquals(ars(45_000), p.allowance.left)
        assertEquals(ars(9_000), p.allowance.perDay)
    }

    @Test
    fun `no limits and no income means no plan`() {
        assertNull(plan(emptyList(), listOf(spend(6, "food", 80_000))))
    }

    @Test
    fun `a regular payment already handled this month is not counted again`() {
        val paid = rent.copy(schedule = rent.schedule!!.copy(handledThrough = LocalDate(2026, 11, 28)))
        val days = listOf(ReportDay(LocalDate(2026, 11, 5), "salary", TransactionType.INCOME, ars(100_000)))
        assertEquals(ars(0), plan(emptyList(), days, templates = listOf(paid))!!.upcoming)
    }
}
