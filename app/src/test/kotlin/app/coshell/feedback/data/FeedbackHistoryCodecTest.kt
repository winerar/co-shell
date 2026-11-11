package app.coshell.feedback.data

import app.coshell.core.domain.feedback.FeedbackStage
import app.coshell.core.domain.feedback.FeedbackStatus
import kotlin.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedbackHistoryCodecTest {
    @Test
    fun roundTripKeepsTextIdAndStatus() {
        val status = FeedbackStatus(
            FeedbackStage.DONE, 105, Instant.parse("2026-11-09T19:00:00Z"), Instant.parse("2026-11-11T09:00:00Z"),
            Instant.parse("2026-11-12T12:00:00Z"), 248,
        )
        val entries = listOf(
            SentFeedback(1L, "Ошибка: две\\nстроки\nи \"ещё\"", "doc-1", status),
            SentFeedback(2L, "Идея: ok", "doc-2", FeedbackStatus(FeedbackStage.SENT)),
            SentFeedback(3L, "старый"),
        )
        assertEquals(entries, decodeFeedbackHistory(encodeFeedbackHistory(entries)))
    }

    @Test
    fun oldFormatIsStillRead() = assertEquals(
        listOf(SentFeedback(5L, "x"), SentFeedback(6L, "две\nстроки")),
        decodeFeedbackHistory("junk\n5\tx\n6\tдве\\nстроки"),
    )

    @Test
    fun brokenJsonIsEmpty() = assertEquals(emptyList<SentFeedback>(), decodeFeedbackHistory("[{\"broken"))
}
