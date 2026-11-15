package app.coshell.core.data.sync

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * The record payload as a JSON object (#340). Every payload carries the sync schema number [KEY]
 * (`v`) — #334 compares it to tell that the other phone runs a newer, incompatible version. It lives
 * inside `json`, not as a document field: the Firestore rules allow only a fixed set of fields.
 *
 * Keys this version does not know are kept aside on pull ([unknownKeys]) and merged back on push
 * ([withExtras]), so an older app editing a record of a newer one does not drop the newer fields.
 */
object SyncPayload {
    /** Sync schema of this app version. Raise it when the payload changes incompatibly. */
    const val SCHEMA = 2

    /** A payload without the key was written before #340 — schema 1. */
    const val KEY = "v"

    private val json = Json

    /** Sync schema of a payload; 1 when it has no number (written before #340). */
    fun schemaOf(payload: String): Int =
        runCatching { parse(payload)[KEY]?.jsonPrimitive?.intOrNull }.getOrNull() ?: 1

    /** [payload] with this version's schema number. */
    fun stamp(payload: String): String = withExtras(payload, null)

    /**
     * [own] (this version's fields) over [extras] (unknown fields saved on pull), stamped with
     * [SCHEMA]: own values win, unknown keys stay.
     */
    fun withExtras(own: String, extras: String?): String {
        val saved = extras?.let(::parse).orEmpty()
        return JsonObject(saved + parse(own) + (KEY to JsonPrimitive(SCHEMA))).toString()
    }

    /**
     * Keys of [remote] that are not in [known] (and not the schema number), as a JSON object;
     * `null` when there are none.
     */
    fun unknownKeys(remote: String, known: Set<String>): String? {
        val unknown = parse(remote).filterKeys { it != KEY && it !in known }
        return if (unknown.isEmpty()) null else JsonObject(unknown).toString()
    }

    /** Top-level keys of a payload. */
    fun keys(payload: String): Set<String> = parse(payload).keys

    private fun parse(payload: String): JsonObject = json.parseToJsonElement(payload).jsonObject
}
