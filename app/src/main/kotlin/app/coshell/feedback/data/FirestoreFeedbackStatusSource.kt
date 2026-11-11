package app.coshell.feedback.data

import app.coshell.core.domain.feedback.FeedbackStatus
import app.coshell.core.domain.feedback.FeedbackStatusSource
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

/**
 * Reads one `feedback` document by its id through the REST API (rules allow `get`, not `list`) and
 * takes the status the robot wrote there. One read per message; any failure is «unknown now».
 */
class FirestoreFeedbackStatusSource(private val projectId: String, private val apiKey: String) : FeedbackStatusSource {

    override suspend fun status(documentId: String): FeedbackStatus? {
        if (projectId.isBlank() || apiKey.isBlank() || documentId.isBlank()) return null
        return withContext(Dispatchers.IO) {
            try {
                val key = URLEncoder.encode(apiKey, "UTF-8")
                val id = URLEncoder.encode(documentId, "UTF-8")
                val url = "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents/feedback/$id?key=$key"
                val connection = URL(url).openConnection() as HttpURLConnection
                try {
                    connection.connectTimeout = TIMEOUT_MS
                    connection.readTimeout = TIMEOUT_MS
                    if (connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
                    val body = connection.inputStream.bufferedReader().use { it.readText() }
                    feedbackStatus(Json.parseToJsonElement(body).jsonObject)
                } finally {
                    connection.disconnect()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
        }
    }

    private companion object {
        const val TIMEOUT_MS = 15_000
    }
}
