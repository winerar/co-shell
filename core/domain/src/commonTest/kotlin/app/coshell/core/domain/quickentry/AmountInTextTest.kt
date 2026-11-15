package app.coshell.core.domain.quickentry

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AmountInTextTest {
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")
    private val amd = CurrencyCode("AMD")
    private val rub = CurrencyCode("RUB")
    private val usdt = CurrencyCode("USDT")

    private fun find(text: String, fallback: CurrencyCode = usd) = AmountInText.find(text, fallback)

    @Test
    fun `argentine bank push - peso sign, dot groups, comma decimals`() {
        val found = find("Compra con tarjeta \$12.500,00 en COTO SUC 115")!!
        assertEquals(Money.ofMajor(12_500, currency = ars), found.money)
        assertTrue(found.currencyFound)
        assertEquals("Compra con tarjeta en COTO SUC 115", found.note)
    }

    @Test
    fun `peso sign with grouping only`() {
        assertEquals(Money.ofMajor(12_500, currency = ars), find("Pagaste $ 12.500 en Mercado Libre")!!.money)
    }

    @Test
    fun `dollars written as US$ or U$S`() {
        assertEquals(Money.ofMajor(15, 50, usd), find("Consumo US$ 15,50 AMAZON", ars)!!.money)
        assertEquals(Money.ofMajor(1_200, currency = usd), find("Débito U\$S 1.200 Netflix", ars)!!.money)
    }

    @Test
    fun `code after the number in any case`() {
        assertEquals(Money.ofMajor(15_000, currency = amd), find("Payment 15,000.00 AMD at Yerevan City")!!.money)
        assertEquals(Money.ofMajor(13, 10, usdt), find("paid 13.10 usdt")!!.money)
    }

    @Test
    fun `code before the number`() {
        assertEquals(Money.ofMajor(15_000, currency = amd), find("AMD 15,000.00 Yerevan City")!!.money)
    }

    @Test
    fun `russian bank sms with rouble sign and spaces`() {
        val found = find("Покупка 1 234,56 ₽ Пятёрочка. Баланс: 10 000 ₽")!!
        assertEquals(Money.ofMajor(1_234, 56, rub), found.money)
        assertTrue(found.note.startsWith("Покупка Пятёрочка"))
    }

    @Test
    fun `card tail time and date are not the amount`() {
        val found = find("Tarjeta *4821 13/11 21:40 compra \$8.300 Farmacity")!!
        assertEquals(Money.ofMajor(8_300, currency = ars), found.money)
    }

    @Test
    fun `without a currency the account currency is used`() {
        val found = find("12 500", ars)!!
        assertEquals(Money.ofMajor(12_500, currency = ars), found.money)
        assertFalse(found.currencyFound)
        assertEquals("", found.note)
    }

    @Test
    fun `without a currency money-looking number wins over a plain one`() {
        assertEquals(Money.ofMajor(4_500, 50, usd), find("Order 7 total 4,500.50")!!.money)
    }

    @Test
    fun `unknown three letter word is not a currency`() {
        val found = find("COTO 500", ars)!!
        assertFalse(found.currencyFound)
        assertEquals(Money.ofMajor(500, currency = ars), found.money)
    }

    @Test
    fun `known custom code counts`() {
        val found = AmountInText.find("got 0.5 TON", usd, known = setOf(CurrencyCode("TON")))
        assertEquals(CurrencyCode("TON"), found!!.money.currency)
    }

    @Test
    fun `no number or zero gives nothing`() {
        assertNull(find("Привет, купи хлеба"))
        assertNull(find("Saldo 0,00"))
    }
}
