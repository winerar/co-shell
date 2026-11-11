package app.coshell.core.data.repository

import app.coshell.core.data.db.TemplateDao
import app.coshell.core.domain.templates.TemplateRepository
import app.coshell.core.model.TransactionTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTemplateRepository(
    private val templates: TemplateDao,
    private val stamper: WriteStamper,
) : TemplateRepository {

    override fun observeTemplates(): Flow<List<TransactionTemplate>> =
        templates.observe().map { list -> list.map { it.toModel() } }

    override suspend fun getTemplate(id: String): TransactionTemplate? = templates.get(id)?.toModel()

    override suspend fun saveTemplate(template: TransactionTemplate) =
        templates.upsert(listOf(template.toEntity(stamper.stamp())))

    override suspend fun deleteTemplate(id: String) {
        val stamp = stamper.stamp()
        templates.markDeleted(id, stamp.now, stamp.by)
    }

    override suspend fun maxSortOrder(): Int? = templates.maxSortOrder()
}
