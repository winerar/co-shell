package app.coshell.feedback.data

import org.junit.Assert.assertEquals
import org.junit.Test

class FeedbackHistoryCodecTest {
    @Test
    fun roundTripKeepsLineBreaksAndBackslashes() {
        val entries = listOf(SentFeedback(1L, "Ошибка: две\\nстроки\nи ещё"), SentFeedback(2L, "Идея: ok"))
        assertEquals(entries, decodeFeedbackHistory(encodeFeedbackHistory(entries)))
    }

    @Test
    fun brokenLinesAreSkipped() = assertEquals(listOf(SentFeedback(5L, "x")), decodeFeedbackHistory("junk\n5\tx"))
}
