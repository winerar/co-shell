package app.coshell.core.domain.search

import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class FilterTotalTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val amd = CurrencyCode("AMD")
    private val tz = TimeZone.of("America/Argentina/Buenos_Aires")

    private fun at(d: Int) = LocalDateTime(2026, 11, d, 12, 0).toInstant(tz)
    private fun money(major: Long, currency: CurrencyCode) = Money.ofMajor(major, currency = currency)
    private fun op(id: String, type: TransactionType, amount: Money, d: Int) =
        Transaction(id, type, "acc", amount, at(d), toAccountId = if (type == TransactionType.TRANSFER) "other" else null,
            toAmount = if (type == TransactionType.TRANSFER) amount else null)

    private val book = RateBook(
        listOf(
            ExchangeRate(LocalDate(2026, 11, 1), usd, ars, Rate.parse("1000"), RateOrigin.DOWNLOADED),
            ExchangeRate(LocalDate(2026, 11, 5), usd, ars, Rate.parse("2000"), RateOrigin.DOWNLOADED),
        ),
    )

    @Test
    fun `nets per currency, transfers left out, each day at its own rate`() {
        val total = book.filterTotal(
            listOf(
                op("1", TransactionType.EXPENSE, money(10_000, ars), 2),
                op("2", TransactionType.EXPENSE, money(20_000, ars), 6),
                op("3", TransactionType.INCOME, money(5, usd), 6),
                op("4", TransactionType.TRANSFER, money(100, usd), 6),
            ),
            main = usd,
            timeZone = tz,
        )

        assertEquals(4, total.count)
        assertEquals(listOf(money(-30_000, ars), money(5, usd)), total.nets)
        // −10 000 / 1000 − 20 000 / 2000 + 5 = −15 USD.
        assertEquals(money(-15, usd), total.converted?.sum)
        assertEquals(emptySet(), total.converted?.missing)
    }

    @Test
    fun `a currency without a rate is listed, zero nets are hidden`() {
        val total = book.filterTotal(
            listOf(
                op("1", TransactionType.EXPENSE, money(500, amd), 2),
                op("2", TransactionType.EXPENSE, money(1_000, ars), 2),
                op("3", TransactionType.INCOME, money(1_000, ars), 3),
            ),
            main = usd,
            timeZone = tz,
        )

        assertEquals(listOf(money(-500, amd)), total.nets)
        assertEquals(setOf(amd), total.converted?.missing)
    }

    @Test
    fun `only transfers or no report currency convert nothing`() {
        assertNull(book.filterTotal(listOf(op("1", TransactionType.TRANSFER, money(1, usd), 2)), usd, tz).converted)
        assertNull(book.filterTotal(listOf(op("1", TransactionType.EXPENSE, money(1, usd), 2)), null, tz).converted)
    }
}
