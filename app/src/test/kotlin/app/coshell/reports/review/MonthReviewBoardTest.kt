package app.coshell.reports.review

import app.coshell.budgets.BudgetSource
import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.model.Budget
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.TransactionType
import app.coshell.core.model.budgetId
import app.coshell.home.budget.buildHomeReview
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** «Итог октября» shown on 2 November 2026: September is the usual month, October is reviewed. */
class MonthReviewBoardTest {
    private val usd = CurrencyCode("USD")
    private val october = YearMonth(2026, 10)
    private val today = LocalDate(2026, 11, 2)
    private val format = MoneyFormat.RU.copy(trimZeroFraction = true)

    private val categories = listOf(
        Category("gifts", "Подарки", CategoryType.EXPENSE),
        Category("cafe", "Кафе", CategoryType.EXPENSE),
        Category("home", "Дом", CategoryType.EXPENSE),
        Category("salary", "Зарплата", CategoryType.INCOME),
    )

    private fun usd(major: Long) = Money.ofMajor(major, currency = usd)

    private fun number(major: Long) = format.formatNumber(usd(major).amountMinor, 2)

    private fun limit(categoryId: String, major: Long) = Budget(budgetId(categoryId, october), categoryId, october, usd(major))

    private fun spent(month: Int, categoryId: String, major: Long) =
        ReportDay(LocalDate(2026, month, 5), categoryId, TransactionType.EXPENSE, usd(major))

    private fun earned(month: Int, major: Long) = ReportDay(LocalDate(2026, month, 1), "salary", TransactionType.INCOME, usd(major))

    private val history = listOf(spent(9, "gifts", 30), spent(9, "cafe", 110), spent(9, "home", 365), earned(9, 2_000))
    private val octoberDays = listOf(spent(10, "gifts", 120), spent(10, "cafe", 180), spent(10, "home", 290), earned(10, 2_100))

    private fun snapshot(days: List<ReportDay>, budgets: List<Budget> = emptyList()) =
        BudgetSource.Snapshot(budgets, categories, days, rates = emptyList(), mainCurrency = usd, startDay = 1)

    @Test
    fun `left, savings rate and the changes against the usual month, the largest first`() {
        val review = buildMonthReview(snapshot(history + octoberDays), october, today, format)

        assertEquals(number(1_510), review.saved)
        assertFalse(review.overspent)
        assertEquals(72, review.savingsRate)
        assertEquals(number(590), review.spent)
        assertEquals(number(2_100), review.earned)
        assertEquals(1, review.usualMonths)
        assertEquals(listOf("gifts", "home", "cafe"), review.changes.map { it.categoryId })
        val gifts = review.changes[0]
        assertEquals("+90", gifts.difference)
        assertEquals(300, gifts.percent)
        assertTrue(gifts.more)
        assertEquals("-75", review.changes[1].difference)
        assertFalse(review.changes[1].more)
        assertNull(review.limits)
    }

    @Test
    fun `limits - kept and overspent, the overspent first`() {
        val review = buildMonthReview(snapshot(history + octoberDays, listOf(limit("gifts", 50), limit("cafe", 200), limit("home", 280))), october, today, format)
        val limits = review.limits!!

        assertEquals(3, limits.total)
        assertEquals(1, limits.within)
        // Gifts went 140% over, home 4%: the larger overspending first.
        assertEquals(listOf("gifts", "home"), limits.over.map { it.categoryId })
        assertEquals(listOf("gifts", "home", "cafe"), limits.all.map { it.categoryId })
    }

    @Test
    fun `no income - no rate, spent more than came in`() {
        val review = buildMonthReview(snapshot(listOf(spent(10, "cafe", 1_350))), october, today, format)

        assertEquals(number(-1_350), review.saved)
        assertTrue(review.overspent)
        assertNull(review.savingsRate)
        assertEquals(0, review.usualMonths)
        assertTrue(review.changes.isEmpty())
    }

    @Test
    fun `the home card - in the first days of the month, gone once hidden or when the month was empty`() {
        val data = snapshot(history + octoberDays)

        assertEquals(october, buildHomeReview(data, dismissed = null, today, format)?.month)
        assertNull(buildHomeReview(data, dismissed = october, today, format))
        assertNull(buildHomeReview(data, dismissed = null, LocalDate(2026, 11, 8), format))
        assertNull(buildHomeReview(snapshot(history), dismissed = null, today, format))
    }
}
