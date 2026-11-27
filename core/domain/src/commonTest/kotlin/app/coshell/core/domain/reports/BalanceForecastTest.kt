package app.coshell.core.domain.reports

import app.coshell.core.domain.rates.CurrencyConverter
import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.PostMode
import app.coshell.core.model.Recurrence
import app.coshell.core.model.RecurringSchedule
import app.coshell.core.model.TransactionTemplate
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BalanceForecastTest {
    private val usd = CurrencyCode("USD")
    private val today = LocalDate(2026, 11, 10)
    private val converter = CurrencyConverter(RateBook.of(emptyList()))

    private fun template(type: TransactionType, minor: Long, day: Int, start: LocalDate = LocalDate(2026, 1, 1), handled: LocalDate? = LocalDate(2026, 11, 9)) = TransactionTemplate(
        id = "t$day$type", name = "t", type = type, accountId = "a", currency = usd, amount = Money(minor, usd),
        toAccountId = if (type == TransactionType.TRANSFER) "b" else null,
        schedule = RecurringSchedule(Recurrence.Monthly(day), start, PostMode.REMIND, handledThrough = handled),
    )

    @Test
    fun `scheduled payments and incomes fall on their days, transfers are not a flow`() {
        val rent = template(TransactionType.EXPENSE, 500_00, 15)
        val salary = template(TransactionType.INCOME, 2_000_00, 1)
        val move = template(TransactionType.TRANSFER, 100_00, 20)
        val (flows, missing) = BalanceForecasts.scheduledFlows(listOf(rent, salary, move), converter, usd, today, today, LocalDate(2026, 12, 31))
        assertTrue(missing.isEmpty())
        assertEquals(listOf(LocalDate(2026, 11, 15), LocalDate(2026, 12, 15)), flows.filter { it.amountMinor < 0 }.map { it.day })
        assertEquals(listOf(-500_00L, -500_00L), flows.filter { it.amountMinor < 0 }.map { it.amountMinor })
        assertEquals(listOf(LocalDate(2026, 12, 1)), flows.filter { it.amountMinor > 0 }.map { it.day })
    }

    @Test
    fun `forecast adds the flows and the daily drift and finds the lowest day and the first day under zero`() {
        val flows = listOf(ScheduledFlow(LocalDate(2026, 11, 12), -300_00))
        val f = BalanceForecasts.of(usd, 100_00, today, 5, flows, variableDaily = -10_00)
        assertEquals(6, f.points.size)
        assertEquals(100_00L, f.points.first().balanceMinor)
        assertEquals(100_00L - 10_00 - 10_00 - 300_00 - 10_00, f.points[3].balanceMinor)
        assertEquals(LocalDate(2026, 11, 12), f.firstBelowZero)
        assertEquals(f.points.last(), f.lowest)
        assertEquals(-f.lowest.balanceMinor, f.deepestShortfallMinor)
    }

    @Test
    fun `a payment overdue and not handled yet is expected right away`() {
        val rent = template(TransactionType.EXPENSE, 500_00, 5, handled = LocalDate(2026, 10, 5))
        val (flows, _) = BalanceForecasts.scheduledFlows(listOf(rent), converter, usd, today, today, LocalDate(2026, 11, 20))
        assertEquals(listOf(today), flows.map { it.day })
    }

    @Test
    fun `money that lasts has no day under zero`() {
        val f = BalanceForecasts.of(usd, 1_000_00, today, 30, emptyList(), variableDaily = -1_00)
        assertNull(f.firstBelowZero)
        assertEquals(0L, f.deepestShortfallMinor)
    }

    @Test
    fun `the drift is the history net without what the templates already did, per day`() {
        val rent = template(TransactionType.EXPENSE, 500_00, 15)
        val past = BalanceForecasts.pastScheduledNet(listOf(rent), converter, usd, today, LocalDate(2026, 8, 12), LocalDate(2026, 11, 9))
        assertEquals(-1_500_00L, past)
        // History: −1 500 of rent and −900 of other spending in 90 days → −10 a day.
        assertEquals(-10_00L, BalanceForecasts.variableDaily(-2_400_00, past))
    }
}
