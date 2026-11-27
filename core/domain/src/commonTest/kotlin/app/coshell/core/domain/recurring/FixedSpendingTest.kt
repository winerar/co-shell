package app.coshell.core.domain.recurring

import app.coshell.core.domain.budgets.budgetPeriod
import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.MonthTotals
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.PostMode
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FixedSpendingTest {
    private val today = LocalDate(2026, 11, 15)
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val eur = CurrencyCode("EUR")
    private val converter = CurrencyConverter(RateBook.of(listOf(ExchangeRate(today, usd, ars, Rate.parse("1000"), RateOrigin.DOWNLOADED))))

    private fun usd(units: Long) = Money(units * 100, usd)

    private fun rule(name: String, recurrence: Recurrence?, amount: Money?, type: TransactionType = TransactionType.EXPENSE) = TransactionTemplate(
        id = name,
        name = name,
        type = type,
        accountId = "a",
        currency = amount?.currency ?: usd,
        amount = amount,
        toAccountId = if (type == TransactionType.TRANSFER) "b" else null,
        schedule = recurrence?.let { RecurringSchedule(it, LocalDate(2026, 1, 1), PostMode.REMIND) },
    )

    private fun month(m: Int, spent: Money) = MonthTotals(budgetPeriod(YearMonth(2026, m)), spent, Money.zero(spent.currency))

    @Test
    fun `a month counts twelve times, a week fifty two, a year once`() {
        assertEquals(12, Recurrence.Monthly(1).timesAYear)
        assertEquals(52, Recurrence.Weekly(DayOfWeek.MONDAY).timesAYear)
        assertEquals(1, Recurrence.Yearly(3, 12).timesAYear)
        val fixed = fixedSpending(
            listOf(
                rule("Кино", Recurrence.Monthly(3), usd(12)),
                rule("Уборка", Recurrence.Weekly(DayOfWeek.FRIDAY), usd(10)),
                rule("Страховка", Recurrence.Yearly(3, 12), usd(360)),
            ),
            converter, usd, today,
        )!!
        assertEquals(listOf("Уборка", "Страховка", "Кино"), fixed.items.map { it.template.name })
        assertEquals(usd(520 + 360 + 144), fixed.yearly)
        assertEquals(Money(8_533, usd), fixed.monthly) // 1 024 / 12 = 85.33
        assertEquals(usd(12), fixed.items.last().monthly)
    }

    @Test
    fun `only expenses with a repeat and an amount count`() {
        val fixed = fixedSpending(
            listOf(
                rule("Зарплата", Recurrence.Monthly(1), usd(1000), TransactionType.INCOME),
                rule("Копилка", Recurrence.Monthly(1), usd(50), TransactionType.TRANSFER),
                rule("Свет", Recurrence.Monthly(15), null),
                rule("Кофе", null, usd(3)),
                rule("Интернет", Recurrence.Monthly(10), usd(25)),
            ),
            converter, usd, today,
        )!!
        assertEquals(listOf("Интернет"), fixed.items.map { it.template.name })
        assertNull(fixedSpending(listOf(rule("Кофе", null, usd(3))), converter, usd, today))
    }

    @Test
    fun `other currencies come in at today's rate and those without one are named`() {
        val fixed = fixedSpending(
            listOf(rule("Аренда", Recurrence.Monthly(1), Money(65_000_000, ars)), rule("Музыка", Recurrence.Monthly(7), Money(600, eur))),
            converter, usd, today,
        )!!
        assertEquals(usd(7_800), fixed.yearly) // 650 000 ARS × 12 / 1 000
        assertEquals(setOf(eur), fixed.missing)
        assertEquals("Музыка", fixed.items.last().template.name)
        assertNull(fixed.items.last().yearlyInTotal)
    }

    @Test
    fun `share is against the last full months with spending`() {
        val months = listOf(month(7, usd(5_000)), month(8, usd(1_000)), month(9, usd(2_000)), month(10, usd(0)), month(11, usd(9_000)))
        // July–September: average 2 667; October had nothing, November is still running.
        assertEquals(22, sharePercent(usd(600), months, today))
        assertEquals(40, sharePercent(usd(600), months.drop(1), today))
        assertEquals(100, sharePercent(usd(5_000), months, today))
        assertNull(sharePercent(usd(600), listOf(month(11, usd(900))), today))
        assertNull(sharePercent(usd(600), emptyList(), today))
    }
}
