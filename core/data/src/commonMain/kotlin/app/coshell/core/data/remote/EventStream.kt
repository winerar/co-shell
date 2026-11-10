package app.coshell.core.data.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A long-lived HTTP GET with `Accept: text/event-stream` (Server-Sent Events). The Android
 * implementation lives in `app`. [lines] emits the response body line by line while the server
 * keeps the connection open, completes when it closes, throws [ApiException] for an HTTP error
 * and an ordinary exception for a network failure.
 */
fun interface EventStreamClient {
    fun lines(url: String): Flow<String>
}

/** One SSE message: `event: put` + `data: {...}`. */
data class ServerSentEvent(val event: String, val data: String)

/**
 * Groups raw SSE lines into events: `event:`/`data:` fields, a blank line ends an event,
 * several `data:` lines are joined with `\n`, comments (`:`) and unknown fields are skipped.
 */
fun Flow<String>.serverSentEvents(): Flow<ServerSentEvent> = flow {
    var event = ""
    val data = StringBuilder()
    var hasData = false
    collect { raw ->
        val line = raw.removeSuffix("\r")
        if (line.isEmpty()) {
            if (event.isNotEmpty() || hasData) emit(ServerSentEvent(event.ifEmpty { "message" }, data.toString()))
            event = ""; data.clear(); hasData = false
            return@collect
        }
        if (line.startsWith(":")) return@collect
        val name = line.substringBefore(':')
        val value = line.substringAfter(':', "").removePrefix(" ")
        when (name) {
            "event" -> event = value
            "data" -> { if (hasData) data.append('\n'); data.append(value); hasData = true }
        }
    }
}
