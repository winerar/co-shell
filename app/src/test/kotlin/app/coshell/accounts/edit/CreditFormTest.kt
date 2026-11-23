package app.coshell.accounts.edit

import app.coshell.core.model.CreditTerms
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.MinPayment
import app.coshell.core.model.MoneyFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** The conditions of a credit card as typed in «Условия карты» (#686). */
class CreditFormTest {
    private val amd = CurrencyCode("AMD")
    private fun form(limit: String = "500 000", day: String = "15", sum: Boolean = false, min: String = "5") =
        AccountEditUiState(credit = true, creditLimit = limit, creditPayDay = day, creditMinSum = sum, creditMin = min)

    @Test
    fun `a percent and a sum both become terms`() {
        assertEquals(CreditTerms(500_000_00, 15, MinPayment.Percent(500)), parseCreditTerms(form(), amd))
        assertEquals(CreditTerms(500_000_00, 15, MinPayment.Fixed(9_000_00)), parseCreditTerms(form(sum = true, min = "9 000"), amd))
        assertEquals(CreditTerms(500_000_00, 31, MinPayment.Percent(550)), parseCreditTerms(form(day = "31", min = "5,5"), amd))
    }

    @Test
    fun `missing or wrong conditions give nothing`() {
        assertNull(parseCreditTerms(form(limit = ""), amd))
        assertNull(parseCreditTerms(form(limit = "0"), amd))
        assertNull(parseCreditTerms(form(day = ""), amd))
        assertNull(parseCreditTerms(form(day = "0"), amd))
        assertNull(parseCreditTerms(form(day = "32"), amd))
        assertNull(parseCreditTerms(form(min = ""), amd))
        assertNull(parseCreditTerms(form(min = "0"), amd))
        assertNull(parseCreditTerms(form(min = "101"), amd))
        assertNull(parseCreditTerms(form(min = "abc"), amd))
        assertNull(parseCreditTerms(form(sum = true, min = ""), amd))
    }

    @Test
    fun `percents go back and forth through the field text`() {
        assertEquals(500, parsePercentBasisPoints("5"))
        assertEquals(525, parsePercentBasisPoints("5.25"))
        assertEquals(10_000, parsePercentBasisPoints("100"))
        assertNull(parsePercentBasisPoints("5,255"))
        assertEquals("5", percentText(500))
        assertEquals("5,5", percentText(550))
        assertEquals("5,25", percentText(525))
        assertEquals("0,5", percentText(50))
    }

    @Test
    fun `saved terms fill the form`() {
        val s = AccountEditUiState().withCreditTerms(CreditTerms(500_000_00, 15, MinPayment.Fixed(9_000_00)), amd, MoneyFormat.RU)
        assertEquals(true, s.credit)
        assertEquals(true, s.creditMinSum)
        assertEquals("15", s.creditPayDay)
        assertEquals(CreditTerms(500_000_00, 15, MinPayment.Fixed(9_000_00)), parseCreditTerms(s, amd))
    }
}
