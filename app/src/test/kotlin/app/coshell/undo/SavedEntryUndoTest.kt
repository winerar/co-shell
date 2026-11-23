package app.coshell.undo

import app.coshell.R
import app.coshell.core.domain.shake.ShakeIntent
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Money
import app.coshell.core.model.Transaction
import app.coshell.core.model.TransactionType
import app.coshell.testing.FakeTransactionRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** #325: «Сохранено · Отменить» for 4 s, and the same window for a shake; nothing but the last save is ever touched. */
class SavedEntryUndoTest {
    private val transactions = FakeTransactionRepository()
    private val center = UndoCenter()
    private var time = Instant.fromEpochSeconds(1_000)
    private val clock = object : Clock { override fun now(): Instant = time }
    private val saved = SavedEntryUndo(transactions, center, clock)

    private fun op(id: String) = Transaction(id, TransactionType.EXPENSE, "card", Money(350_000, CurrencyCode("ARS")), time)

    private suspend fun save(vararg ids: String) {
        ids.forEach { transactions.saveTransaction(op(it)) }
        saved.saved(ids.map(::op), "3 500 ARS")
    }

    @Test
    fun `a save offers a short snackbar with its amount`() = runTest {
        val offer = async { center.offers.first() }
        testScheduler.runCurrent()

        save("a")

        val shown = offer.await()
        assertEquals(R.string.entry_saved, shown.message)
        assertEquals(listOf<Any>("3 500 ARS"), shown.args)
        assertTrue(shown.short)
    }

    @Test
    fun `a shake right after the save means undo and a later one the usual`() = runTest {
        save("a")
        time += 3.seconds
        assertEquals(ShakeIntent.UNDO_LAST_SAVE, saved.intent())
        time += 2.seconds
        assertEquals(ShakeIntent.NEW_ENTRY, saved.intent())
    }

    @Test
    fun `with nothing saved a shake is the usual`() = runTest {
        assertEquals(ShakeIntent.NEW_ENTRY, saved.intent())
    }

    @Test
    fun `undoing by a shake removes the save and offers to bring it back`() = runTest {
        save("a", "tip")
        val offers = async { center.offers.first() }
        testScheduler.runCurrent()

        saved.undoLast()

        assertNull(transactions.getTransaction("a"))
        assertNull(transactions.getTransaction("tip"))
        val back = offers.await()
        assertEquals(R.string.entry_undone, back.message)
        assertEquals(R.string.action_restore, back.actionLabel)
        back.undo()
        assertEquals("a", transactions.getTransaction("a")?.id)
        assertEquals("tip", transactions.getTransaction("tip")?.id)
    }

    @Test
    fun `a second shake never removes anything else`() = runTest {
        save("a")
        saved.undoLast()
        transactions.saveTransaction(op("older"))

        assertEquals(ShakeIntent.NEW_ENTRY, saved.intent())
        saved.undoLast()
        assertEquals("older", transactions.getTransaction("older")?.id)
    }

    @Test
    fun `another offer in between closes the window`() = runTest {
        save("a")
        center.offer(UndoOffer(R.string.transaction_deleted) { })

        assertEquals(ShakeIntent.NEW_ENTRY, saved.intent())
    }

    @Test
    fun `the snackbar own undo takes the save back and closes the window`() = runTest {
        val offer = async { center.offers.first() }
        testScheduler.runCurrent()
        save("a")

        offer.await().undo()

        assertNull(transactions.getTransaction("a"))
        assertEquals(ShakeIntent.NEW_ENTRY, saved.intent())
    }
}
