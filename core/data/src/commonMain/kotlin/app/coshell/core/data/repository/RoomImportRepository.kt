package app.coshell.core.data.repository

import app.coshell.core.data.db.ImportDao
import app.coshell.core.domain.importer.ImportBatch
import app.coshell.core.domain.importer.ImportRepository
import app.coshell.core.domain.importer.ImportWrite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

class RoomImportRepository(
    private val dao: ImportDao,
    private val stamper: WriteStamper,
) : ImportRepository {

    override suspend fun knownExternalIds(source: String): Set<String> = dao.externalIds(source).toSet()

    /** New operations are authored by whoever imports; a new personal account belongs to them too. */
    override suspend fun save(write: ImportWrite) {
        val stamp = stamper.stamp()
        dao.save(
            accounts = write.accounts.map { a ->
                (if (!a.isShared && a.ownerMemberId == null) a.copy(ownerMemberId = stamp.by) else a).toEntity(stamp)
            },
            categories = write.categories.map { it.toEntity(stamp) },
            transactions = write.transactions.map { t -> t.copy(authorMemberId = t.authorMemberId ?: stamp.by).toEntity(stamp) },
        )
    }

    override fun observeBatches(): Flow<List<ImportBatch>> = dao.observeBatches().map { rows ->
        rows.map {
            ImportBatch(
                id = it.batchId, source = it.source, count = it.count,
                firstDate = Instant.fromEpochMilliseconds(it.firstDate),
                lastDate = Instant.fromEpochMilliseconds(it.lastDate),
                importedAt = Instant.fromEpochMilliseconds(it.importedAt),
            )
        }
    }

    override suspend fun undoBatch(batchId: String): Int {
        val stamp = stamper.stamp()
        return dao.undoBatch(batchId, stamp.now, stamp.by)
    }
}
