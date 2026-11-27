package app.coshell.transactions.edit

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.undo.EditedEntries
import app.coshell.undo.SavedEntries
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** #325: only a **new** operation opens the «Сохранено» / shake window; an edit never does (it gets «Изменено», #798). */
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
    fun `editing an existing operation reports the old and the new row`() = runTest {
        seed()
        val old = Transaction("old-op", TransactionType.EXPENSE, "card", Money(1_000_00, ars), now, categoryId = "cafe")
        transactions.saveTransaction(old)
        val calls = mutableListOf<Pair<Transaction, Transaction>>()
        val vm = viewModel(transactionId = "old-op", editedEntries = EditedEntries { before, after -> calls += before to after })
        vm.onAmountChange("2 000")
        vm.save()

        val (before, after) = calls.single()
        assertEquals(old, before)
        assertEquals(Money(2_000_00, ars), after.amount)
    }

    @Test
    fun `a new operation does not report an edit`() = runTest {
        seed()
        var edits = 0
        val vm = viewModel(editedEntries = EditedEntries { _, _ -> edits++ })
        vm.onAmountChange("3 500")
        vm.onCategorySelect("cafe")
        vm.save()

        assertEquals(0, edits)
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
