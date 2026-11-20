package app.coshell.feedback.data

import app.coshell.core.domain.feedback.FeedbackAttachment
import app.coshell.core.domain.feedback.FeedbackReport
import app.coshell.core.domain.feedback.storedText
import kotlin.time.Instant
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * [FeedbackReport] as a Firestore REST document (`{"fields": {"text": {"stringValue": …}, …}}`).
 * Field names and types must match `firebase/firestore.rules` and `scripts/feedback_to_issues.py`.
 * `processed` is set to true by the GitHub Action once it has created an issue. [files] — ids of the `feedback_files`
 * documents of the attachments; the field is left out when there are none (rules of older deploys still accept it then).
 */
internal fun feedbackDocument(report: FeedbackReport, files: List<String> = emptyList()): JsonObject = buildJsonObject {
    putJsonObject("fields") {
        string("text", report.storedText())
        putJsonObject("createdAt") { put("timestampValue", report.createdAt.toString()) }
        string("appVersion", report.device.appVersion)
        putJsonObject("versionCode") { put("integerValue", report.device.versionCode.toString()) }
        string("device", report.device.device)
        string("os", report.device.os)
        string("installationId", report.device.installationId)
        putJsonObject("processed") { put("booleanValue", false) }
        if (files.isNotEmpty()) {
            putJsonObject("attachments") {
                putJsonObject("arrayValue") { putJsonArray("values") { files.forEach { id -> addJsonObject { put("stringValue", id) } } } }
            }
        }
    }
}

/**
 * One base64 part of an attachment (`feedback_files`): [file] — its number in the message, [part] of [parts].
 * Fields and limits must match `firebase/firestore.rules` and `scripts/feedback_to_issues.py`.
 */
internal fun feedbackFileDocument(file: FeedbackAttachment, index: Int, part: Int, parts: Int, data: String, createdAt: Instant): JsonObject =
    buildJsonObject {
        putJsonObject("fields") {
            string("name", FeedbackAttachment.cleanName(file.name))
            string("mime", file.mime.take(MAX_MIME))
            integer("file", index)
            integer("part", part)
            integer("parts", parts)
            string("data", data)
            putJsonObject("createdAt") { put("timestampValue", createdAt.toString()) }
        }
    }

private const val MAX_MIME = 100

private fun kotlinx.serialization.json.JsonObjectBuilder.integer(name: String, value: Int) =
    putJsonObject(name) { put("integerValue", value.toString()) }

private fun kotlinx.serialization.json.JsonObjectBuilder.string(name: String, value: String) =
    putJsonObject(name) { put("stringValue", value) }
