package app.coshell.feedback.data

import app.coshell.core.domain.feedback.FeedbackStage
import app.coshell.core.domain.feedback.FeedbackStatus
import kotlin.time.Instant
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * A Firestore REST `feedback` document → [FeedbackStatus]. The status fields are written by the robot
 * (`scripts/feedback_status.py`, keep names in sync); a message not yet turned into an issue has none.
 */
internal fun feedbackStatus(document: JsonObject): FeedbackStatus {
    val fields = document["fields"] as? JsonObject ?: JsonObject(emptyMap())
    fun value(name: String, type: String) = (fields[name] as? JsonObject)?.get(type)?.jsonPrimitive
    fun time(name: String) = value(name, "timestampValue")?.content?.let { runCatching { Instant.parse(it) }.getOrNull() }
    fun int(name: String) = value(name, "integerValue")?.content?.toIntOrNull()
    val stage = when (value("status", "stringValue")?.content) {
        "received" -> FeedbackStage.RECEIVED
        "in_progress" -> FeedbackStage.IN_PROGRESS
        "done" -> FeedbackStage.DONE
        "later" -> FeedbackStage.LATER
        "closed" -> FeedbackStage.CLOSED
        else -> if (value("processed", "booleanValue")?.booleanOrNull == true) FeedbackStage.RECEIVED else FeedbackStage.SENT
    }
    return FeedbackStatus(stage, int("issueNumber"), time("issueAt"), time("inProgressAt"), time("doneAt"), int("doneBuild"))
}

/** The id from a create response (`"name": "projects/…/documents/feedback/<id>"`); empty if absent. */
internal fun feedbackDocumentId(created: JsonObject): String =
    created["name"]?.jsonPrimitive?.content?.substringAfterLast('/').orEmpty()
