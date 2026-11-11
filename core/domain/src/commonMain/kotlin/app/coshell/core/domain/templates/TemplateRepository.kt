package app.coshell.core.domain.templates

import app.coshell.core.model.TransactionTemplate
import kotlinx.coroutines.flow.Flow

/** Operation templates. Implementation — Room in core:data; deletes are soft, like everywhere. */
interface TemplateRepository {
    /** By [TransactionTemplate.sortOrder], then by name. */
    fun observeTemplates(): Flow<List<TransactionTemplate>>
    suspend fun getTemplate(id: String): TransactionTemplate?
    suspend fun saveTemplate(template: TransactionTemplate)
    suspend fun deleteTemplate(id: String)

    /** Largest [TransactionTemplate.sortOrder] of live templates, `null` when there are none. */
    suspend fun maxSortOrder(): Int?
}
