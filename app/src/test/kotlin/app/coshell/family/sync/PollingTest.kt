package app.coshell.family.sync

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PollingTest {
    @Test
    fun `runs at once and then every interval`() = runTest {
        var runs = 0
        val job = launch { pollForever(10_000) { runs++ } }
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
    fun `a failed run does not stop polling`() = runTest {
        var runs = 0
        val errors = mutableListOf<Throwable>()
        val job = launch {
            pollForever(10_000, onError = { errors += it }) {
                runs++
                if (runs == 1) error("offline")
            }
        }
        advanceTimeBy(10_000); runCurrent()
        assertEquals(2, runs)
        assertEquals(1, errors.size)
        job.cancel()
    }
}
