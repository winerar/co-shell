package app.coshell.core.domain.update

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest

class CheckForUpdateUseCaseTest {
    private val start = Instant.parse("2026-11-08T10:00:00Z")
    private val source = FakeReleaseSource()
    private val store = FakeUpdateStateStore()
    private val clock = FixedClock(start)
    private val check = CheckForUpdateUseCase(source, store, InstalledVersion(10, "0.1.0"), clock)

    @Test
    fun `reports a newer release`() = runTest {
        source.release = release(11)
        assertEquals(UpdateCheckResult.Available(release(11)), check())
        assertEquals(start, store.lastCheck)
    }

    @Test
    fun `same or older release is up to date`() = runTest {
        source.release = release(10)
        assertEquals(UpdateCheckResult.UpToDate, check())
        source.release = release(9)
        assertEquals(UpdateCheckResult.UpToDate, check(force = true))
    }

    @Test
    fun `automatic check runs at most once a day`() = runTest {
        source.release = release(10)
        check()
        clock.now = start + 23.hours
        assertEquals(UpdateCheckResult.CheckedRecently, check())
        assertEquals(1, source.calls)
        clock.now = start + 24.hours
        assertEquals(UpdateCheckResult.UpToDate, check())
        assertEquals(2, source.calls)
    }

    @Test
    fun `forced check ignores the daily limit`() = runTest {
        source.release = release(10)
        check()
        source.release = release(12)
        assertEquals(UpdateCheckResult.Available(release(12)), check(force = true))
    }

    @Test
    fun `clock moved back does not block checks`() = runTest {
        store.lastCheck = start + 5.hours
        source.release = release(10)
        assertEquals(UpdateCheckResult.UpToDate, check())
    }

    @Test
    fun `failure is reported and does not count as a check`() = runTest {
        source.error = IllegalStateException("offline")
        assertEquals(UpdateCheckResult.Failed("offline"), check())
        assertEquals(null, store.lastCheck)
    }

    @Test
    fun `build without update server is not configured`() = runTest {
        assertIs<UpdateCheckResult.NotConfigured>(check())
    }
}
