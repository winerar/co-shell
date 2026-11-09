package app.coshell.undo

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UndoCenterTest {
    @Test
    fun `delivers the offer to the collector and runs its undo`() = runTest {
        val center = UndoCenter()
        var undone = 0
        val received = launch {
            val offer = center.offers.first()
            assertEquals(42, offer.message)
            offer.undo()
        }
        testScheduler.runCurrent()
        center.offer(UndoOffer(42) { undone++ })
        received.join()
        assertEquals(1, undone)
    }
}
