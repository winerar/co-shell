package app.coshell.feedback.data

import app.coshell.core.domain.feedback.FeedbackReport
import app.coshell.core.domain.feedback.FeedbackSender
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Adds a document to the Firestore collection `feedback` through the REST API.
 * No Firebase SDK and no sign-in: security rules allow anyone only to *create* a well-formed
 * document there (`firebase/firestore.rules`). [apiKey] is the project's public Web API key.
 * Blank [projectId] or [apiKey] (local builds) means feedback is not configured. Returns the new document's id.
 * Attached files go first, as base64 parts in `feedback_files` (one document each); the message lists their ids.
 */
class FirestoreFeedbackSender(private val projectId: String, private val apiKey: String) : FeedbackSender {

    override val isConfigured get() = projectId.isNotBlank() && apiKey.isNotBlank()

    override suspend fun send(report: FeedbackReport) = withContext(Dispatchers.IO) {
        val files = report.attachments.flatMapIndexed { index, file ->
            val parts = file.chunks()
            parts.mapIndexed { part, data -> create("feedback_files", feedbackFileDocument(file, index, part, parts.size, data, report.createdAt)) }
        }
        create("feedback", feedbackDocument(report, files))
    }

    /** Adds a document to [collection]; returns its id (empty if the answer has none). */
    private fun create(collection: String, document: JsonObject): String {
        val key = URLEncoder.encode(apiKey, "UTF-8")
        val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/$collection?key=$key"
        val body = document.toString().toByteArray()
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "POST"
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.outputStream.use { it.write(body) }
            val code = connection.responseCode
            check(code == HttpURLConnection.HTTP_OK) { "HTTP $code" }
            val created = connection.inputStream.bufferedReader().use { it.readText() }
            runCatching { feedbackDocumentId(Json.parseToJsonElement(created).jsonObject) }.getOrDefault("")
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val TIMEOUT_MS = 15_000
    }
}
