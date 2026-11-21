package app.coshell.core.domain.drafts

import app.coshell.core.model.Draft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Drafts in memory: the default for view models in tests and previews (the app uses Room). */
class InMemoryDraftRepository : DraftRepository {
    val items = MutableStateFlow<List<Draft>>(emptyList())
    override fun observeDrafts(): Flow<List<Draft>> = items
    override suspend fun getDraft(id: String): Draft? = items.value.firstOrNull { it.id == id }
    override suspend fun saveDrafts(drafts: List<Draft>) {
        items.value = (items.value.filter { old -> drafts.none { it.id == old.id } } + drafts).sortedWith(compareBy({ it.createdAt }, { it.id }))
    }
    override suspend fun deleteDrafts(ids: List<String>) {
        items.value = items.value.filter { it.id !in ids }
    }
}
