package app.coshell.feedback.data

import app.coshell.core.domain.feedback.FeedbackReport
import app.coshell.core.domain.feedback.FeedbackSender
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Adds a document to the Firestore collection `feedback` through the REST API.
 * No Firebase SDK and no sign-in: security rules allow anyone only to *create* a well-formed
 * document there (`firebase/firestore.rules`). [apiKey] is the project's public Web API key.
 * Blank [projectId] or [apiKey] (local builds) means feedback is not configured.
 */
class FirestoreFeedbackSender(private val projectId: String, private val apiKey: String) : FeedbackSender {

    override val isConfigured get() = projectId.isNotBlank() && apiKey.isNotBlank()

    override suspend fun send(report: FeedbackReport) = withContext(Dispatchers.IO) {
        val key = URLEncoder.encode(apiKey, "UTF-8")
        val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/feedback?key=$key"
        val body = feedbackDocument(report).toString().toByteArray()
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.outputStream.use { it.write(body) }
            val code = connection.responseCode
            check(code == HttpURLConnection.HTTP_OK) { "HTTP $code" }
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val TIMEOUT_MS = 15_000
    }
}
