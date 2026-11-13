package app.coshell.core.data.sync

/**
 * One synced row as it travels to and from the server. [json] is the Room entity serialized by
 * [SyncCodec] (without `pendingSync`); the other fields are copied out so that the server and
 * security rules can see them without parsing the payload.
 */
data class SyncRecord(
    val kind: SyncKind,
    val id: String,
    val updatedAt: Long,
    val updatedBy: String?,
    val deleted: Boolean,
    val json: String,
) {
    /** Document id on the server: `transaction_<uuid>`; ids never contain `/`. */
    val key: String get() = "${kind.wire}_$id"
}

/**
 * A record read from the server. [cursor] is an opaque position right after this record in the
 * server's order; passing it back to [RemoteStore.pull] returns only records stored later.
 */
data class RemoteRecord(val record: SyncRecord, val cursor: String)

/** Synced tables. [wire] names are stored on the server — never rename them. */
enum class SyncKind(val wire: String) {
    ACCOUNT("account"),
    CATEGORY("category"),
    TRANSACTION("transaction"),
    BUDGET("budget"),
    MANUAL_RATE("manualRate"),
    TEMPLATE("template"),
    SETTING("setting"),
    TAG("tag"),
    ;

    companion object {
        /** `null` for kinds from a newer app version — they are skipped, not fatal. */
        fun fromWire(wire: String): SyncKind? = entries.firstOrNull { it.wire == wire }
    }
}

/**
 * Server side of sync. The Firestore implementation lives in [FirestoreRemoteStore]; tests use
 * an in-memory fake. Both calls throw on network or permission errors.
 */
interface RemoteStore {
    /**
     * Records stored after [cursor] (a [RemoteRecord.cursor] from an earlier pull), oldest first;
     * `null` — everything. Records already read are never returned again, so an idle pull is cheap.
     */
    suspend fun pull(cursor: String?): List<RemoteRecord>

    /** Stores [records], replacing what the server had under the same [SyncRecord.key]. */
    suspend fun push(records: List<SyncRecord>)
}
