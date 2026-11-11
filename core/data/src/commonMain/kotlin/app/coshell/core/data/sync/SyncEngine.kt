package app.coshell.core.data.sync

import app.coshell.core.data.db.PulledRows
import app.coshell.core.data.db.SyncStateEntity
import app.coshell.core.data.db.SyncDao
import app.coshell.core.data.remote.ApiException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock

/**
 * What one [SyncEngine.sync] run did. [pushPaused] — sending was skipped: the daily quota is over
 * ([QuotaPause]); local edits wait for the reset.
 */
data class SyncResult(val pulled: Int, val applied: Int, val pushed: Int, val pushPaused: Boolean = false)

/**
 * Two-way sync of Room with the family's server copy (docs/ARCHITECTURE.md §10).
 *
 * 1. **Pull** records the server stored since the last cursor and merge them: a row is replaced
 *    only if the incoming version is newer ([remoteWins]); a newer local edit stays pending.
 * 2. **Push** every pending row; the flag is cleared only if the row was not edited meanwhile.
 *
 * Pull goes first so that a stale offline edit cannot silently overwrite a newer one on the
 * server. Runs are serialized; a failed run leaves rows pending and the cursor where it was,
 * so the next run simply repeats the work (every step is idempotent). A push that runs out of the
 * daily quota keeps the chunks already sent and pauses sending until the reset ([QuotaPause]).
 */
class SyncEngine(
    private val dao: SyncDao,
    private val remote: RemoteStore,
    private val clock: Clock = Clock.System,
) {
    private val mutex = Mutex()

    suspend fun sync(): SyncResult = mutex.withLock {
        val pulled = remote.pull(cursor = dao.state(CURSOR))
        var applied = 0
        // The cursor points right after the last merged record: the next pull reads only newer
        // ones. (It used to be ">= last server time", and every record of the last commit — they
        // share one server time — was read again on each 10-second poll, exhausting the quota.)
        pulled.chunked(MERGE_CHUNK).forEach { chunk ->
            applied += dao.merge(chunk.toRows(), CURSOR, chunk.last().cursor)
        }
        val paused = isPushPaused()
        val pushed = if (paused) 0 else push()
        dao.setState(SyncStateEntity(LAST_SYNC, clock.now().toEpochMilliseconds().toString()))
        SyncResult(pulled = pulled.size, applied = applied, pushed = pushed, pushPaused = paused)
    }

    /** `true` while the quota pause lasts; an expired pause is cleared. */
    private suspend fun isPushPaused(): Boolean {
        val until = dao.state(QuotaPause.KEY)?.toLongOrNull() ?: return false
        if (clock.now().toEpochMilliseconds() < until) return true
        dao.setState(SyncStateEntity(QuotaPause.KEY, ""))
        return false
    }

    private suspend fun pausePush() {
        val until = QuotaPause.nextReset(clock.now())
        dao.setState(SyncStateEntity(QuotaPause.KEY, until.toEpochMilliseconds().toString()))
    }

    private suspend fun push(): Int {
        val pending = dao.pending()
        val records = buildList {
            pending.accounts.mapTo(this, SyncCodec::encode)
            pending.categories.mapTo(this, SyncCodec::encode)
            pending.transactions.mapTo(this, SyncCodec::encode)
            pending.budgets.mapTo(this, SyncCodec::encode)
            pending.manualRates.mapTo(this, SyncCodec::encode)
        }
        records.chunked(PUSH_CHUNK).forEach { chunk ->
            try {
                remote.push(chunk)
            } catch (e: ApiException) {
                if (e.isQuotaExceeded) pausePush()
                throw e
            }
            dao.clearSent(chunk)
        }
        return records.size
    }

    private fun List<RemoteRecord>.toRows(): PulledRows {
        val byKind = map { it.record }.groupBy { it.kind }
        fun <T> of(kind: SyncKind, decode: (SyncRecord) -> T) = byKind[kind].orEmpty().map(decode)
        return PulledRows(
            accounts = of(SyncKind.ACCOUNT, SyncCodec::account),
            categories = of(SyncKind.CATEGORY, SyncCodec::category),
            transactions = of(SyncKind.TRANSACTION, SyncCodec::transaction),
            budgets = of(SyncKind.BUDGET, SyncCodec::budget),
            manualRates = of(SyncKind.MANUAL_RATE, SyncCodec::manualRate),
        )
    }

    companion object {
        /** Key in `sync_state`: [RemoteRecord.cursor] of the newest pulled record. */
        const val CURSOR = "pull_cursor"
        /** Key in `sync_state`: epoch ms of the last successful sync, shown on the family screen. */
        const val LAST_SYNC = "last_sync"
        // Firestore commits take at most 500 writes; SQLite `IN (...)` lists stay small.
        private const val PUSH_CHUNK = 400
        private const val MERGE_CHUNK = 500
    }
}
