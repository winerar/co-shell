package app.coshell.transactions.edit

import app.coshell.converter.keypadMoney
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ParseAmountTest {
    private val ars = CurrencyCode("ARS")

    @Test
    fun `adds and takes away terms of the keypad sum`() {
        assertEquals(Money(137_500, ars), parsePositiveAmount("1250 + 125", ars).money)
        assertEquals(Money(112_500, ars), parsePositiveAmount("1250 − 125", ars).money)
        assertEquals(Money(122_500, ars), parsePositiveAmount("1250 − 125 + 100", ars).money)
    }

    @Test
    fun `a sum that is not above zero is rejected`() {
        val parsed = parsePositiveAmount("100 − 100", ars)
        assertNull(parsed.money)
        assertEquals(TransactionEditUiState.AmountError.NOT_POSITIVE, parsed.error)
        assertEquals(TransactionEditUiState.AmountError.NOT_POSITIVE, parsePositiveAmount("100 − 250", ars).error)
    }

    @Test
    fun `a single term and an unfinished sum still parse`() {
        assertEquals(Money(5_000, ars), parsePositiveAmount("50", ars).money)
        assertEquals(Money(5_000, ars), parsePositiveAmount("50 − ", ars).money)
        assertEquals(TransactionEditUiState.AmountError.REQUIRED, parsePositiveAmount("", ars).error)
    }

    @Test
    fun `the converter never goes below zero`() {
        assertEquals(Money(9_000, ars), keypadMoney("100 − 10", ars))
        assertEquals(Money.zero(ars), keypadMoney("10 − 100", ars))
    }
}
