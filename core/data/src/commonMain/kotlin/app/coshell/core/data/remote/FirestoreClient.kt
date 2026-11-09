package app.coshell.core.data.remote

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/** Supplies a fresh Firebase ID token for `Authorization: Bearer`. */
fun interface IdTokenProvider {
    suspend fun idToken(): String
}

/**
 * Thin Firestore REST v1 client: document GET, `commit` and `runQuery`.
 * [host] is `https://firestore.googleapis.com` or the emulator (`http://localhost:8080`).
 */
class FirestoreClient(
    private val http: HttpClient,
    private val projectId: String,
    private val tokens: IdTokenProvider,
    private val host: String = PRODUCTION_HOST,
) {
    /** `projects/p/databases/(default)/documents` — prefix of every document name. */
    val root = "projects/$projectId/databases/(default)/documents"
    private val base = "$host/v1/$root"

    /** Full resource name of a document path like `families/main`. */
    fun name(path: String) = "$root/$path"

    /** The document's JSON, or `null` if it does not exist. */
    suspend fun get(path: String): JsonObject? {
        val response = call("GET", "$base/$path", null)
        if (response.code == 404) return null
        return FirestoreJson.parseToJsonElement(response.orThrow().body).jsonObject
    }

    /** Atomic batch of writes (`[{"update": ..., "updateTransforms": ...}]`). */
    suspend fun commit(writes: List<JsonObject>) {
        val body = JsonObject(mapOf("writes" to kotlinx.serialization.json.JsonArray(writes)))
        call("POST", "$base:commit", body.toString()).orThrow()
    }

    /** Runs a structured query under [parentPath] (`""` — database root); returns found documents. */
    suspend fun runQuery(parentPath: String, structuredQuery: JsonObject): List<JsonObject> {
        val url = if (parentPath.isEmpty()) "$base:runQuery" else "$base/$parentPath:runQuery"
        val body = JsonObject(mapOf("structuredQuery" to structuredQuery))
        val response = call("POST", url, body.toString()).orThrow()
        return FirestoreJson.parseToJsonElement(response.body).asArray()
            .mapNotNull { (it as? JsonObject)?.get("document") as? JsonObject }
    }

    private suspend fun call(method: String, url: String, body: String?): HttpResponse =
        http.send(HttpRequest(method, url, body, mapOf("Authorization" to "Bearer ${tokens.idToken()}")))

    companion object {
        const val PRODUCTION_HOST = "https://firestore.googleapis.com"
    }
}

internal fun JsonElement.obj(): JsonObject = jsonObject
