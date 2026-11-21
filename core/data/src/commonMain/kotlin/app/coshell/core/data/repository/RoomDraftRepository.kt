package app.coshell.core.data.repository

import app.coshell.core.data.db.DraftDao
import app.coshell.core.data.db.DraftEntity
import app.coshell.core.domain.drafts.DraftRepository
import app.coshell.core.model.CurrencyCode
import app.coshell.core.model.Draft
import app.coshell.core.model.DraftCategorySource
import app.coshell.core.model.DraftOrigin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Drafts in Room (#586): device-only, so no [WriteStamper] and no tombstones. An unknown stored enum value (a newer version) reads as the default. */
class RoomDraftRepository(private val dao: DraftDao) : DraftRepository {
    override fun observeDrafts(): Flow<List<Draft>> = dao.observe().map { list -> list.map { it.toModel() } }
    override suspend fun getDraft(id: String): Draft? = dao.get(id)?.toModel()
    override suspend fun saveDrafts(drafts: List<Draft>) = dao.replaceAll(drafts.map { it.toEntity() })
    override suspend fun deleteDrafts(ids: List<String>) {
        if (ids.isNotEmpty()) dao.delete(ids)
    }
}

internal fun DraftEntity.toModel() = Draft(
    id = id, title = title, currency = CurrencyCode(currency), amountMinor = amountMinor, accountId = accountId, categoryId = categoryId,
    categorySource = categorySource?.let { name -> DraftCategorySource.entries.firstOrNull { it.name == name } },
    dateMillis = date, createdAt = createdAt, origin = DraftOrigin.entries.firstOrNull { it.name == origin } ?: DraftOrigin.NOTE,
)

internal fun Draft.toEntity() = DraftEntity(
    id = id, title = title, currency = currency.value, amountMinor = amountMinor, accountId = accountId, categoryId = categoryId,
    categorySource = categorySource?.name, date = dateMillis, createdAt = createdAt, origin = origin.name,
)
