package app.coshell.family.sync

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PollingTest {
    private fun backoff() = PollBackoff(baseMs = 10_000, maxMs = 60_000, fastIdleRuns = 2)

    @Test
    fun `busy runs keep the fast pace`() = runTest {
        var runs = 0
        val job = launch { pollForever(backoff()) { runs++; true } }
        runCurrent()
        assertEquals(1, runs)
        advanceTimeBy(9_999); runCurrent()
        assertEquals(1, runs)
        advanceTimeBy(1); runCurrent()
        assertEquals(2, runs)
        advanceTimeBy(30_000); runCurrent()
        assertEquals(5, runs)
        job.cancel()
    }

    @Test
    fun `idle runs slow down up to the maximum`() {
        val b = backoff()
        val pauses = (1..6).map { b.idle(); b.nextDelayMs }
        assertEquals(listOf(10_000L, 10_000, 20_000, 40_000, 60_000, 60_000), pauses)
        b.active()
        assertEquals(10_000L, b.nextDelayMs)
    }

    @Test
    fun `a failed run does not stop polling and counts as idle`() = runTest {
        var runs = 0
        val errors = mutableListOf<Throwable>()
        val job = launch {
            pollForever(backoff(), onError = { errors += it }) {
                runs++
                if (runs == 1) error("offline")
                false
            }
        }
        advanceTimeBy(10_000); runCurrent()
        assertEquals(2, runs)
        assertEquals(1, errors.size)
        advanceTimeBy(10_000); runCurrent()
        assertEquals(3, runs) // third idle run: the next pause is 20 s
        advanceTimeBy(19_999); runCurrent()
        assertEquals(3, runs)
        advanceTimeBy(1); runCurrent()
        assertEquals(4, runs)
        job.cancel()
    }

    @Test
    fun `wake brings back the fast pace`() = runTest {
        var runs = 0
        val wake = Channel<Unit>(Channel.CONFLATED)
        val job = launch { pollForever(backoff(), wake) { runs++; false } }
        advanceTimeBy(10_000 + 10_000 + 20_000); runCurrent()
        assertEquals(4, runs) // now waiting 40 s
        advanceTimeBy(5_000)
        wake.trySend(Unit); runCurrent()
        assertEquals(4, runs) // not at once
        advanceTimeBy(10_000); runCurrent()
        assertEquals(5, runs) // one fast pause after the wake
        job.cancel()
    }
}
