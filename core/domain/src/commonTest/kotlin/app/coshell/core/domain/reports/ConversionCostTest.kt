package app.coshell.core.domain.reports

import app.coshell.core.domain.rates.RateBook
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Money
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.core.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ConversionCostTest {
    private val usd = CurrencyCode("USD")
    private val usdt = CurrencyCode("USDT")
    private val ars = CurrencyCode("ARS")
    private val day = LocalDate(2026, 11, 5)
    private val book = RateBook(
        listOf(
            ExchangeRate(day, usd, ars, Rate.parse("1000"), RateOrigin.DOWNLOADED),
            ExchangeRate(day, usd, usdt, Rate.parse("1"), RateOrigin.DOWNLOADED),
        ),
    )

    private fun purchase(id: String, paidUnits: Long, priceUnits: Long, account: String = "lemon", type: TransactionType = TransactionType.EXPENSE, paidIn: CurrencyCode = usdt) =
        ForeignPurchase(id, day, account, type, Money(paidUnits * 100, paidIn), Money(priceUnits * 100, ars))

    @Test
    fun `paying more than the official rate costs the difference in the report currency`() {
        // 100 000 ARS is 100 USDT at the official rate; 102 USDT left the card: 2 USD more, «хуже на 2 %».
        val cost = conversionCost(listOf(purchase("a", 102, 100_000)), book, usd)!!
        assertEquals(Money(200, usd), cost.total)
        val pair = cost.pairs.single()
        assertEquals(ars, pair.priceCurrency)
        assertEquals(usdt, pair.paidCurrency)
        assertEquals(Money(10_000, usdt), pair.cost.official)
        assertEquals(200L, pair.cost.extraBp)
    }

    @Test
    fun `a better rate is a negative cost and a refund counts back`() {
        val cost = conversionCost(
            listOf(purchase("a", 98, 100_000), purchase("b", 51, 50_000, type = TransactionType.INCOME)),
            book,
            usd,
        )!!
        // −2 USD for the first, the refund gave back 1 USD more than official: −2 − 1 = −3.
        assertEquals(Money(-300, usd), cost.total)
    }

    @Test
    fun `accounts are ordered worst first and purchases newest first`() {
        val cost = conversionCost(listOf(purchase("a", 101, 100_000, "lemon"), purchase("b", 104, 100_000, "galicia")), book, usd)!!
        assertEquals(listOf("galicia", "lemon"), cost.accounts.map { it.accountId })
        assertEquals(listOf(400L, 100L), cost.accounts.map { it.cost.extraBp })
    }

    @Test
    fun `a purchase without a rate is left out and named, nothing counted is null`() {
        val amd = CurrencyCode("AMD")
        val noRate = purchase("a", 100, 40_000, paidIn = amd)
        assertNull(conversionCost(listOf(noRate), book, usd))
        val cost = conversionCost(listOf(noRate, purchase("b", 101, 100_000)), book, usd)!!
        assertEquals(setOf(amd), cost.missing)
        assertEquals(listOf("b"), cost.purchases.map { it.purchase.id })
    }
}
