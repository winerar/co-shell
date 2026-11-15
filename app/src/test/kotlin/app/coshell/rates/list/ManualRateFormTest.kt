package app.coshell.rates.list

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.ExchangeRate
import app.coshell.core.model.Rate
import app.coshell.core.model.RateOrigin
import app.coshell.core.designsystem.components.keypad.AmountKeys
import app.coshell.core.designsystem.components.keypad.KeypadKey
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ManualRateFormTest {
    private val nov9 = LocalDate(2026, 11, 9)
    private fun form(from: String = "usd", to: String = "ARS", rate: String = "1 450,5") =
        ManualRateForm(from, to, rate, nov9, nov9)

    @Test
    fun `a small rate typed on the keypad keeps its fraction`() {
        val keys = listOf(KeypadKey.Digit('0'), KeypadKey.Decimal) + "000675".map { KeypadKey.Digit(it) }
        val text = keys.fold("") { t, k -> AmountKeys.apply(t, k, ManualRateForm.RATE_FRACTION_DIGITS) }
        assertEquals("0,000675", text)
        assertEquals(Rate.parse("0.000675"), form(rate = text).validate().second!!.rate)
    }

    @Test
    fun `valid form gives a manual rate with spaces and comma accepted`() {
        val (checked, rate) = form().validate()
        assertEquals(
            ExchangeRate(nov9, CurrencyCode("USD"), CurrencyCode("ARS"), Rate.parse("1450.5"), RateOrigin.MANUAL),
            rate,
        )
        assertNull(checked.fromError)
        assertFalse(checked.rateError)
    }

    @Test
    fun `bad currency, same currencies and zero rate are reported`() {
        assertEquals(ManualRateForm.CurrencyError.INVALID, form(from = "$").validate().first.fromError)
        assertEquals(ManualRateForm.CurrencyError.SAME, form(to = "USD").validate().first.toError)
        val (checked, rate) = form(rate = "0").validate()
        assertTrue(checked.rateError)
        assertNull(rate)
        assertTrue(form(rate = "abc").validate().first.rateError)
    }

    @Test
    fun `same slot means same day and pair in either direction`() {
        val a = ExchangeRate(nov9, CurrencyCode("USD"), CurrencyCode("ARS"), Rate.parse("1450"), RateOrigin.MANUAL)
        assertTrue(a.sameSlotAs(a.copy(from = a.to, to = a.from, rate = Rate.parse("0.0007"))))
        assertFalse(a.sameSlotAs(a.copy(date = LocalDate(2026, 11, 8))))
    }
}
