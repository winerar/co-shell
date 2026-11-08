package app.coshell.core.model

import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyFormatTest {
    private val nbsp = MoneyFormat.NBSP
    private val g = MoneyFormat.NARROW_NBSP

    @Test
    fun `formats rubles in russian style`() {
        assertEquals("1${g}234,56$nbsp₽", MoneyFormat.RU.format(Money(123456, CurrencyCode("RUB"))))
        assertEquals("0,05$nbsp₽", MoneyFormat.RU.format(Money(5, CurrencyCode("RUB"))))
        assertEquals("-12,30$nbsp₽", MoneyFormat.RU.format(Money(-1230, CurrencyCode("RUB"))))
    }

    @Test
    fun `formats in english style with leading symbol`() {
        assertEquals("$1,234.56", MoneyFormat.EN.format(Money(123456, CurrencyCode("USD"))))
        assertEquals("-$0.99", MoneyFormat.EN.format(Money(-99, CurrencyCode("USD"))))
    }

    @Test
    fun `respects zero and three fraction digits`() {
        assertEquals("1${g}500$nbsp¥", MoneyFormat.RU.format(Money(1500, CurrencyCode("JPY"))))
        assertEquals("1,234${nbsp}KWD", MoneyFormat.RU.format(Money(1234, CurrencyCode("KWD"))))
    }

    @Test
    fun `can trim a zero fraction`() {
        val format = MoneyFormat.RU.copy(trimZeroFraction = true)
        assertEquals("1${g}200$nbsp₽", format.format(Money(120000, CurrencyCode("RUB"))))
        assertEquals("1${g}200,50$nbsp₽", format.format(Money(120050, CurrencyCode("RUB"))))
    }

    @Test
    fun `handles extreme values`() {
        val text = MoneyFormat.RU.formatNumber(Long.MIN_VALUE, 2)
        assertEquals('-', text.first())
    }
}
