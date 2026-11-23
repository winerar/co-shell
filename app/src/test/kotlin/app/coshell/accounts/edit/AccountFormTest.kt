package app.coshell.accounts.edit

import app.coshell.accounts.edit.AccountEditUiState.BalanceError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountFormTest {

    private fun form(name: String = "Cash", currency: String = "USD", balance: String = "") =
        AccountEditUiState(name = name, currency = currency, initialBalance = balance)

    @Test
    fun `a valid form makes a new account with a fresh id`() {
        val result = buildAccount(form(balance = "12,50"), existing = null) { "new-id" }
        val account = (result as AccountFormResult.Valid).account
        assertEquals("new-id", account.id)
        assertEquals(1250L, account.initialBalanceMinor)
    }

    @Test
    fun `a blank name and a wrong currency are both reported`() {
        val result = (buildAccount(form(name = " ", currency = "??"), null) { "x" } as AccountFormResult.Invalid)
        val shown = result.showErrors(form())
        assertTrue(shown.nameError)
        assertEquals(AccountEditUiState.CurrencyError.INVALID, shown.currencyError)
    }

    @Test
    fun `too many fraction digits is reported on the balance`() {
        val result = (buildAccount(form(balance = "1,23456"), null) { "x" } as AccountFormResult.Invalid)
        assertEquals(BalanceError.TOO_MANY_FRACTION_DIGITS, result.showErrors(form()).balanceError)
    }

    @Test
    fun `card digits of the wrong length are an error`() {
        val result = (buildAccount(form().copy(cardDigits = "12"), null) { "x" } as AccountFormResult.Invalid)
        assertTrue(result.showErrors(form()).cardDigitsError)
    }
}
