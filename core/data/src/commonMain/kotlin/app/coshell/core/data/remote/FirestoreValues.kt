package app.coshell.core.data.remote

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.time.Instant

// Firestore REST typed values: {"stringValue": "x"}, {"integerValue": "1"}, ...
// https://firebase.google.com/docs/firestore/reference/rest/v1/Value

internal val FirestoreJson = Json { ignoreUnknownKeys = true }

internal fun fsString(v: String?): JsonObject =
    if (v == null) fsNull() else buildJsonObject { put("stringValue", v) }
internal fun fsInt(v: Long): JsonObject = buildJsonObject { put("integerValue", v.toString()) }
internal fun fsBool(v: Boolean): JsonObject = buildJsonObject { put("booleanValue", v) }
internal fun fsNull(): JsonObject = buildJsonObject { put("nullValue", JsonNull) }
internal fun fsTimestamp(epochMs: Long): JsonObject =
    buildJsonObject { put("timestampValue", Instant.fromEpochMilliseconds(epochMs).toString()) }
internal fun fsArray(values: List<JsonObject>): JsonObject =
    buildJsonObject { put("arrayValue", buildJsonObject { put("values", JsonArray(values)) }) }
internal fun fsMap(fields: Map<String, JsonObject>): JsonObject =
    buildJsonObject { put("mapValue", buildJsonObject { put("fields", JsonObject(fields)) }) }

/** Reads typed values back; missing or null fields give `null`. */
internal class FsFields(private val fields: JsonObject) {
    private fun value(name: String, type: String): JsonElement? = fields[name]?.jsonObject?.get(type)

    fun string(name: String): String? = value(name, "stringValue")?.jsonPrimitive?.contentOrNull
    fun long(name: String): Long? = value(name, "integerValue")?.jsonPrimitive?.contentOrNull?.toLongOrNull()
    fun bool(name: String): Boolean? = value(name, "booleanValue")?.jsonPrimitive?.booleanOrNull
    fun timestampMs(name: String): Long? =
        value(name, "timestampValue")?.jsonPrimitive?.contentOrNull?.let { Instant.parse(it).toEpochMilliseconds() }
    fun strings(name: String): List<String> =
        (value(name, "arrayValue")?.jsonObject?.get("values") as? JsonArray).orEmpty()
            .mapNotNull { it.jsonObject["stringValue"]?.jsonPrimitive?.contentOrNull }
    fun map(name: String): FsFields? =
        value(name, "mapValue")?.jsonObject?.get("fields")?.jsonObject?.let(::FsFields)
    fun keys(): Set<String> = fields.keys

    companion object {
        fun of(document: JsonObject) = FsFields(document["fields"]?.jsonObject ?: JsonObject(emptyMap()))
    }
}

/** Throws [ApiException] for a non-2xx answer, using Google's `{"error": {...}}` body when present. */
internal fun HttpResponse.orThrow(): HttpResponse {
    if (isSuccess) return this
    val error = runCatching {
        // runQuery returns errors as an array with one element.
        val root = FirestoreJson.parseToJsonElement(body)
        val obj = if (root is JsonArray) root.first().jsonObject else root.jsonObject
        obj["error"]?.jsonObject
    }.getOrNull()
    val status = (error?.get("status") as? JsonPrimitive)?.contentOrNull
    val message = (error?.get("message") as? JsonPrimitive)?.contentOrNull ?: body.take(200)
    throw ApiException(code, status, message)
}

internal fun JsonElement.asArray(): JsonArray = jsonArray
