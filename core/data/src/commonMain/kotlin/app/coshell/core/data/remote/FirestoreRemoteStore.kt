package app.coshell.core.data.remote

import app.coshell.core.data.sync.RemoteRecord
import app.coshell.core.data.sync.RemoteStore
import app.coshell.core.data.sync.SyncKind
import app.coshell.core.data.sync.SyncRecord
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Family records in Firestore: one collection `families/{familyId}/records`, one document per
 * row (`transaction_<id>`), so a pull is a single query and an idle pull costs one read.
 * `serverUpdatedAt` is set by the server (`REQUEST_TIME`) and drives the pull cursor; conflict
 * resolution uses the phone's `updatedAt` (see SyncEngine). Field list must match firestore.rules.
 */
class FirestoreRemoteStore(
    private val client: FirestoreClient,
    private val familyId: String,
) : RemoteStore {
    private val collection get() = "families/$familyId/records"

    override suspend fun pull(cursor: String?): List<RemoteRecord> {
        val result = mutableListOf<RemoteRecord>()
        var after = cursor?.let { Position.parse(it) } // end of the previous pull or page
        val since = cursor?.toLongOrNull() // cursor saved by an older app version: epoch ms
        while (true) {
            val page = client.runQuery("families/$familyId", query(since, after))
            page.mapNotNullTo(result, ::parse)
            if (page.size < PAGE) return result
            after = Position.of(page.last()) ?: return result
        }
    }

    override suspend fun push(records: List<SyncRecord>) {
        if (records.isEmpty()) return
        client.commit(records.map(::write))
    }

    private fun query(since: Long?, after: Position?) = buildJsonObject {
        put("from", buildJsonArray { add(buildJsonObject { put("collectionId", "records") }) })
        if (since != null && after == null) {
            put("where", buildJsonObject {
                put("fieldFilter", buildJsonObject {
                    put("field", fieldRef(SERVER_TIME))
                    put("op", "GREATER_THAN_OR_EQUAL")
                    put("value", fsTimestamp(since))
                })
            })
        }
        put("orderBy", buildJsonArray {
            add(buildJsonObject { put("field", fieldRef(SERVER_TIME)); put("direction", "ASCENDING") })
            add(buildJsonObject { put("field", fieldRef("__name__")); put("direction", "ASCENDING") })
        })
        if (after != null) {
            // Strictly after (time, name) of the last document read — no skips, no repeats.
            put("startAt", buildJsonObject {
                put("values", JsonArray(listOf(
                    buildJsonObject { put("timestampValue", after.time) },
                    buildJsonObject { put("referenceValue", after.name) },
                )))
                put("before", false)
            })
        }
        put("limit", PAGE)
    }

    private fun fieldRef(path: String) = buildJsonObject { put("fieldPath", path) }

    private fun write(r: SyncRecord) = buildJsonObject {
        put("update", buildJsonObject {
            put("name", client.name("$collection/${r.key}"))
            put("fields", JsonObject(mapOf(
                "kind" to fsString(r.kind.wire),
                "id" to fsString(r.id),
                "updatedAt" to fsInt(r.updatedAt),
                "updatedBy" to fsString(r.updatedBy),
                "deleted" to fsBool(r.deleted),
                "json" to fsString(r.json),
            )))
        })
        put("updateTransforms", buildJsonArray {
            add(buildJsonObject { put("fieldPath", SERVER_TIME); put("setToServerValue", "REQUEST_TIME") })
        })
    }

    private fun parse(document: JsonObject): RemoteRecord? {
        val f = FsFields.of(document)
        val kind = f.string("kind")?.let(SyncKind::fromWire) ?: return null // newer app version
        val record = SyncRecord(
            kind = kind,
            id = f.string("id") ?: return null,
            updatedAt = f.long("updatedAt") ?: return null,
            updatedBy = f.string("updatedBy"),
            deleted = f.bool("deleted") ?: false,
            json = f.string("json") ?: return null,
        )
        return RemoteRecord(record, Position.of(document)?.toString() ?: return null)
    }

    /**
     * A place in the `(serverUpdatedAt, __name__)` order. The time is kept exactly as the server
     * sent it (microseconds): rounding to ms would make the last records look "after" the cursor.
     */
    private data class Position(val time: String, val name: String) {
        override fun toString() = "$time $name"

        companion object {
            fun parse(cursor: String): Position? =
                cursor.split(' ', limit = 2).takeIf { it.size == 2 }?.let { Position(it[0], it[1]) }

            fun of(document: JsonObject): Position? {
                val time = document["fields"]?.obj()?.get(SERVER_TIME)?.obj()?.get("timestampValue")
                    ?.jsonPrimitive?.content ?: return null
                return Position(time, document["name"]?.jsonPrimitive?.content ?: return null)
            }
        }
    }

    private companion object {
        const val SERVER_TIME = "serverUpdatedAt"
        const val PAGE = 500
    }
}

