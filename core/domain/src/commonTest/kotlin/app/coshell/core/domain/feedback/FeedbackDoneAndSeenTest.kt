package app.coshell.core.domain.feedback

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class FeedbackDoneAndSeenTest {
    private val doneAt = Instant.parse("2026-11-01T12:00:00Z")
    private val done = FeedbackStatus(FeedbackStage.DONE, issueNumber = 7, doneAt = doneAt, doneBuild = 40)

    @Test
    fun `done in an installed build goes away after two weeks`() {
        assertTrue(done.isDoneAndSeen(doneAt + 14.days, installedBuild = 40))
        assertTrue(done.isDoneAndSeen(doneAt + 30.days, installedBuild = 52))
    }

    @Test
    fun `stays for the first two weeks`() {
        assertFalse(done.isDoneAndSeen(doneAt + 14.days - 1.minutes, installedBuild = 40))
    }

    @Test
    fun `stays until the build with the change is installed`() {
        assertFalse(done.isDoneAndSeen(doneAt + 60.days, installedBuild = 39))
    }

    @Test
    fun `stays without a build or a date`() {
        assertFalse(done.copy(doneBuild = null).isDoneAndSeen(doneAt + 60.days, installedBuild = 99))
        assertFalse(done.copy(doneAt = null).isDoneAndSeen(doneAt + 60.days, installedBuild = 99))
    }

    @Test
    fun `postponed, in work and closed stay`() {
        listOf(FeedbackStage.LATER, FeedbackStage.IN_PROGRESS, FeedbackStage.CLOSED, FeedbackStage.RECEIVED).forEach {
            assertFalse(done.copy(stage = it).isDoneAndSeen(doneAt + 60.days, installedBuild = 99), it.name)
        }
    }
}
