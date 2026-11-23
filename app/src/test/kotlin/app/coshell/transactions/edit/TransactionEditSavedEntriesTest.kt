package app.coshell.transactions.edit

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.undo.SavedEntries
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** #325: only a **new** operation opens the «Сохранено» / shake window; an edit never does. */
class TransactionEditSavedEntriesTest : TransactionEditTestBase() {
    private class Recorder : SavedEntries {
        val calls = mutableListOf<Pair<List<Transaction>, String>>()
        override fun saved(items: List<Transaction>, amount: String) {
            calls += items to amount
        }
    }

    @Test
    fun `a new operation reports its save with the formatted amount`() = runTest {
        seed()
        val recorder = Recorder()
        val vm = viewModel(savedEntries = recorder)
        vm.onAmountChange("3 500")
        vm.onCategorySelect("cafe")
        vm.save()

        assertTrue(vm.state.value.isDone)
        val (items, amount) = recorder.calls.single()
        assertEquals(listOf("new"), items.map { it.id })
        assertEquals(MoneyAmountText, amount)
    }

    @Test
    fun `editing an existing operation never opens the undo window`() = runTest {
        seed()
        transactions.saveTransaction(Transaction("old-op", TransactionType.EXPENSE, "card", Money(1_000_00, ars), now, categoryId = "cafe"))
        val recorder = Recorder()
        val vm = viewModel(transactionId = "old-op", savedEntries = recorder)
        vm.onAmountChange("2 000")
        vm.save()

        assertTrue(vm.state.value.isDone)
        assertEquals(0, recorder.calls.size)
    }

    @Test
    fun `an invalid form reports nothing`() = runTest {
        seed()
        val recorder = Recorder()
        val vm = viewModel(savedEntries = recorder)
        vm.save()

        assertEquals(0, recorder.calls.size)
    }

    private companion object {
        /** `MoneyFormat.RU` of 3 500 ARS: narrow no-break space groups, no-break space before the code. */
        val MoneyAmountText = MoneyFormat.RU.format(Money(3_500_00, CurrencyCode("ARS")))
    }
}
