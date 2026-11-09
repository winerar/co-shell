package app.coshell.rates.data

import app.coshell.core.data.rates.HttpGet
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** [HttpGet] on plain [HttpURLConnection]: no HTTP library, small APK (performance.md). */
class UrlConnectionHttpGet(private val timeoutMs: Int = 15_000) : HttpGet {

    override suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = timeoutMs
            connection.readTimeout = timeoutMs
            connection.setRequestProperty("Accept", "application/json")
            check(connection.responseCode == HttpURLConnection.HTTP_OK) { "HTTP ${connection.responseCode} for $url" }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
