package app.coshell.core.domain

import app.coshell.core.model.Account
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class SaveAccountUseCaseTest {
    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")
    private val accounts = FakeAccountRepository()
    private val transactions = FakeTransactionRepository()
    private val save = SaveAccountUseCase(accounts, transactions)

    @Test
    fun newAccountsAreAppendedAfterExistingOnes() = runTest {
        accounts.saveAccount(Account("a", "Наличные", ars, sortOrder = 4))
        accounts.saveAccount(Account("b", "Старый", ars, archived = true, sortOrder = 7))

        assertEquals(SaveAccountUseCase.Result.Saved, save(Account("c", "Карта", usd, sortOrder = 0)))
        assertEquals(8, accounts.getAccount("c")?.sortOrder)
    }

    @Test
    fun firstAccountGetsOrderZero() = runTest {
        save(Account("a", "Наличные", ars, sortOrder = 3))
        assertEquals(0, accounts.getAccount("a")?.sortOrder)
    }

    @Test
    fun currencyOfUnusedAccountCanChange() = runTest {
        accounts.saveAccount(Account("a", "Карта", ars))
        assertEquals(SaveAccountUseCase.Result.Saved, save(Account("a", "Карта", usd)))
        assertEquals(usd, accounts.getAccount("a")?.currency)
    }

    @Test
    fun currencyOfUsedAccountIsLocked() = runTest {
        accounts.saveAccount(Account("a", "Карта", ars))
        transactions.saveTransaction(
            Transaction("t", TransactionType.EXPENSE, "a", Money(100, ars), Instant.fromEpochSeconds(0)),
        )

        val result = save(Account("a", "Карта", usd))

        assertEquals(SaveAccountUseCase.Result.Invalid(SaveAccountUseCase.Reason.CURRENCY_LOCKED), result)
        assertEquals(ars, accounts.getAccount("a")?.currency)
        // Other fields can still be edited.
        assertEquals(SaveAccountUseCase.Result.Saved, save(Account("a", "Основная карта", ars)))
        assertEquals("Основная карта", accounts.getAccount("a")?.name)
    }
}
