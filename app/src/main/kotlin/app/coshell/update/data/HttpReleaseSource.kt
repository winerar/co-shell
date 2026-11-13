package app.coshell.update.data

import app.coshell.core.domain.update.AppRelease
import app.coshell.core.domain.update.ReleaseHistorySource
import app.coshell.core.domain.update.ReleaseNote
import app.coshell.core.domain.update.ReleaseSource
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads `version.json` (and `releases.json` for «Прошлые версии», #284) from [baseUrl] (latest release of the public releases repo, set at build time).
 * GitHub answers with a redirect to its file storage; [HttpURLConnection] follows it (https → https).
 * A blank [baseUrl] means updates are not configured for this build.
 */
class HttpReleaseSource(private val baseUrl: String) : ReleaseSource, ReleaseHistorySource {

    override suspend fun latestRelease(): AppRelease? {
        if (baseUrl.isBlank()) return null
        val url = baseUrl.trimEnd('/') + "/version.json"
        return parseReleaseManifest(fetch(url), url)
    }

    override suspend fun history(): List<ReleaseNote> {
        if (baseUrl.isBlank()) return emptyList()
        return parseReleaseHistory(fetch(baseUrl.trimEnd('/') + "/releases.json"))
    }

    private suspend fun fetch(url: String): String =
        withContext(Dispatchers.IO) {
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

    private companion object {
        const val TIMEOUT_MS = 15_000
    }
}
