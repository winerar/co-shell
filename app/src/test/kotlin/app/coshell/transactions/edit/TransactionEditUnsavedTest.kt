package app.coshell.transactions.edit

import app.coshell.core.model.Money
import app.coshell.core.model.MoneyFormat
import app.coshell.core.model.TransactionType
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** «Не сохранено» (#323, board `EwDraftA`): what was typed in a closed sheet is offered on the next new one. */
class TransactionEditUnsavedTest : TransactionEditTestBase() {
    @Test
    fun `typing keeps a draft and the next new sheet offers it`() = runTest {
        seed()
        val first = viewModel()
        first.onAmountChange("3500")
        first.onCategorySelect("cafe")
        first.onNoteChange("С Аней")

        nowOverride = now + 5.minutes
        val second = viewModel()
        val offer = second.state.value.unsaved!!
        assertEquals(MoneyFormat.RU.format(Money(3_500_00, ars)), offer.amount)
        assertEquals("Кафе", offer.label)
        assertEquals(5, offer.minutes)
        assertEquals("", second.state.value.amount)
    }

    @Test
    fun `continue puts the draft back and the plate goes away`() = runTest {
        seed()
        val first = viewModel()
        first.onAmountChange("3500")
        first.onCategorySelect("cafe")
        first.onNoteChange("С Аней")

        val second = viewModel()
        second.onUnsavedContinue()
        val s = second.state.value
        assertNull(s.unsaved)
        assertEquals(MoneyFormat.RU.formatForEditing(Money(3_500_00, ars)), s.amount)
        assertEquals("cafe", s.categoryId)
        assertEquals("С Аней", s.note)
        assertEquals("card", s.accountId)
    }

    @Test
    fun `forgetting clears the draft`() = runTest {
        seed()
        viewModel().apply { onAmountChange("100") }
        val second = viewModel()
        assertNotNull(second.state.value.unsaved)
        second.onUnsavedDiscard()
        assertNull(second.state.value.unsaved)
        assertNull(entryDraftStore.load())
        assertNull(viewModel().state.value.unsaved)
    }

    @Test
    fun `an empty sheet leaves no draft and does not wipe an older one`() = runTest {
        seed()
        viewModel().apply { onAmountChange("100") }
        viewModel() // opened and closed untouched
        assertNotNull(entryDraftStore.load())
    }

    @Test
    fun `saving clears the draft`() = runTest {
        seed()
        val vm = viewModel()
        vm.onAmountChange("100")
        vm.onCategorySelect("cafe")
        vm.save()
        assertNull(entryDraftStore.load())
        assertNull(viewModel().state.value.unsaved)
    }

    @Test
    fun `a draft older than half a day is not offered`() = runTest {
        seed()
        viewModel().apply { onAmountChange("100") }
        nowOverride = now + 13.hours
        assertNull(viewModel().state.value.unsaved)
    }

    @Test
    fun `an edit, a receipt or a template does not offer the draft`() = runTest {
        seed()
        viewModel().apply { onAmountChange("100") }
        assertNull(viewModel(transactionId = "missing").state.value.unsaved)
        assertNull(viewModel(type = "TRANSFER", creditCardId = "none").state.value.unsaved)
    }
}
