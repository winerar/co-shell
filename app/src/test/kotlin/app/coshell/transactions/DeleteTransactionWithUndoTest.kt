package app.coshell.transactions

import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.testing.FakeTransactionRepository
import app.coshell.undo.UndoCenter
import kotlin.time.Instant
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DeleteTransactionWithUndoTest {
    private val transactions = FakeTransactionRepository()
    private val undo = UndoCenter()
    private val delete = DeleteTransactionWithUndo(transactions, undo)
    private val t = Transaction("t", TransactionType.EXPENSE, "card", Money(100, CurrencyCode("ARS")), Instant.fromEpochSeconds(0))

    @Test
    fun `deletes at once and undo brings it back`() = runTest {
        transactions.saveTransaction(t)
        val offer = async { undo.offers.first() }
        testScheduler.runCurrent()

        delete("t")
        assertNull(transactions.getTransaction("t"))

        offer.await().undo()
        assertEquals(t, transactions.getTransaction("t"))
    }

    @Test
    fun `unknown id does nothing`() = runTest {
        delete("missing")
        assertNull(transactions.getTransaction("missing"))
    }
}
