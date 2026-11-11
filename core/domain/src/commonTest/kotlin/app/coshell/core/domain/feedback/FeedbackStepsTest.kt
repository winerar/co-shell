package app.coshell.core.domain.feedback

import app.coshell.core.domain.feedback.FeedbackStep.Kind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

class FeedbackStepsTest {
    private val sent = Instant.parse("2026-11-09T18:21:00Z")
    private val task = Instant.parse("2026-11-09T19:00:00Z")
    private val work = Instant.parse("2026-11-11T09:00:00Z")
    private val done = Instant.parse("2026-11-12T12:00:00Z")

    private fun kinds(steps: List<FeedbackStep>) = steps.map { it.kind to it.reached }

    @Test
    fun `without a status only sending is reached, the rest is ahead`() {
        assertEquals(
            listOf(Kind.SENT to true, Kind.TASK to false, Kind.IN_PROGRESS to false, Kind.DONE to false),
            kinds(feedbackSteps(sent, null)),
        )
    }

    @Test
    fun `done in a build walks the whole way`() {
        val steps = feedbackSteps(sent, FeedbackStatus(FeedbackStage.DONE, 105, task, work, done, 248))
        assertEquals(listOf(Kind.SENT to true, Kind.TASK to true, Kind.IN_PROGRESS to true, Kind.DONE to true), kinds(steps))
        assertEquals(248, steps.last().build)
        assertEquals(task, steps[1].at)
    }

    @Test
    fun `done without a seen work step skips it`() {
        val steps = feedbackSteps(sent, FeedbackStatus(FeedbackStage.DONE, 105, task, doneAt = done))
        assertEquals(listOf(Kind.SENT to true, Kind.TASK to true, Kind.DONE to true), kinds(steps))
    }

    @Test
    fun `postponed and closed end the timeline`() {
        assertEquals(Kind.LATER, feedbackSteps(sent, FeedbackStatus(FeedbackStage.LATER, 7, task)).last().kind)
        assertEquals(Kind.CLOSED, feedbackSteps(sent, FeedbackStatus(FeedbackStage.CLOSED, 7, task, doneAt = done)).last().kind)
    }

    @Test
    fun `status settles once done is released or closed`() {
        assertFalse(FeedbackStatus(FeedbackStage.DONE, 1, doneAt = done).isSettled)
        assertTrue(FeedbackStatus(FeedbackStage.DONE, 1, doneAt = done, doneBuild = 248).isSettled)
        assertTrue(FeedbackStatus(FeedbackStage.CLOSED, 1).isSettled)
        assertFalse(FeedbackStatus(FeedbackStage.LATER, 1).isSettled)
    }
}
