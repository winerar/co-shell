package app.coshell.feedback.data

import app.coshell.core.domain.feedback.DeviceInfo
import app.coshell.core.domain.feedback.FeedbackAttachment
import app.coshell.core.domain.feedback.FeedbackReport
import kotlin.time.Instant
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FeedbackFilesDocumentTest {
    private val now = Instant.parse("2026-11-20T10:00:00Z")
    private val report = FeedbackReport("Формат чека: Teleshka", now, DeviceInfo("1.0", 1, "Pixel", "Android 16", "id"))

    private fun fields(json: kotlinx.serialization.json.JsonObject) = json["fields"]!!.jsonObject

    @Test
    fun `no files - no attachments field, so older rules accept it`() {
        assertFalse("attachments" in fields(feedbackDocument(report)))
    }

    @Test
    fun `file ids are listed in order`() {
        val values = fields(feedbackDocument(report, listOf("a", "b")))["attachments"]!!.jsonObject["arrayValue"]!!.jsonObject["values"]!!.jsonArray
        assertEquals(listOf("a", "b"), values.map { it.jsonObject["stringValue"]!!.jsonPrimitive.content })
    }

    @Test
    fun `a file part carries its name, place and data`() {
        val file = FeedbackAttachment("/x/chek.pdf", "application/pdf", byteArrayOf(1))
        val doc = fields(feedbackFileDocument(file, index = 1, part = 0, parts = 2, data = "AQ==", createdAt = now))
        assertEquals("chek.pdf", doc["name"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
        assertEquals("1", doc["file"]!!.jsonObject["integerValue"]!!.jsonPrimitive.content)
        assertEquals("2", doc["parts"]!!.jsonObject["integerValue"]!!.jsonPrimitive.content)
        assertEquals("AQ==", doc["data"]!!.jsonObject["stringValue"]!!.jsonPrimitive.content)
    }
}
