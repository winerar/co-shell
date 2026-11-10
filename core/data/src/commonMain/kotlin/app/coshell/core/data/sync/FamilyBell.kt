package app.coshell.core.data.sync

import app.coshell.core.data.auth.AuthGateway
import app.coshell.core.data.remote.ApiException
import app.coshell.core.data.remote.EventStreamClient
import app.coshell.core.data.remote.HttpClient
import app.coshell.core.data.remote.HttpRequest
import app.coshell.core.data.remote.HttpResponse
import app.coshell.core.data.remote.ServerSentEvent
import app.coshell.core.data.remote.serverSentEvents
import app.coshell.core.domain.family.FamilyRemote.Companion.FAMILY_ID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.coroutines.cancellation.CancellationException

/** «Something changed, pull now» for the other phones. [None] — sync is not set up. */
fun interface ChangeBell {
    suspend fun ring()

    companion object {
        val None = ChangeBell {}
    }
}

/**
 * Instant sync while the app is open (docs/ARCHITECTURE.md §10, «Звонок»). Firestore REST has no
 * listener, so after sending its edits a phone writes `{at, by}` to `families/main/ping` in the
 * Realtime Database, and the other phone, holding an SSE stream on that node, pulls at once.
 * The ping carries no budget data. Access: `families/main/members/{uid}` in the RTDB
 * (`firebase/database.rules.json`) — the first member adds themselves, members add the others.
 *
 * [databaseUrl] blank — the bell is off (polling still works).
 */
class FamilyBell(
    private val http: HttpClient,
    private val stream: EventStreamClient,
    databaseUrl: String,
    private val auth: AuthGateway,
    familyId: String = FAMILY_ID,
) : ChangeBell {
    private val enabled = databaseUrl.isNotBlank()
    private val base = "${databaseUrl.trimEnd('/')}/families/$familyId"

    /** Tells the other phones to pull. Throws [ApiException] if the database refused. */
    override suspend fun ring() {
        val me = auth.uid ?: return
        if (!enabled) return
        val body = JsonObject(mapOf("at" to JsonObject(mapOf(".sv" to JsonPrimitive("timestamp"))), "by" to JsonPrimitive(me)))
        send("PUT", "ping", body.toString()).orThrow()
    }

    /**
     * Lets every family member use the bell (adds only, never removes). Members add the others;
     * the very first one adds themselves while the list is empty. `false` — not allowed yet: this
     * phone waits until another member's phone registers it.
     */
    suspend fun register(memberUids: Set<String>): Boolean {
        val me = auth.uid ?: return false
        if (!enabled) return false
        val all = JsonObject((memberUids + me).associateWith { JsonPrimitive(true) }).toString()
        if (send("PATCH", "members", all).isSuccess) return true
        if (!send("PUT", "members/$me", "true").isSuccess) return false
        return send("PATCH", "members", all).isSuccess
    }

    /**
     * Emits whenever another member rings (also once on connect if the last ring was theirs —
     * a cheap catch-up). Registers [memberUids] and reconnects until cancelled: after the hourly
     * `auth_revoked` (ID token expired) at once, after errors with a growing pause up to 2 min.
     */
    fun rings(memberUids: Set<String>, onError: (Throwable) -> Unit = {}): Flow<Unit> {
        if (!enabled) return emptyFlow()
        return channelFlow {
            var pause = MIN_RETRY_MS
            while (true) {
                try {
                    register(memberUids)
                    val me = auth.uid
                    stream.lines("$base/ping.json?auth=${auth.idToken()}").serverSentEvents()
                        .takeWhile { it.event != "cancel" && it.event != "auth_revoked" }
                        .collect { event ->
                            pause = MIN_RETRY_MS
                            if (event.isRingFrom(me)) send(Unit)
                        }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    onError(e)
                    pause = (pause * 2).coerceAtMost(MAX_RETRY_MS)
                }
                delay(pause)
            }
        }
    }

    private suspend fun send(method: String, path: String, body: String): HttpResponse =
        http.send(HttpRequest(method, "$base/$path.json?auth=${auth.idToken()}", body))

    private fun HttpResponse.orThrow() {
        if (!isSuccess) throw ApiException(code, null, body.take(200))
    }

    private companion object {
        const val MIN_RETRY_MS = 1_000L
        const val MAX_RETRY_MS = 120_000L
    }
}

/**
 * Is this `put`/`patch` a ring by someone other than [me]? The stream sends
 * `{"path":"/","data":{"at":…,"by":"uid"}}`; an empty node (`data: null`) is not a ring;
 * a ring whose author cannot be read counts (one extra pull is cheap).
 */
internal fun ServerSentEvent.isRingFrom(me: String?): Boolean {
    if (event != "put" && event != "patch") return false
    val message = runCatching { Json.parseToJsonElement(data).jsonObject }.getOrNull() ?: return true
    val path = message["path"]?.jsonPrimitive?.contentOrNull
    val payload = message["data"] ?: JsonNull
    val by = when (path) {
        "/" -> {
            if (payload is JsonNull) return false
            (payload as? JsonObject)?.get("by")?.jsonPrimitive?.contentOrNull
        }
        "/by" -> (payload as? JsonPrimitive)?.contentOrNull
        else -> null
    }
    return by == null || by != me
}
