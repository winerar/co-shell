package app.coshell.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyFormatTest {
    private val nbsp = MoneyFormat.NBSP
    private val g = MoneyFormat.NARROW_NBSP

    @Test
    fun `formats rubles in russian style`() {
        assertEquals("1${g}234,56${nbsp}RUB", MoneyFormat.RU.format(Money(123456, CurrencyCode("RUB"))))
        assertEquals("0,05${nbsp}RUB", MoneyFormat.RU.format(Money(5, CurrencyCode("RUB"))))
        assertEquals("-12,30${nbsp}RUB", MoneyFormat.RU.format(Money(-1230, CurrencyCode("RUB"))))
    }

    @Test
    fun `formats in english style with the code after the number`() {
        assertEquals("1,234.56${nbsp}USD", MoneyFormat.EN.format(Money(123456, CurrencyCode("USD"))))
        assertEquals("-0.99${nbsp}USD", MoneyFormat.EN.format(Money(-99, CurrencyCode("USD"))))
    }

    @Test
    fun `respects zero and three fraction digits`() {
        assertEquals("1${g}500${nbsp}JPY", MoneyFormat.RU.format(Money(1500, CurrencyCode("JPY"))))
        assertEquals("1,234${nbsp}KWD", MoneyFormat.RU.format(Money(1234, CurrencyCode("KWD"))))
    }

    @Test
    fun `always shows the letter code, even for currencies with a well-known symbol`() {
        assertEquals("1,00${nbsp}USD", MoneyFormat.RU.format(Money(100, CurrencyCode("USD"))))
        assertEquals("1,00${nbsp}ARS", MoneyFormat.RU.format(Money(100, CurrencyCode("ARS"))))
        assertEquals("1,00${nbsp}AMD", MoneyFormat.RU.format(Money(100, CurrencyCode("AMD"))))
        assertEquals("1,00${nbsp}USDT", MoneyFormat.RU.format(Money(100, CurrencyCode("USDT"))))
    }

    @Test
    fun `can trim a zero fraction`() {
        val format = MoneyFormat.RU.copy(trimZeroFraction = true)
        assertEquals("1${g}200${nbsp}RUB", format.format(Money(120000, CurrencyCode("RUB"))))
        assertEquals("1${g}200,50${nbsp}RUB", format.format(Money(120050, CurrencyCode("RUB"))))
    }

    @Test
    fun `handles extreme values`() {
        val text = MoneyFormat.RU.formatNumber(Long.MIN_VALUE, 2)
        assertEquals('-', text.first())
    }

    @Test
    fun `editing format parses back to the same amount`() {
        val usd = CurrencyCode("USD")
        for (format in listOf(MoneyFormat.RU, MoneyFormat.EN)) {
            for (minor in listOf(1L, 1_250_50L, 123_456_789_00L, 100L)) {
                val money = Money(minor, usd)
                assertEquals(money, MoneyParser.parseOrNull(format.formatForEditing(money), usd))
            }
        }
        assertEquals("1${g}250,50", MoneyFormat.RU.formatForEditing(Money(1_250_50, usd)))
    }
}
