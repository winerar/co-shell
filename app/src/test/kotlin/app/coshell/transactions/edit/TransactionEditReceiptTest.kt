package app.coshell.transactions.edit

import app.coshell.transactions.edit.TransactionEditUiState.EntryStep
import app.coshell.testing.FakeTemplateUsage
import app.coshell.testing.fakeEntryTags
import app.coshell.testing.fakeEntryTemplates
import app.coshell.testing.FakeTemplateRepository
import app.coshell.core.model.TransactionTemplate
import androidx.lifecycle.SavedStateHandle
import app.coshell.core.domain.SaveTransactionUseCase
import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.IdGenerator
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.navigation.TransactionEditDestination
import app.coshell.transactions.DeleteTransactionWithUndo
import app.coshell.undo.UndoCenter
import app.coshell.testing.FakeAccountRepository
import app.coshell.testing.FakeCategoryRepository
import app.coshell.testing.FakeExchangeRateRepository
import app.coshell.testing.FakeTransactionRepository
import app.coshell.testing.MainDispatcherRule
import app.coshell.core.designsystem.components.keypad.KeypadKey
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test


/** A receipt in another currency (#149): both amounts kept, refusals, dropped on a transfer. */
class TransactionEditReceiptTest : TransactionEditTestBase() {
    @Test
    fun `purchase in pesos paid from a dollar account keeps both amounts`() = runTest {
        seed()
        val vm = viewModel()
        vm.onAccountSelect("cash")
        vm.onReceiptToggle(true)
        assertEquals("ARS", vm.state.value.receiptCurrency) // the only other currency among accounts
        vm.onAmountChange("14,29")
        vm.onReceiptAmountChange("15 000")
        assertEquals("1 USD = 1\u00A0049,69 ARS", receiptRateText(vm.state.value))
        vm.onCategorySelect("food")
        vm.save()

        val saved = transactions.getTransaction("new")!!
        assertEquals(Money(14_29, usd), saved.amount)
        assertEquals(Money(15_000_00, ars), saved.originalAmount)
    }

    @Test
    fun `receipt in the account currency is refused and the receipt is reloaded on edit`() = runTest {
        seed()
        val vm = viewModel()
        vm.onReceiptToggle(true)
        vm.onReceiptCurrencyChange("ars") // account "card" is in ARS
        vm.onAmountChange("100")
        vm.onReceiptAmountChange("1")
        vm.onCategorySelect("food")
        vm.save()
        assertEquals(TransactionEditUiState.ReceiptCurrencyError.SAME_AS_ACCOUNT, vm.state.value.receiptCurrencyError)
        assertTrue(transactions.items.value.isEmpty())

        transactions.saveTransaction(
            Transaction(
                "t", TransactionType.EXPENSE, "cash", Money(10_00, usd), now,
                categoryId = "food", originalAmount = Money(10_500_00, ars),
            ),
        )
        val edit = viewModel("t").state.value
        assertTrue(edit.hasReceipt)
        assertEquals("ARS", edit.receiptCurrency)
        assertEquals(MoneyFormat.RU.formatForEditing(Money(10_500_00, ars)), edit.receiptAmount)
        assertEquals(listOf("ARS", "USD"), edit.receiptCurrencies) // earlier receipts first
    }

    @Test
    fun `switching to transfer drops the receipt`() = runTest {
        seed()
        val vm = viewModel()
        vm.onAccountSelect("cash")
        vm.onReceiptToggle(true)
        vm.onReceiptAmountChange("15 000")
        vm.onTypeChange(TransactionType.TRANSFER)
        vm.onToAccountSelect("card")
        vm.onAmountChange("10")
        vm.onToAmountChange("10 500")
        vm.save()
        assertNull(transactions.getTransaction("new")!!.originalAmount)
    }

    // region Templates (#104)
}
