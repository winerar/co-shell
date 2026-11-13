package app.coshell.core.domain

import app.coshell.core.domain.SaveTransactionUseCase.Reason
import app.coshell.core.domain.SaveTransactionUseCase.Result
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class SaveTransactionUseCaseTest {
    private val usd = CurrencyCode("USD")
    private val ars = CurrencyCode("ARS")
    private val date = Instant.parse("2026-11-08T12:00:00Z")

    private val accounts = FakeAccountRepository()
    private val categories = FakeCategoryRepository()
    private val transactions = FakeTransactionRepository()
    private val save = SaveTransactionUseCase(accounts, categories, transactions)

    @BeforeTest
    fun setUp() = runTest {
        accounts.saveAccount(Account("usd", "Bybit", usd))
        accounts.saveAccount(Account("ars", "Песо", ars))
        categories.saveCategory(Category("food", "Продукты", CategoryType.EXPENSE))
        categories.saveCategory(Category("salary", "Зарплата", CategoryType.INCOME))
    }

    @Test
    fun `saves a valid expense`() = runTest {
        val tx = Transaction("t", TransactionType.EXPENSE, "ars", Money(1_500_000, ars), date, categoryId = "food")
        assertEquals(Result.Saved, save(tx))
        assertTrue("t" in transactions.items.value)
    }

    @Test
    fun `saves a currency exchange between accounts`() = runTest {
        val tx = Transaction(
            "t", TransactionType.TRANSFER, "usd", Money(1000_00, usd), date,
            toAccountId = "ars", toAmount = Money(1_568_000_00, ars),
        )
        assertEquals(Result.Saved, save(tx))
    }

    @Test
    fun `saves a purchase paid from an account in another currency`() = runTest {
        // Receipt 15 000 ARS, debited 14.29 USD
        val tx = Transaction(
            "t", TransactionType.EXPENSE, "usd", Money(1_429, usd), date,
            categoryId = "food", originalAmount = Money(1_500_000, ars),
        )
        assertEquals(Result.Saved, save(tx))
        assertEquals(Money(1_500_000, ars), transactions.items.value["t"]?.originalAmount)
    }

    @Test
    fun `rejects a receipt in the account currency or on a transfer`() = runTest {
        val same = Transaction(
            "t", TransactionType.EXPENSE, "usd", Money(1_429, usd), date,
            categoryId = "food", originalAmount = Money(1_429, usd),
        )
        assertEquals(Result.Invalid(Reason.ORIGINAL_CURRENCY_INVALID), save(same))
        val transfer = Transaction(
            "t", TransactionType.TRANSFER, "usd", Money(100, usd), date,
            toAccountId = "ars", toAmount = Money(100_000, ars), originalAmount = Money(100_000, ars),
        )
        assertEquals(Result.Invalid(Reason.ORIGINAL_CURRENCY_INVALID), save(transfer))
        assertTrue(transactions.items.value.isEmpty())
    }

    @Test
    fun `rejects an amount in a currency other than the account's`() = runTest {
        val tx = Transaction("t", TransactionType.EXPENSE, "ars", Money(100, usd), date)
        assertEquals(Result.Invalid(Reason.CURRENCY_MISMATCH), save(tx))
        assertTrue(transactions.items.value.isEmpty())
    }

    @Test
    fun `rejects a transfer whose target amount is in the wrong currency`() = runTest {
        val tx = Transaction(
            "t", TransactionType.TRANSFER, "usd", Money(100, usd), date,
            toAccountId = "ars", toAmount = Money(100, usd),
        )
        assertEquals(Result.Invalid(Reason.TARGET_CURRENCY_MISMATCH), save(tx))
    }

    @Test
    fun `rejects unknown accounts and categories`() = runTest {
        assertEquals(
            Result.Invalid(Reason.ACCOUNT_NOT_FOUND),
            save(Transaction("t", TransactionType.EXPENSE, "nope", Money(1, usd), date)),
        )
        assertEquals(
            Result.Invalid(Reason.CATEGORY_NOT_FOUND),
            save(Transaction("t", TransactionType.EXPENSE, "usd", Money(1, usd), date, categoryId = "nope")),
        )
    }

    @Test
    fun `rejects an income category on an expense`() = runTest {
        val tx = Transaction("t", TransactionType.EXPENSE, "usd", Money(1, usd), date, categoryId = "salary")
        assertEquals(Result.Invalid(Reason.CATEGORY_TYPE_MISMATCH), save(tx))
    }

    @Test
    fun `accepts an income in an expense category as a refund`() = runTest {
        val tx = Transaction("t", TransactionType.INCOME, "usd", Money(1, usd), date, categoryId = "food")
        assertEquals(Result.Saved, save(tx))
    }
}
