package app.coshell.core.data.sync

/** In-memory server: keeps the last pushed record per key and stamps it with a growing server time. */
class FakeRemoteStore : RemoteStore {
    private val docs = linkedMapOf<String, RemoteRecord>()
    private var serverClock = 1_000L
    var failNextPush = false
    var failNextPull = false
    /** Called after a push is stored, before the client clears its flags. */
    var afterPush: suspend () -> Unit = {}
    var pullCalls = 0
        private set

    val records: Collection<RemoteRecord> get() = docs.values

    override suspend fun pull(since: Long?): List<RemoteRecord> {
        pullCalls++
        if (failNextPull) { failNextPull = false; error("offline") }
        return docs.values.filter { since == null || it.serverTime >= since }.sortedBy { it.serverTime }
    }

    override suspend fun push(records: List<SyncRecord>) {
        if (failNextPush) { failNextPush = false; error("offline") }
        serverClock += 10
        records.forEach { docs[it.key] = RemoteRecord(it, serverClock) }
        afterPush()
    }
}
