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

    override suspend fun pull(since: Long?): List<RemoteRecord> {
        val result = mutableListOf<RemoteRecord>()
        var after: JsonObject? = null // last document of the previous page
        while (true) {
            val page = client.runQuery("families/$familyId", query(since, after))
            page.mapNotNullTo(result, ::parse)
            if (page.size < PAGE) return result
            after = page.last()
        }
    }

    override suspend fun push(records: List<SyncRecord>) {
        if (records.isEmpty()) return
        client.commit(records.map(::write))
    }

    private fun query(since: Long?, after: JsonObject?) = buildJsonObject {
        put("from", buildJsonArray { add(buildJsonObject { put("collectionId", "records") }) })
        if (since != null) {
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
            // Page cursor: strictly after (time, name) of the last document — no skips, no repeats.
            val time = after["fields"]!!.obj()[SERVER_TIME]!!
            put("startAt", buildJsonObject {
                put("values", JsonArray(listOf(time, buildJsonObject { put("referenceValue", after["name"]!!.jsonPrimitive.content) })))
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
        return RemoteRecord(record, f.timestampMs(SERVER_TIME) ?: return null)
    }

    private companion object {
        const val SERVER_TIME = "serverUpdatedAt"
        const val PAGE = 500
    }
}

