package app.coshell.transactions.edit

import app.coshell.core.domain.categoryguess.CategoryGuess
import app.coshell.core.model.Category
import app.coshell.core.model.CategoryType
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** «Умная категория» in the entry sheet (#584): filled from the past, never over a manual choice. */
class TransactionEditGuessTest : TransactionEditTestBase() {
    private val link = "https://www.afip.gob.ar/fe/qr/?p=eyJ2ZXIiOjEsImN0eiI6MSwidGlwb0RvY1JlYyI6OTksInB0b1Z0YSI6NDQyLCJpbXBvcnRlIjozNzkxNCwidGlwb0NtcCI6NiwiZmVjaGEiOiIyMDI2LTExLTE5IiwiY29kQXV0Ijo4NjQ3MzQ2MjgyODA0OSwiY3VpdCI6MzA2ODU4NDk3NTEsInRpcG9Db2RBdXQiOiJFIiwibnJvQ21wIjoxMTc4MTcsIm5yb0RvY1JlYyI6MCwibW9uZWRhIjoiQVJTIn0="

    private suspend fun seedPast(note: String = "", key: String? = null) = transactions.saveTransaction(
        Transaction("past", TransactionType.EXPENSE, "card", Money(100_00, ars), now, categoryId = "cafe", note = note, receiptKey = key),
    )

    @Test
    fun `a scanned seller seen before gives its category`() = runTest {
        seed()
        seedPast(key = "30685849751:1:6:5")
        val state = viewModel(receiptQr = link).state.value
        assertEquals("cafe", state.categoryId)
        assertTrue(state.categoryGuessed)
    }

    @Test
    fun `a new seller leaves the category empty`() = runTest {
        seed()
        assertNull(viewModel(receiptQr = link).state.value.categoryId)
    }

    @Test
    fun `the note guesses until a manual choice`() = runTest {
        seed()
        seedPast(note = "Panadería Sol")
        val vm = viewModel()
        vm.onNoteChange("panadería sol")
        assertEquals("cafe", vm.state.value.categoryId)
        assertTrue(vm.state.value.categoryGuessed)

        vm.onNoteChange("other")
        assertNull(vm.state.value.categoryId)

        vm.onCategorySelect("food")
        vm.onNoteChange("Panadería Sol")
        assertEquals("food", vm.state.value.categoryId)
        assertFalse(vm.state.value.categoryGuessed)
    }

    @Test
    fun `a brand never seen before is guessed from the dictionary and marked`() = runTest {
        seed()
        categories.saveCategory(Category("rest", "Кафе", CategoryType.EXPENSE, icon = "restaurant"))
        val vm = viewModel()
        vm.onNoteChange("Starbucks")
        assertEquals("rest", vm.state.value.categoryId)
        assertTrue(vm.state.value.categoryGuessed)
        assertEquals(CategoryGuess.Source.DICTIONARY, vm.state.value.categoryGuessSource)

        vm.onNoteChange("Juan Pérez")
        assertNull(vm.state.value.categoryId)
        assertNull(vm.state.value.categoryGuessSource)
    }
}
