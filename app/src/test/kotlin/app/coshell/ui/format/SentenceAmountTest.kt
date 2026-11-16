package app.coshell.ui.format

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SentenceAmountTest {
    @Test fun `currency code can wrap but digit groups stay glued`() {
        val text = "из " + MoneyFormat.RU.format(Money(123456789, CurrencyCode("USD")))
        val result = text.breakableBeforeCurrency()
        assertFalse(result.contains(MoneyFormat.NBSP))
        assertEquals("из 1${MoneyFormat.NARROW_NBSP}234${MoneyFormat.NARROW_NBSP}567,89 USD", result)
    }
}
