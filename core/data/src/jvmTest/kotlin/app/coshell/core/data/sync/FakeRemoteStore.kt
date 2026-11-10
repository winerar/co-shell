package app.coshell.core.data.sync

/**
 * In-memory server: keeps the last pushed record per key and stamps it with a growing server time.
 * Like Firestore, every record of one push shares one server time; order is (time, key).
 */
class FakeRemoteStore : RemoteStore {
    private val docs = linkedMapOf<String, RemoteRecord>()
    private var serverClock = 1_000L
    var failNextPush = false
    var failNextPull = false
    /** The next pull throws this (e.g. an `ApiException` with a server status). */
    var failNextPullWith: Throwable? = null
    /** Called after a push is stored, before the client clears its flags. */
    var afterPush: suspend () -> Unit = {}
    var pullCalls = 0
        private set

    val records: Collection<RemoteRecord> get() = docs.values

    /** How many records all pulls returned — the Firestore read bill. */
    var recordsRead = 0
        private set

    override suspend fun pull(cursor: String?): List<RemoteRecord> {
        pullCalls++
        if (failNextPull) { failNextPull = false; error("offline") }
        failNextPullWith?.let { failNextPullWith = null; throw it }
        return docs.values.filter { cursor == null || it.cursor > cursor }.sortedBy { it.cursor }
            .also { recordsRead += it.size }
    }

    override suspend fun push(records: List<SyncRecord>) {
        if (failNextPush) { failNextPush = false; error("offline") }
        serverClock += 10
        records.forEach { docs[it.key] = RemoteRecord(it, "%015d %s".format(serverClock, it.key)) }
        afterPush()
    }
}
