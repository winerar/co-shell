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
import app.coshell.core.model.CreditTerms
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.MinPayment
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
    fun `card digits are kept as four numbers and saved with the account`() = runTest {
        val vm = viewModel()
        vm.onNameChange("Карта")
        vm.onCardDigitsChange("40 83**3900-77")
        assertEquals("4083", vm.state.value.cardDigits)
        vm.onCardDigitsChange("3900")
        vm.save()

        assertTrue(vm.state.value.isDone)
        assertEquals("3900", accounts.getAccount("new")!!.cardDigits)
    }

    @Test
    fun `fewer than four digits are refused and an empty field unlinks the account`() = runTest {
        accounts.saveAccount(Account("a", "Карта", ars, cardDigits = "3900"))
        val vm = viewModel("a")
        assertEquals("3900", vm.state.value.cardDigits)

        vm.onCardDigitsChange("39")
        vm.save()
        assertTrue(vm.state.value.cardDigitsError)
        assertFalse(vm.state.value.isDone)

        vm.onCardDigitsChange("")
        assertFalse(vm.state.value.cardDigitsError)
        vm.save()
        assertTrue(vm.state.value.isDone)
        assertNull(accounts.getAccount("a")!!.cardDigits)
    }

    @Test
    fun `a travel pass is saved as another account out of the total and another type clears it`() = runTest {
        val vm = viewModel()
        vm.onNameChange("SUBE")
        vm.onPrepaidSelect()
        assertTrue(vm.state.value.prepaid)
        assertEquals(AccountType.OTHER, vm.state.value.type)
        assertFalse(vm.state.value.includedInTotal)
        vm.onInitialBalanceChange("2 400")
        vm.save()

        val saved = accounts.getAccount("new")!!
        assertTrue(saved.prepaid)
        assertEquals(AccountType.OTHER, saved.type)
        assertTrue(saved.excludedFromTotal)
        assertEquals(240_000, saved.initialBalanceMinor)

        val again = viewModel("new")
        assertTrue(again.state.value.prepaid)
        again.onTypeChange(AccountType.CARD)
        assertFalse(again.state.value.prepaid)
        again.save()
        assertFalse(accounts.getAccount("new")!!.prepaid)
    }

    @Test
    fun `a credit card keeps its conditions and another type drops them`() = runTest {
        val vm = viewModel()
        vm.onNameChange("Кредитка")
        vm.onCurrencyChange("amd")
        vm.onCreditSelect()
        assertTrue(vm.state.value.credit)
        assertEquals(AccountType.OTHER, vm.state.value.type)
        vm.onCreditLimitChange("500 000")
        vm.onCreditPayDayChange("1x5")
        assertEquals("15", vm.state.value.creditPayDay)
        vm.onCreditMinSumChange(true)
        vm.onCreditMinChange("9 000")
        vm.save()

        val saved = accounts.getAccount("new")!!
        assertEquals(CreditTerms(500_000_00, 15, MinPayment.Fixed(9_000_00)), saved.credit)

        val again = viewModel("new")
        assertTrue(again.state.value.credit)
        assertEquals("15", again.state.value.creditPayDay)
        again.onTypeChange(AccountType.CARD)
        again.save()
        assertNull(accounts.getAccount("new")!!.credit)
    }

    @Test
    fun `a credit card without full conditions is not saved`() = runTest {
        val vm = viewModel()
        vm.onNameChange("Кредитка")
        vm.onCreditSelect()
        vm.onCreditLimitChange("500 000")
        vm.save()

        assertTrue(vm.state.value.creditError)
        assertFalse(vm.state.value.isDone)
        assertNull(accounts.getAccount("new"))
        vm.onCreditPayDayChange("15")
        assertFalse(vm.state.value.creditError)
    }

    @Test
    fun `a travel pass and a credit card exclude each other`() = runTest {
        val vm = viewModel()
        vm.onCreditSelect()
        vm.onPrepaidSelect()
        assertTrue(vm.state.value.prepaid)
        assertFalse(vm.state.value.credit)
        vm.onCreditSelect()
        assertTrue(vm.state.value.credit)
        assertFalse(vm.state.value.prepaid)
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
