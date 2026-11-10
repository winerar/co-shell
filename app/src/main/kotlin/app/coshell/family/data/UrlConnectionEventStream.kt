package app.coshell.family.data

import app.coshell.core.data.remote.ApiException
import app.coshell.core.data.remote.EventStreamClient
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.job

/**
 * [EventStreamClient] on [HttpURLConnection] (the Realtime Database stream for the sync bell).
 * The server sends `keep-alive` every 30 s, so a read that waits longer than [readTimeoutMs] means
 * the connection is dead and the flow fails (the caller reconnects). Cancelling the collector
 * disconnects at once instead of waiting for the next line.
 */
class UrlConnectionEventStream(
    private val connectTimeoutMs: Int = 20_000,
    private val readTimeoutMs: Int = 75_000,
) : EventStreamClient {

    override fun lines(url: String): Flow<String> = flow {
        val connection = URL(url).openConnection() as HttpURLConnection
        val onCancel = currentCoroutineContext().job.invokeOnCompletion { connection.disconnect() }
        try {
            connection.connectTimeout = connectTimeoutMs
            connection.readTimeout = readTimeoutMs
            connection.setRequestProperty("Accept", "text/event-stream")
            val code = connection.responseCode
            if (code !in 200..299) {
                val error = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw ApiException(code, null, error.take(200))
            }
            connection.inputStream.bufferedReader().use { reader ->
                while (true) emit(reader.readLine() ?: break)
            }
        } finally {
            onCancel.dispose()
            connection.disconnect()
        }
    }.flowOn(Dispatchers.IO)
}
