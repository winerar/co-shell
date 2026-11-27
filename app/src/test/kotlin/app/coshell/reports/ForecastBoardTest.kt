package app.coshell.reports

import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.BalanceDay
import app.coshell.core.domain.reports.ReportDay
import app.coshell.core.domain.reports.ReportLedger
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.PostMode
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** «Прогноз остатка» (#770): the numbers of the card and when it says «не хватит». */
class ForecastBoardTest {
    private val usd = CurrencyCode("USD")
    private val today = LocalDate(2026, 11, 11)
    private val format = MoneyFormat.RU.copy(trimZeroFraction = true)
    private val categories = listOf(Category("cafe", "Кафе", CategoryType.EXPENSE))
    private val card = Account("card", "Card", usd)

    private fun history(vararg days: ReportDay) = ReportLedger(days.toList(), categories, RateBook.of(emptyList()), usd)

    private fun rent(units: Long) = TransactionTemplate(
        "rent", "Rent", TransactionType.EXPENSE, "card", usd, Money(units * 100, usd),
        schedule = RecurringSchedule(Recurrence.Monthly(20), LocalDate(2026, 1, 1), PostMode.REMIND, handledThrough = LocalDate(2026, 11, 10)),
    )

    private fun forecast(balanceUnits: Long, templates: List<TransactionTemplate>, vararg days: ReportDay) = buildForecast(
        today, listOf(card), listOf(BalanceDay(LocalDate(2026, 10, 1), "card", balanceUnits * 100)), templates,
        history(*days), days.isNotEmpty(), RateBook.of(emptyList()), format,
    )

    @Test
    fun `a template with a repeat alone gives a forecast with both horizons`() {
        val f = checkNotNull(forecast(1_000, listOf(rent(300))))
        assertEquals(listOf(31, 61), f.views.map { it.future.size })
        assertEquals(14, f.views.first().past.size)
        assertFalse(f.views.first().warning)
        // 20 November rent and 20 December rent: the 60-day line goes deeper than the 30-day one.
        assertEquals(1_000_00L - 300_00, f.views.first().future.last())
        assertEquals(1_000_00L - 600_00, f.views.last().future.last())
    }

    @Test
    fun `money that runs out says from which day and by how much`() {
        val f = checkNotNull(forecast(200, listOf(rent(300))))
        val view = f.views.first()
        assertTrue(view.warning)
        assertEquals(LocalDate(2026, 11, 20), view.belowFrom)
        assertEquals(format.format(Money(100_00, usd)), view.shortfall)
    }

    @Test
    fun `the rest of the money goes on at the pace of the history without what templates already did`() {
        // 90 days: 900 spent in all, three rents of 100 inside it → the other 600 over 90 days is ~6.66 a day.
        val spent = ReportDay(LocalDate(2026, 10, 1), "cafe", TransactionType.EXPENSE, Money(900_00, usd))
        val f = checkNotNull(forecast(1_000, listOf(rent(100)), spent))
        val line = f.views.first().future
        // Day 1 without the rent: −6,66 → 666 minor units a day.
        assertEquals(1_000_00L - 666, line[1])
    }

    @Test
    fun `nothing to forecast from gives no card`() {
        assertNull(forecast(1_000, emptyList()))
    }

    @Test
    fun `an account without a rate gives no card`() {
        val book = RateBook.of(emptyList())
        val ars = Account("ars", "Peso", CurrencyCode("ARS"))
        val none = buildForecast(
            today, listOf(card, ars), listOf(BalanceDay(LocalDate(2026, 10, 1), "ars", 100_00)), listOf(rent(300)),
            history(), false, book, format,
        )
        assertNull(none)
    }
}
