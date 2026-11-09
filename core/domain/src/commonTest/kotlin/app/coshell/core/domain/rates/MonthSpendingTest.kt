package app.coshell.core.domain.rates

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class MonthSpendingTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val rub = CurrencyCode("RUB")
    private val tz = TimeZone.of("America/Argentina/Buenos_Aires")

    private fun day(d: Int) = LocalDate(2026, 11, d)
    private fun money(major: Long, currency: CurrencyCode) = Money.ofMajor(major, currency = currency)
    private fun at(d: Int, hour: Int = 12) = LocalDateTime(2026, 11, d, hour, 0).toInstant(tz)

    private val book = RateBook(
        listOf(
            ExchangeRate(day(1), usd, ars, Rate.parse("1000"), RateOrigin.DOWNLOADED),
            ExchangeRate(day(5), usd, ars, Rate.parse("2000"), RateOrigin.DOWNLOADED),
        ),
    )

    private fun expense(id: String, amount: Money, d: Int, receipt: Money? = null) =
        Transaction(id, TransactionType.EXPENSE, "acc", amount, at(d), originalAmount = receipt)

    @Test
    fun `month bounds follow the local calendar`() {
        val (start, end) = monthBounds(day(9), tz)

        assertEquals(LocalDateTime(2026, 11, 1, 0, 0).toInstant(tz), start)
        assertEquals(LocalDateTime(2026, 12, 1, 0, 0).toInstant(tz), end)
    }

    @Test
    fun `december bounds roll over to the next year`() {
        val (_, end) = monthBounds(LocalDate(2026, 12, 31), tz)

        assertEquals(LocalDateTime(2027, 1, 1, 0, 0).toInstant(tz), end)
    }

    @Test
    fun `each expense uses the rate of its own day`() {
        val items = listOf(expense("1", money(10_000, ars), 2), expense("2", money(10_000, ars), 6))

        val (inUsd, inArs) = book.expenseTotals(items, listOf(usd, ars), tz)

        assertEquals(money(15, usd), inUsd.sum) // 10 000 / 1000 + 10 000 / 2000
        assertEquals(money(20_000, ars), inArs.sum)
        assertEquals(emptySet(), inUsd.missing)
    }

    @Test
    fun `incomes and transfers are not spending`() {
        val items = listOf(
            expense("1", money(5, usd), 2),
            Transaction("2", TransactionType.INCOME, "acc", money(100, usd), at(3)),
            Transaction("3", TransactionType.TRANSFER, "acc", money(50, usd), at(4), toAccountId = "b", toAmount = money(50, usd)),
        )

        val (inUsd) = book.expenseTotals(items, listOf(usd), tz)

        assertEquals(money(5, usd), inUsd.sum)
    }

    @Test
    fun `counts the debited amount, not the receipt`() {
        val items = listOf(expense("1", money(10, usd), 6, receipt = money(15_000, ars)))

        val (inArs) = book.expenseTotals(items, listOf(ars), tz)

        assertEquals(money(20_000, ars), inArs.sum) // 10 USD at 2000, not the 15 000 on the receipt
    }

    @Test
    fun `expenses without a rate are left out and named`() {
        val items = listOf(expense("1", money(5, usd), 2), expense("2", money(1_000, rub), 3))

        val (inUsd) = book.expenseTotals(items, listOf(usd), tz)

        assertEquals(money(5, usd), inUsd.sum)
        assertEquals(setOf(rub), inUsd.missing)
    }
}
