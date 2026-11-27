package app.coshell.undo

import app.coshell.R
import app.coshell.testing.FakeTransactionRepository
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** #798: «Изменено · Отменить» puts the old values of an edited operation back. */
class EditedEntryUndoTest {
    private val ars = CurrencyCode("ARS")
    private val before = Transaction("op", TransactionType.EXPENSE, "card", Money(1_000_00, ars), Instant.fromEpochMilliseconds(0), categoryId = "cafe")
    private val after = before.copy(amount = Money(2_000_00, ars))

    @Test
    fun `an edit offers undo that restores the old row`() = runTest {
        val repo = FakeTransactionRepository()
        repo.saveTransaction(after)
        val center = UndoCenter()
        EditedEntryUndo(repo, center).edited(before, after)

        val offer = center.latest!!
        assertEquals(R.string.entry_edited, offer.message)
        offer.undo()
        assertEquals(before.amount, repo.getTransaction("op")?.amount)
    }

    @Test
    fun `saving the same values offers nothing`() {
        val center = UndoCenter()
        EditedEntryUndo(FakeTransactionRepository(), center).edited(before, before)
        assertNull(center.latest)
    }
}
