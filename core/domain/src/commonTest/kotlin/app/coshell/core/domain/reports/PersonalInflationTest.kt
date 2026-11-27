package app.coshell.core.domain.reports

import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PersonalInflationTest {
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")
    private val categories = listOf(Category("food", "Продукты", CategoryType.EXPENSE), Category("cafe", "Кафе", CategoryType.EXPENSE))
    private val months = (6..11).map { budgetPeriod(YearMonth(2026, it)) }

    /** The peso gets 10 % cheaper every month: 1000, 1100, 1210 … per dollar. */
    private val book = RateBook(
        (6..11).runningFold(1000L) { rate, _ -> rate * 11 / 10 }.take(6).mapIndexed { i, rate ->
            ExchangeRate(LocalDate(2026, 6 + i, 1), usd, ars, Rate.parse(rate.toString()), RateOrigin.DOWNLOADED)
        },
    )

    private fun ars(units: Long) = Money(units * 100, ars)

    private fun inflation(days: List<ReportDay>, monthsCounted: List<app.coshell.core.domain.budgets.BudgetPeriod> = months) = personalInflation(
        ReportLedger(days, categories, book, ars),
        ReportLedger(days, categories, book, usd),
        categories,
        monthsCounted,
    )

    @Test
    fun `spending that grows with the rate is inflation in pesos and flat in dollars`() {
        // 100 USD of food every month, paid in pesos at the rate of the day.
        val rates = listOf(1000L, 1100L, 1210L, 1331L, 1464L, 1610L)
        val days = rates.mapIndexed { i, rate -> day(6 + i, 5, "food", ars(100 * rate)) }
        val result = inflation(days)!!
        assertEquals(ars, result.local)
        assertEquals(usd, result.hard)
        assertEquals(0, result.hardPercent)
        // Average of Sep–Nov against Jun–Aug: (1331+1464+1610) / (1000+1100+1210) − 1 ≈ +33 %.
        assertEquals(33, result.localPercent)
        assertEquals(6, result.localIndex.size)
        assertEquals(result.hardIndex.first(), result.hardIndex.last())
    }

    @Test
    fun `the folders that grew most in dollars come first, small ones are left out`() {
        val rates = listOf(1000L, 1100L, 1210L, 1331L, 1464L, 1610L)
        val days = rates.flatMapIndexed { i, rate ->
            val cafeUsd = if (i < 3) 50L else 75L
            listOf(day(6 + i, 5, "food", ars(100 * rate)), day(6 + i, 6, "cafe", ars(cafeUsd * rate)))
        }
        assertEquals(listOf("cafe" to 50, "food" to 0), inflation(days)!!.fastest.map { it.categoryId to it.percent })
    }

    @Test
    fun `fewer than four months or nothing spent at first gives nothing`() {
        val days = (6..11).map { day(it, 5, "food", ars(100_000)) }
        assertNull(inflation(days, months.take(3)))
        assertNull(inflation(days.drop(3)))
    }

    @Test
    fun `the family pays in the currency with the most spending`() {
        assertEquals(ars, spendingCurrency(listOf(day(6, 1, "food", ars(10)), day(6, 2, "food", Money(5_000, usd)), day(6, 3, "food", ars(10_000)))))
        assertNull(spendingCurrency(emptyList()))
    }
}
