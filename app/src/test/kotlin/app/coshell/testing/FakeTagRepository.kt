package app.coshell.testing

import app.coshell.core.domain.tags.TagRepository
import app.coshell.core.domain.tags.TagUsage
import app.coshell.core.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Live tags only (deleting drops the row); [usage] is set by the test. */
class FakeTagRepository(vararg initial: Tag) : TagRepository {
    val items = MutableStateFlow(initial.associateBy { it.id })
    val usage = MutableStateFlow<Map<String, TagUsage>>(emptyMap())
    override fun observeTags(): Flow<List<Tag>> = items.map { it.values.sortedBy { t -> t.name.lowercase() } }
    override fun observeUsage(): Flow<Map<String, TagUsage>> = usage
    override suspend fun getTags() = items.value.values.toList()
    override suspend fun getTag(id: String) = items.value[id]
    override suspend fun isIdTaken(id: String) = id in items.value
    override suspend fun saveTag(tag: Tag) { items.value += tag.id to tag }
    override suspend fun deleteTag(id: String) { items.value -= id }
    override suspend fun addImportedTags(tags: List<Tag>) { tags.filter { it.id !in items.value }.forEach { saveTag(it) } }
}
