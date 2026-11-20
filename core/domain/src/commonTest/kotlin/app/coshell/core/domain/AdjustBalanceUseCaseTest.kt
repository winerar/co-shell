package app.coshell.core.domain

import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.SystemCategories
import app.coshell.core.model.TransactionType
import app.coshell.core.model.isBalanceAdjustment
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class AdjustBalanceUseCaseTest {
    private val ars = CurrencyCode("ARS")
    private val accounts = FakeAccountRepository()
    private val categories = FakeCategoryRepository()
    private val transactions = FakeTransactionRepository()
    private val now = Instant.parse("2026-11-20T12:00:00Z")
    private val adjust = AdjustBalanceUseCase(
        accounts, transactions, EnsureAdjustmentCategoriesUseCase(categories), IdGenerator { "adj-1" },
        object : Clock { override fun now() = now },
    )

    private suspend fun account(balance: Long) {
        accounts.saveAccount(Account(id = "card", name = "Card", currency = ars))
        accounts.balances.value = mapOf("card" to Money(balance, ars))
    }

    @Test
    fun `more money than the balance records an income adjustment`() = runTest {
        account(10_000)

        val result = assertIs<AdjustBalanceUseCase.Result.Adjusted>(adjust("card", Money(12_500, ars)))

        val saved = transactions.getTransaction("adj-1")!!
        assertEquals(result.transaction, saved)
        assertEquals(TransactionType.INCOME, saved.type)
        assertEquals(Money(2_500, ars), saved.amount)
        assertEquals(SystemCategories.ADJUSTMENT_INCOME_ID, saved.categoryId)
        assertEquals(now, saved.date)
        assertTrue(saved.isBalanceAdjustment)
    }

    @Test
    fun `less money records an expense adjustment and creates the category`() = runTest {
        account(10_000)

        adjust("card", Money(-500, ars))

        val saved = transactions.getTransaction("adj-1")!!
        assertEquals(TransactionType.EXPENSE, saved.type)
        assertEquals(Money(10_500, ars), saved.amount)
        assertEquals(SystemCategories.ADJUSTMENT_EXPENSE_ID, saved.categoryId)
        assertTrue(categories.getCategory(SystemCategories.ADJUSTMENT_EXPENSE_ID) != null)
    }

    @Test
    fun `the same balance records nothing`() = runTest {
        account(10_000)

        assertEquals(AdjustBalanceUseCase.Result.Unchanged, adjust("card", Money(10_000, ars)))
        assertEquals(0, transactions.countForAccount("card"))
    }

    @Test
    fun `refuses an unknown account and another currency`() = runTest {
        account(10_000)

        assertEquals(AdjustBalanceUseCase.Result.Invalid(AdjustBalanceUseCase.Reason.ACCOUNT_NOT_FOUND), adjust("x", Money(1, ars)))
        assertEquals(
            AdjustBalanceUseCase.Result.Invalid(AdjustBalanceUseCase.Reason.CURRENCY_MISMATCH),
            adjust("card", Money(1, CurrencyCode("USD"))),
        )
    }

    @Test
    fun `difference is actual minus current`() = runTest {
        account(10_000)

        assertEquals(Money(-3_000, ars), adjust.difference("card", Money(7_000, ars)))
    }
}
