package app.coshell.transactions.edit

import app.coshell.core.model.Account
import app.coshell.core.model.CreditTerms
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.MinPayment
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** «Провести» a credit card payment from «Скоро» (#686): a transfer into the card for the minimum. */
class TransactionEditCreditTest : TransactionEditTestBase() {
    private val amd = CurrencyCode("AMD")

    private suspend fun seedCard() {
        seed()
        accounts.saveAccount(Account("wage", "Зарплатная", amd, sortOrder = 2))
        // 180 000 of debt: the balance is negative.
        accounts.saveAccount(
            Account("credit", "Кредитка", amd, sortOrder = 3, initialBalanceMinor = -180_000_00, credit = CreditTerms(500_000_00, 15, MinPayment.Percent(500))),
        )
    }

    @Test
    fun `the payment opens a transfer into the card for the minimum from an account of the same currency`() = runTest {
        seedCard()
        val state = viewModel(type = "TRANSFER", creditCardId = "credit").state.value

        assertEquals(TransactionType.TRANSFER, state.type)
        assertEquals("credit", state.toAccountId)
        assertEquals("wage", state.accountId)
        assertEquals(MoneyFormat.RU.formatForEditing(Money(9_000_00, amd)), state.amount)
        assertEquals("", state.toAmount)
    }

    @Test
    fun `a card is never its own source and a foreign source gets the minimum as the arriving sum`() = runTest {
        seedCard()
        accounts.saveAccount(Account("wage", "Зарплатная", CurrencyCode("USD"), sortOrder = 2))
        val state = viewModel(type = "TRANSFER", creditCardId = "credit").state.value

        assertTrue(state.accountId != "credit")
        assertEquals("credit", state.toAccountId)
        assertEquals(MoneyFormat.RU.formatForEditing(Money(9_000_00, amd)), state.toAmount.ifEmpty { state.amount })
    }

    @Test
    fun `a card without a debt opens an ordinary form`() = runTest {
        seed()
        accounts.saveAccount(Account("credit", "Кредитка", amd, credit = CreditTerms(500_000_00, 15, MinPayment.Percent(500))))
        val state = viewModel(type = "TRANSFER", creditCardId = "credit").state.value
        assertEquals("", state.amount)
    }
}
