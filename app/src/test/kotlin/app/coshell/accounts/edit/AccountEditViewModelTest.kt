package app.coshell.accounts.edit

import androidx.lifecycle.SavedStateHandle
import app.coshell.accounts.edit.AccountEditUiState.BalanceError
import app.coshell.accounts.edit.AccountEditUiState.CurrencyError
import app.coshell.core.domain.AdjustBalanceUseCase
import app.coshell.core.domain.DeleteAccountUseCase
import app.coshell.core.domain.EnsureAdjustmentCategoriesUseCase
import app.coshell.core.domain.SaveAccountUseCase
import app.coshell.core.model.Account
import app.coshell.core.model.AccountType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.SystemCategories
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.navigation.AccountEditDestination
import app.coshell.testing.FakeAccountRepository
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeTransactionRepository
import app.coshell.testing.MainDispatcherRule
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AccountEditViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val ars = CurrencyCode("ARS")
    private val usd = CurrencyCode("USD")
    private val accounts = FakeAccountRepository()
    private val transactions = FakeTransactionRepository()
    private val categories = FakeCategoryRepository()

    private fun viewModel(accountId: String? = null) = AccountEditViewModel(
        savedStateHandle = SavedStateHandle(mapOf(AccountEditDestination.ARG_ACCOUNT_ID to accountId)),
        accounts = accounts,
        saveAccount = SaveAccountUseCase(accounts, transactions),
        deleteAccount = DeleteAccountUseCase(accounts, transactions),
        ids = IdGenerator { "new" },
        moneyFormat = MoneyFormat.RU,
        adjustBalance = AdjustBalanceUseCase(accounts, transactions, EnsureAdjustmentCategoriesUseCase(categories), IdGenerator { "adj" }),
    )

    @Test
    fun `new account is saved with parsed initial balance`() = runTest {
        val vm = viewModel()
        assertEquals("ARS", vm.state.value.currency)

        vm.onNameChange("  Наличные ")
        vm.onCurrencyChange("usd")
        vm.onTypeChange(AccountType.CASH)
        vm.onInitialBalanceChange("1 250,5")
        vm.save()

        assertTrue(vm.state.value.isDone)
        assertFalse(vm.state.value.foreignCurrencyAdded)
        assertEquals(
            Account("new", "Наличные", usd, AccountType.CASH, initialBalanceMinor = 125_050),
            accounts.getAccount("new"),
        )
    }

    @Test
    fun `local-only mark can be set when creating an account`() = runTest {
        val vm = viewModel()
        vm.onNameChange("Копилка")
        vm.onLocalOnlyChange(true)
        vm.save()

        assertTrue(accounts.getAccount("new")!!.localOnly)
    }

    @Test
    fun `local-only mark cannot be switched on for a saved account but can be switched off`() = runTest {
        accounts.saveAccount(Account("shared", "Общий", ars))
        accounts.saveAccount(Account("mine", "Копилка", ars, localOnly = true))

        val shared = viewModel("shared")
        assertTrue(shared.state.value.localOnlyLocked)
        shared.onLocalOnlyChange(true)
        assertFalse(shared.state.value.localOnly)

        val mine = viewModel("mine")
        assertFalse(mine.state.value.localOnlyLocked)
        assertTrue(mine.state.value.localOnly)
        mine.onLocalOnlyChange(false)
        mine.save()
        assertFalse(accounts.getAccount("mine")!!.localOnly)
    }

    @Test
    fun `new account outside the totals currencies asks for the currency pill`() = runTest {
        val vm = viewModel()
        vm.onNameChange("Pesos")
        vm.onCurrencyChange("ars")
        vm.save()
        assertTrue(vm.state.value.foreignCurrencyAdded)
    }

    @Test
    fun `editing an account never asks for the currency pill`() = runTest {
        accounts.saveAccount(Account("p", "Pesos", ars))
        val vm = viewModel("p")
        vm.onNameChange("Pesos 2")
        vm.save()
        assertTrue(vm.state.value.isDone)
        assertFalse(vm.state.value.foreignCurrencyAdded)
    }

    @Test
    fun `account can be left out of the total and back`() = runTest {
        accounts.saveAccount(Account("debt", "Долг Ане", usd))
        val vm = viewModel("debt")
        assertTrue(vm.state.value.includedInTotal)

        vm.onIncludedInTotalChange(false)
        vm.save()
        assertTrue(accounts.getAccount("debt")!!.excludedFromTotal)

        val reopened = viewModel("debt")
        assertFalse(reopened.state.value.includedInTotal)
        reopened.onIncludedInTotalChange(true)
        reopened.save()
        assertFalse(accounts.getAccount("debt")!!.excludedFromTotal)
    }

    @Test
    fun `account colour is saved, shown on reopen and can be removed`() = runTest {
        accounts.saveAccount(Account("card", "Карта", usd))
        val vm = viewModel("card")
        assertNull(vm.state.value.color)

        vm.onColorChange(0xFFB388FF)
        vm.save()
        assertEquals(0xFFB388FF, accounts.getAccount("card")!!.color)

        val reopened = viewModel("card")
        assertEquals(0xFFB388FF, reopened.state.value.color)
        reopened.onColorChange(null)
        reopened.save()
        assertNull(accounts.getAccount("card")!!.color)
    }

    @Test
    fun `invalid form shows errors and saves nothing`() = runTest {
        val vm = viewModel()
        vm.onCurrencyChange("A")
        vm.save()
        assertTrue(vm.state.value.nameError)
        assertEquals(CurrencyError.INVALID, vm.state.value.currencyError)

        vm.onNameChange("Карта")
        vm.onCurrencyChange("ARS")
        vm.onInitialBalanceChange("10,5555")
        vm.save()
        assertEquals(BalanceError.TOO_MANY_FRACTION_DIGITS, vm.state.value.balanceError)
        assertFalse(vm.state.value.isDone)
        assertTrue(accounts.items.value.isEmpty())
    }

    @Test
    fun `suggested currencies start with the ones in use`() = runTest {
        accounts.saveAccount(Account("a", "Ереван", CurrencyCode("AMD")))
        val vm = viewModel()
        assertEquals("AMD", vm.state.value.suggestedCurrencies.first())
        assertEquals("AMD", vm.state.value.currency)
    }

    @Test
    fun `existing account is loaded and its currency locked when used`() = runTest {
        accounts.saveAccount(Account("a", "Карта", ars, initialBalanceMinor = -1_500_00))
        transactions.saveTransaction(
            Transaction("t", TransactionType.EXPENSE, "a", Money(100, ars), Instant.fromEpochSeconds(0)),
        )
        val vm = viewModel("a")
        val state = vm.state.value
        assertFalse(state.isNew)
        assertEquals("Карта", state.name)
        assertEquals("-1 500", state.initialBalance)
        assertTrue(state.currencyLocked)

        vm.onCurrencyChange("USD")
        assertEquals("ARS", vm.state.value.currency)
    }

    @Test
    fun `delete of used account is blocked, archive works`() = runTest {
        accounts.saveAccount(Account("a", "Карта", ars))
        transactions.saveTransaction(
            Transaction("t", TransactionType.EXPENSE, "a", Money(100, ars), Instant.fromEpochSeconds(0)),
        )
        val vm = viewModel("a")
        vm.delete()
        assertTrue(vm.state.value.deleteBlocked)

        vm.dismissDeleteBlocked()
        vm.toggleArchived()
        assertTrue(vm.state.value.isDone)
        assertEquals(true, accounts.getAccount("a")?.archived)
    }

    @Test
    fun `unused account is deleted`() = runTest {
        accounts.saveAccount(Account("a", "Карта", ars))
        val vm = viewModel("a")
        vm.delete()
        assertTrue(vm.state.value.isDone)
        assertNull(accounts.getAccount("a"))
    }

    @Test
    fun `actual balance shows the difference and save records an adjustment`() = runTest {
        accounts.saveAccount(Account("cash", "Cash", ars, initialBalanceMinor = 10_000))
        val vm = viewModel("cash")
        assertTrue(vm.state.value.canAdjust)
        assertEquals("100,00\u00A0ARS", vm.state.value.currentBalance)

        vm.onActualBalanceChange("75")
        assertEquals("\u221225,00\u00A0ARS", vm.state.value.adjustment)
        vm.save()

        assertTrue(vm.state.value.isDone)
        val adj = transactions.getTransaction("adj")!!
        assertEquals(TransactionType.EXPENSE, adj.type)
        assertEquals(Money(2_500, ars), adj.amount)
        assertEquals(SystemCategories.ADJUSTMENT_EXPENSE_ID, adj.categoryId)
    }

    @Test
    fun `bad actual balance blocks saving and blank records nothing`() = runTest {
        accounts.saveAccount(Account("cash", "Cash", ars, initialBalanceMinor = 10_000))
        val vm = viewModel("cash")

        vm.onActualBalanceChange("12,3456")
        vm.save()
        assertEquals(BalanceError.TOO_MANY_FRACTION_DIGITS, vm.state.value.actualBalanceError)
        assertFalse(vm.state.value.isDone)

        vm.onActualBalanceChange("")
        assertNull(vm.state.value.adjustment)
        vm.save()
        assertTrue(vm.state.value.isDone)
        assertNull(transactions.getTransaction("adj"))
    }

    @Test
    fun `save balance records the adjustment at once and keeps the form open`() = runTest {
        accounts.saveAccount(Account("cash", "Cash", ars, initialBalanceMinor = 10_000))
        val vm = viewModel("cash")

        vm.onActualBalanceChange("120")
        vm.saveBalance()

        assertFalse(vm.state.value.isDone)
        assertEquals("+20,00\u00A0ARS", vm.state.value.balanceSaved)
        assertEquals("120,00\u00A0ARS", vm.state.value.currentBalance)
        assertEquals("", vm.state.value.actualBalance)
        val adj = transactions.getTransaction("adj")!!
        assertEquals(TransactionType.INCOME, adj.type)
        assertEquals(Money(2_000, ars), adj.amount)

        vm.dismissBalanceSaved()
        assertNull(vm.state.value.balanceSaved)
    }

    @Test
    fun `save balance with a bad or blank number records nothing`() = runTest {
        accounts.saveAccount(Account("cash", "Cash", ars, initialBalanceMinor = 10_000))
        val vm = viewModel("cash")

        vm.onActualBalanceChange("abc")
        vm.saveBalance()
        vm.onActualBalanceChange("")
        vm.saveBalance()

        assertNull(transactions.getTransaction("adj"))
        assertNull(vm.state.value.balanceSaved)
    }
}
