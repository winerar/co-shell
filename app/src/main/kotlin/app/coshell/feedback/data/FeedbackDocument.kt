package app.coshell.feedback.data

import app.coshell.core.domain.feedback.FeedbackReport
import app.coshell.core.domain.feedback.storedText
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * [FeedbackReport] as a Firestore REST document (`{"fields": {"text": {"stringValue": …}, …}}`).
 * Field names and types must match `firebase/firestore.rules` and `scripts/feedback_to_issues.py`.
 * `processed` is set to true by the GitHub Action once it has created an issue.
 */
internal fun feedbackDocument(report: FeedbackReport): JsonObject = buildJsonObject {
    putJsonObject("fields") {
        string("text", report.storedText())
        putJsonObject("createdAt") { put("timestampValue", report.createdAt.toString()) }
        string("appVersion", report.device.appVersion)
        putJsonObject("versionCode") { put("integerValue", report.device.versionCode.toString()) }
        string("device", report.device.device)
        string("os", report.device.os)
        string("installationId", report.device.installationId)
        putJsonObject("processed") { put("booleanValue", false) }
    }
}

private fun kotlinx.serialization.json.JsonObjectBuilder.string(name: String, value: String) =
    putJsonObject(name) { put("stringValue", value) }
