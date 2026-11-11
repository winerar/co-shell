package app.coshell.feedback.data

import app.coshell.core.domain.feedback.FeedbackStage
import app.coshell.core.domain.feedback.FeedbackStatus
import kotlin.time.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedbackStatusDocumentTest {
    private fun doc(fields: String) = Json.parseToJsonElement("""{"name":"projects/p/databases/(default)/documents/feedback/abc","fields":{$fields}}""").jsonObject

    @Test
    fun `robot fields become a status`() {
        val status = feedbackStatus(
            doc(
                """"processed":{"booleanValue":true},"issueNumber":{"integerValue":"157"},"status":{"stringValue":"done"},
                "issueAt":{"timestampValue":"2026-11-10T15:46:10Z"},"inProgressAt":{"timestampValue":"2026-11-11T12:00:00.123456Z"},
                "doneAt":{"timestampValue":"2026-11-12T10:00:00Z"},"doneBuild":{"integerValue":"248"}""",
            ),
        )
        val expected = FeedbackStatus(
            FeedbackStage.DONE, 157, Instant.parse("2026-11-10T15:46:10Z"), Instant.parse("2026-11-11T12:00:00.123456Z"),
            Instant.parse("2026-11-12T10:00:00Z"), 248,
        )
        assertEquals(expected, status)
    }

    @Test
    fun `without a status field it is sent or received`() {
        assertEquals(FeedbackStage.SENT, feedbackStatus(doc(""""processed":{"booleanValue":false}""")).stage)
        val received = feedbackStatus(doc(""""processed":{"booleanValue":true},"issueNumber":{"integerValue":"9"}"""))
        assertEquals(FeedbackStatus(FeedbackStage.RECEIVED, 9), received)
    }

    @Test
    fun `document id comes from the create response`() {
        assertEquals("abc", feedbackDocumentId(doc("")))
        assertEquals("", feedbackDocumentId(Json.parseToJsonElement("{}").jsonObject))
    }
}
