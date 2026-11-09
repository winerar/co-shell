package app.coshell.family.data

import app.coshell.core.data.remote.HttpClient
import app.coshell.core.data.remote.HttpRequest
import app.coshell.core.data.remote.HttpResponse
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** [HttpClient] on plain [HttpURLConnection] for Firestore and Firebase Auth REST (no SDKs, small APK). */
class UrlConnectionHttpClient(private val timeoutMs: Int = 20_000) : HttpClient {

    override suspend fun send(request: HttpRequest): HttpResponse = withContext(Dispatchers.IO) {
        val connection = URL(request.url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = request.method
            connection.connectTimeout = timeoutMs
            connection.readTimeout = timeoutMs
            connection.setRequestProperty("Accept", "application/json")
            request.headers.forEach { (name, value) -> connection.setRequestProperty(name, value) }
            if (request.body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", request.contentType)
                connection.outputStream.use { it.write(request.body!!.toByteArray()) }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            HttpResponse(code, stream?.bufferedReader()?.use { it.readText() }.orEmpty())
        } finally {
            connection.disconnect()
        }
    }
}
