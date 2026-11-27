package app.coshell.transactions.edit

import app.coshell.core.model.Draft
import app.coshell.core.model.DraftCategorySource
import app.coshell.core.model.TransactionType
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** A draft of a quick note in the entry sheet (#586): the form is filled from it, saving removes it. */
class TransactionEditDraftTest : TransactionEditTestBase() {
    private val day = 86_400_000L

    private fun draft(
        amount: Long? = 3_500_00,
        currency: app.coshell.core.model.CurrencyCode = ars,
        account: String? = "card",
        category: String? = "cafe",
        source: DraftCategorySource? = DraftCategorySource.HISTORY,
    ) = Draft("d1", "кофе", currency, amount, account, category, source, now.toEpochMilliseconds() - day, now.toEpochMilliseconds())

    @Test
    fun `the form opens filled from the draft`() = runTest {
        seed()
        drafts.saveDrafts(listOf(draft()))
        val state = viewModel(draftId = "d1").state.value
        assertEquals(TransactionType.EXPENSE, state.type)
        assertEquals("card", state.accountId)
        assertEquals("cafe", state.categoryId)
        assertEquals("кофе", state.note)
        assertEquals("3500", state.amount.filter { it.isDigit() })
        assertEquals(state.today.minus(1, DateTimeUnit.DAY), state.date)
        assertTrue(state.categoryGuessed)
        assertFalse(state.receiptEnabled)
    }

    @Test
    fun `an amount in another currency than the account goes to the purchase amount`() = runTest {
        seed()
        drafts.saveDrafts(listOf(draft(currency = usd, account = "card")))
        val state = viewModel(draftId = "d1").state.value
        assertEquals("", state.amount)
        assertTrue(state.receiptEnabled)
        assertEquals("USD", state.receiptCurrency)
        assertTrue(state.receiptAmount.isNotEmpty())
    }

    @Test
    fun `a draft without an amount or account leaves them for the person`() = runTest {
        seed()
        drafts.saveDrafts(listOf(draft(amount = null, account = null, category = null, source = null)))
        val state = viewModel(draftId = "d1").state.value
        assertEquals("", state.amount)
        assertNull(state.categoryId)
        assertEquals("card", state.accountId) // the usual default: the first account
    }

    @Test
    fun `saving the operation removes the draft`() = runTest {
        seed()
        drafts.saveDrafts(listOf(draft()))
        val vm = viewModel(draftId = "d1")
        vm.save()
        assertTrue(vm.state.value.isDone)
        assertNull(drafts.getDraft("d1"))
        assertEquals(1, transactions.items.value.size)
    }

    @Test
    fun `an unsaved sheet keeps the draft`() = runTest {
        seed()
        drafts.saveDrafts(listOf(draft(amount = null)))
        val vm = viewModel(draftId = "d1")
        vm.save() // no amount: the form shows the error, nothing is saved
        assertFalse(vm.state.value.isDone)
        assertEquals("d1", drafts.getDraft("d1")?.id)
    }
}
