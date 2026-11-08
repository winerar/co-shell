package app.coshell.feedback

import app.coshell.core.domain.feedback.DeviceInfo
import app.coshell.core.domain.feedback.FeedbackReport
import app.coshell.feedback.data.feedbackDocument
import kotlin.time.Instant
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedbackDocumentTest {
    @Test
    fun `report becomes a firestore document`() {
        val report = FeedbackReport(
            text = "Текст \"с кавычками\"",
            createdAt = Instant.parse("2026-11-08T10:00:00Z"),
            device = DeviceInfo("0.1.0", 27, "Google Pixel 8", "Android 15 (API 35)", "id-1"),
        )
        val expected = """
            {"fields":{
              "text":{"stringValue":"Текст \"с кавычками\""},
              "createdAt":{"timestampValue":"2026-11-08T10:00:00Z"},
              "appVersion":{"stringValue":"0.1.0"},
              "versionCode":{"integerValue":"27"},
              "device":{"stringValue":"Google Pixel 8"},
              "os":{"stringValue":"Android 15 (API 35)"},
              "installationId":{"stringValue":"id-1"},
              "processed":{"booleanValue":false}
            }}
        """.trimIndent()
        assertEquals(Json.parseToJsonElement(expected), feedbackDocument(report))
    }
}
