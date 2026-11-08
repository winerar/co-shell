package app.coshell.core.domain

import app.coshell.core.domain.usecase.SaveTransaction
import app.coshell.core.domain.usecase.SaveTransaction.Error
import app.coshell.core.domain.usecase.SaveTransaction.Result
import app.coshell.core.model.Money
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SaveTransactionTest {
    private val accounts = FakeAccountRepository()
    private val categories = FakeCategoryRepository()
    private val transactions = FakeTransactionRepository()
    private val save = SaveTransaction(accounts, categories, transactions)

    private suspend fun setUp() {
        accounts.upsert(rubCard)
        accounts.upsert(eurCash)
        categories.upsert(food)
        categories.upsert(salary)
    }

    @Test
    fun `saves a valid expense`() = runTest {
        setUp()
        val expense = tx("1", TransactionType.EXPENSE, Money(500, RUB), category = food)
        assertEquals(Result.Saved, save(expense))
        assertEquals(expense, transactions.getTransaction(expense.id))
    }

    @Test
    fun `saves a currency exchange transfer`() = runTest {
        setUp()
        val exchange = tx("1", TransactionType.TRANSFER, Money(10_000, RUB), to = eurCash, toAmount = Money(100, EUR))
        assertEquals(Result.Saved, save(exchange))
    }

    @Test
    fun `rejects amount in a currency other than the account's`() = runTest {
        setUp()
        val wrong = tx("1", TransactionType.EXPENSE, Money(500, EUR), category = food)
        assertEquals(Result.Invalid(Error.CURRENCY_MISMATCH), save(wrong))
        assertNull(transactions.getTransaction(wrong.id))
    }

    @Test
    fun `rejects transfer amount in a currency other than the target's`() = runTest {
        setUp()
        val wrong = tx("1", TransactionType.TRANSFER, Money(10_000, RUB), to = eurCash, toAmount = Money(100, RUB))
        assertEquals(Result.Invalid(Error.CURRENCY_MISMATCH), save(wrong))
    }

    @Test
    fun `rejects unknown or deleted accounts`() = runTest {
        setUp()
        accounts.delete(rubCard.id)
        val expense = tx("1", TransactionType.EXPENSE, Money(500, RUB))
        assertEquals(Result.Invalid(Error.ACCOUNT_NOT_FOUND), save(expense))
    }

    @Test
    fun `rejects category of the wrong type`() = runTest {
        setUp()
        val wrong = tx("1", TransactionType.EXPENSE, Money(500, RUB), category = salary)
        assertEquals(Result.Invalid(Error.CATEGORY_TYPE_MISMATCH), save(wrong))
    }

    @Test
    fun `allows a transaction without category`() = runTest {
        setUp()
        assertEquals(Result.Saved, save(tx("1", TransactionType.INCOME, Money(500, RUB))))
    }
}
