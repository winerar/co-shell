package app.coshell.reports

import app.coshell.core.domain.rates.RateBook
import app.coshell.core.domain.reports.ForeignPurchase
import app.coshell.core.domain.reports.conversionCost
import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/** «Оплата в другой валюте» (#786): the card's lines and rates. */
class ConversionBoardTest {
    private val usd = CurrencyCode("USD")
    private val usdt = CurrencyCode("USDT")
    private val ars = CurrencyCode("ARS")
    private val day = LocalDate(2026, 11, 5)
    private val format = MoneyFormat.RU.copy(trimZeroFraction = true)
    private val book = RateBook(
        listOf(
            ExchangeRate(day, usd, ars, Rate.parse("1015"), RateOrigin.DOWNLOADED),
            ExchangeRate(day, usd, usdt, Rate.parse("1"), RateOrigin.DOWNLOADED),
        ),
    )
    private val lemon = Account(id = "lemon", name = "Lemon", currency = usdt)

    private fun plain(text: String) = text.replace(' ', ' ').replace(' ', ' ')

    @Test
    fun `the card tells purchases, what was paid, the official price and the cost`() {
        // 101 500 ARS is 100 USDT officially; 101,80 USDT left the card.
        val cost = conversionCost(listOf(ForeignPurchase("a", day, "lemon", TransactionType.EXPENSE, Money(10_180, usdt), Money(10_150_000, ars))), book, usd)
        val card = buildConversion(cost, listOf(lemon), format)!!
        assertEquals("1,80 USD", plain(card.total))
        assertFalse(card.better)
        assertEquals("101 500 ARS", plain(card.pair.price))
        assertEquals("101,80 USDT", plain(card.pair.paid))
        assertEquals("100 USDT", plain(card.pair.official))
        assertEquals("1 USDT = 997,05 ARS", plain(card.pair.rate))
        assertEquals("1 015", plain(card.pair.officialRate))
        assertEquals("1,8", card.pair.percent)
        assertEquals("Lemon", card.purchases.single().account)
    }

    @Test
    fun `nothing paid in another currency gives no card`() {
        assertNull(buildConversion(null, emptyList(), format))
    }
}
