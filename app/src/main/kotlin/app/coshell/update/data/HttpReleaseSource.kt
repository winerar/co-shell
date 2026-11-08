package app.coshell.update.data

import app.coshell.core.domain.update.AppRelease
import app.coshell.core.domain.update.ReleaseSource
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads `version.json` from [baseUrl] (latest release of the public releases repo, set at build time).
 * GitHub answers with a redirect to its file storage; [HttpURLConnection] follows it (https → https).
 * A blank [baseUrl] means updates are not configured for this build.
 */
class HttpReleaseSource(private val baseUrl: String) : ReleaseSource {

    override suspend fun latestRelease(): AppRelease? {
        if (baseUrl.isBlank()) return null
        val url = baseUrl.trimEnd('/') + "/version.json"
        val text = withContext(Dispatchers.IO) {
            val connection = URL(url).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS
                connection.useCaches = false
                check(connection.responseCode == HttpURLConnection.HTTP_OK) { "HTTP ${connection.responseCode}" }
                connection.inputStream.bufferedReader().use { it.readText() }
            } finally {
                connection.disconnect()
            }
        }
        return parseReleaseManifest(text, url)
    }

    private companion object {
        const val TIMEOUT_MS = 15_000
    }
}
