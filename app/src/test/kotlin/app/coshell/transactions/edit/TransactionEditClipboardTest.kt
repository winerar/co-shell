package app.coshell.transactions.edit

import app.coshell.core.model.TransactionType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** «Вставить из буфера» (#321): the amount, currency and note come from the text on the clipboard. */
class TransactionEditClipboardTest : TransactionEditTestBase() {
    @Test
    fun `an amount in the account currency fills the amount and the note`() = runTest {
        seed()
        val vm = viewModel()
        vm.onPasteText("Compra con tarjeta ARS 12.500,00 en COTO")
        val state = vm.state.value
        assertEquals("12500", state.amount.filter { it.isDigit() }.take(5))
        assertFalse(state.receiptEnabled)
        assertTrue(state.note.contains("COTO"))
        assertFalse(state.clipboardNone)
    }

    @Test
    fun `an amount in another currency goes to the purchase amount`() = runTest {
        seed()
        val vm = viewModel()
        vm.onPasteText("Paid 15 USD at the shop")
        val state = vm.state.value
        assertEquals("", state.amount)
        assertTrue(state.receiptEnabled)
        assertEquals("USD", state.receiptCurrency)
    }

    @Test
    fun `a text without an amount shows the pill and changes nothing`() = runTest {
        seed()
        val vm = viewModel()
        vm.onPasteText("hello there")
        assertTrue(vm.state.value.clipboardNone)
        assertEquals("", vm.state.value.amount)
        vm.onClipboardNoneDismiss()
        assertFalse(vm.state.value.clipboardNone)
    }

    @Test
    fun `an empty clipboard shows the pill`() = runTest {
        seed()
        val vm = viewModel()
        vm.onPasteText(null)
        assertTrue(vm.state.value.clipboardNone)
    }

    @Test
    fun `the paste icon stays on a new operation and leaves for a transfer`() = runTest {
        seed()
        val vm = viewModel()
        assertTrue(vm.state.value.showsPasteButton)
        vm.onPasteText("ARS 500")
        assertTrue(vm.state.value.showsPasteButton)
        vm.onTypeChange(TransactionType.TRANSFER)
        assertFalse(vm.state.value.showsPasteButton)
    }
}
