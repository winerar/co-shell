package app.coshell.core.domain.importer

import app.coshell.core.model.Account
import app.coshell.core.model.Category
import app.coshell.core.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

/** One import run as stored: its operations carry the same `importBatchId`. */
data class ImportBatch(
    val id: String,
    val source: String,
    /** Live operations of the batch (undone or deleted ones are not counted). */
    val count: Int,
    val firstDate: Instant,
    val lastDate: Instant,
    /** When the batch was saved (the earliest write stamp of its operations). */
    val importedAt: Instant,
)

/** Everything one import writes — saved atomically, or nothing is saved. */
data class ImportWrite(
    val accounts: List<Account>,
    val categories: List<Category>,
    val transactions: List<Transaction>,
)

/** Storage side of importers (stage 5c; ARCHITECTURE §8). Implemented in core:data. */
interface ImportRepository {

    /** `importExternalId`s already stored for [source], deleted ones included: re-import skips them. */
    suspend fun knownExternalIds(source: String): Set<String>

    /** One database transaction; the author of new operations is whoever imports. */
    suspend fun save(write: ImportWrite)

    /** Batches with live operations, newest first. */
    fun observeBatches(): Flow<List<ImportBatch>>

    /** Soft-deletes the batch's operations; returns how many. Accounts and categories stay. */
    suspend fun undoBatch(batchId: String): Int
}
