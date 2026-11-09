package app.coshell.core.data.sync

import app.coshell.core.data.db.CoShellDatabase
import app.coshell.core.data.db.createInMemoryCoShellDatabase
import app.coshell.core.data.repository.RoomAccountRepository
import app.coshell.core.data.repository.RoomTransactionRepository
import app.coshell.core.data.repository.WriteStamper
import kotlin.time.Clock
import kotlin.time.Instant

/** A test "phone": its own database, clock, member id and sync engine against a shared server. */
class Phone(member: String, server: RemoteStore, startMs: Long) {
    var nowMs = startMs
    private val clock = object : Clock {
        override fun now() = Instant.fromEpochMilliseconds(nowMs)
    }
    val db: CoShellDatabase = createInMemoryCoShellDatabase()
    private val stamper = WriteStamper(clock) { member }
    val accounts = RoomAccountRepository(db.accountDao(), db.transactionDao(), stamper)
    val transactions = RoomTransactionRepository(db.transactionDao(), stamper)
    val engine = SyncEngine(db.syncDao(), server, clock)

    fun tick(ms: Long = 1_000) { nowMs += ms }
    fun close() = db.close()
}
