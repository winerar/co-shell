package app.coshell.rates.list

import app.coshell.core.designsystem.components.keypad.KeypadKey
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import org.junit.Assert.assertEquals
import org.junit.Test

class ConverterTest {
    private val usd = CurrencyCode("USD")
    private val jpy = CurrencyCode("JPY")

    @Test
    fun `keypad sum adds terms and reads an unfinished fraction`() {
        assertEquals(Money.ofMajor(1_512, currency = usd), keypadMoney("1500 + 12,", usd))
        assertEquals(Money.ofMajor(12, 50, currency = usd), keypadMoney("12,5", usd))
        assertEquals(Money.zero(usd), keypadMoney("", usd))
    }

    @Test
    fun `typing follows the currency's fraction digits`() {
        val input = ConverterInput("12", jpy)
        assertEquals("12", input.type(KeypadKey.Decimal, jpy).text)
        assertEquals("12,", ConverterInput("12").type(KeypadKey.Decimal, usd).text)
    }

    @Test
    fun `switching to a currency without kopecks drops the fraction`() {
        val switched = ConverterInput("12,5 + 3,25", usd).from(jpy)
        assertEquals("12 + 3", switched.text)
        assertEquals(jpy, switched.from)
    }
}
