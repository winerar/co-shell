package app.coshell.transactions.edit

import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionType
import app.coshell.transactions.edit.TransactionEditUiState.AccountOption
import app.coshell.transactions.edit.TransactionEditUiState.AmountError
import app.coshell.transactions.edit.TransactionEditUiState.ToAccountError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionFormTest {
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")
    private val card = Account("card", "Карта", ars)
    private val cash = Account("cash", "Наличные", usd)
    private val wallet = Account("wallet", "Кошелёк", ars)
    private val byId = listOf(card, cash, wallet).associateBy { it.id }
    private val options = byId.values.map { AccountOption(it.id, it.name, it.currency.value, it.currency.value) }

    private fun form(
        type: TransactionType = TransactionType.EXPENSE,
        amount: String = "100",
        accountId: String? = "card",
        toAccountId: String? = null,
        toAmount: String = "",
        categoryId: String? = "food",
    ) = TransactionEditUiState(
        type = type,
        amount = amount,
        toAmount = toAmount,
        accounts = options,
        accountId = accountId,
        toAccountId = toAccountId,
        categoryId = categoryId,
        note = "  кофе ",
    )

    private fun errors(form: TransactionEditUiState) = (validateForm(form, byId) as FormResult.Invalid).errors
    private fun valid(form: TransactionEditUiState) = (validateForm(form, byId) as FormResult.Valid).form

    @Test
    fun `expense is parsed in the account currency`() {
        val result = valid(form(amount = "1 250,5"))
        assertEquals(Money(1_250_50, ars), result.amount)
        assertEquals("food", result.categoryId)
        assertEquals("кофе", result.note)
    }

    @Test
    fun `amount errors`() {
        assertEquals(AmountError.REQUIRED, errors(form(amount = "")).amountError)
        assertEquals(AmountError.INVALID, errors(form(amount = "abc")).amountError)
        assertEquals(AmountError.NOT_POSITIVE, errors(form(amount = "0")).amountError)
        assertEquals(AmountError.NOT_POSITIVE, errors(form(amount = "-5")).amountError)
        assertEquals(AmountError.TOO_MANY_FRACTION_DIGITS, errors(form(amount = "1,2345")).amountError)
    }

    @Test
    fun `expense needs an account and a category`() {
        val e = errors(form(accountId = null, categoryId = null))
        assertTrue(e.accountError)
        assertTrue(e.categoryError)
    }

    @Test
    fun `transfer in one currency reuses the amount and drops the category`() {
        val result = valid(form(type = TransactionType.TRANSFER, toAccountId = "wallet"))
        assertEquals(wallet, result.toAccount)
        assertEquals(Money(100_00, ars), result.toAmount)
        assertEquals(null, result.categoryId)
    }

    @Test
    fun `transfer between currencies needs the arriving amount`() {
        val missing = form(type = TransactionType.TRANSFER, toAccountId = "cash")
        assertEquals(AmountError.REQUIRED, errors(missing).toAmountError)

        val result = valid(missing.copy(toAmount = "0,08"))
        assertEquals(Money(8, usd), result.toAmount)
    }

    @Test
    fun `transfer target errors`() {
        assertEquals(ToAccountError.REQUIRED, errors(form(type = TransactionType.TRANSFER)).toAccountError)
        assertEquals(
            ToAccountError.SAME_ACCOUNT,
            errors(form(type = TransactionType.TRANSFER, toAccountId = "card")).toAccountError,
        )
    }

    @Test
    fun `receipt in another currency is validated and kept`() {
        val base = form(accountId = "cash", amount = "14,29")
        val ok = valid(base.copy(receiptEnabled = true, receiptCurrency = "ARS", receiptAmount = "15 000"))
        assertEquals(Money(15_000_00, ars), ok.originalAmount)

        val noAmount = errors(base.copy(receiptEnabled = true, receiptCurrency = "ARS"))
        assertEquals(AmountError.REQUIRED, noAmount.receiptAmountError)
        val badCode = errors(base.copy(receiptEnabled = true, receiptCurrency = "р", receiptAmount = "1"))
        assertEquals(TransactionEditUiState.ReceiptCurrencyError.INVALID, badCode.receiptCurrencyError)
        val same = errors(base.copy(receiptEnabled = true, receiptCurrency = "USD", receiptAmount = "1"))
        assertEquals(TransactionEditUiState.ReceiptCurrencyError.SAME_AS_ACCOUNT, same.receiptCurrencyError)
        // Turned off: whatever was typed is ignored.
        assertEquals(null, valid(base.copy(receiptCurrency = "р")).originalAmount)
    }

    @Test
    fun `rate hint reads in the direction above one`() {
        val base = form(accountId = "cash", amount = "14,29").copy(receiptEnabled = true, receiptCurrency = "ARS")
        assertEquals("1 USD = 1\u00A0049,69 ARS", receiptRateText(base.copy(receiptAmount = "15 000")))
        assertEquals("1 ARS = 1\u00A0050 USD", receiptRateText(base.copy(amount = "1 050", receiptAmount = "1")))
        assertEquals(null, receiptRateText(base.copy(receiptAmount = "")))
        assertEquals("0,0067", formatRate(app.coshell.core.model.Rate.parse("0.0067")))
    }
}
