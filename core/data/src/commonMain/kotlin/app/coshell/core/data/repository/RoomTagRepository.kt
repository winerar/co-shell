package app.coshell.core.data.repository

import app.coshell.core.data.db.LabelUse
import app.coshell.core.data.db.TagDao
import app.coshell.core.domain.tags.TagRepository
import app.coshell.core.domain.tags.TagUsage
import app.coshell.core.model.Tag
import app.coshell.core.model.TagIds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

class RoomTagRepository(
    private val tags: TagDao,
    private val stamper: WriteStamper,
) : TagRepository {

    override fun observeTags(): Flow<List<Tag>> = tags.observe().map { list -> list.map { it.toModel() } }

    // Thousands of operations: the JSON is parsed off the main thread, once per change of `transactions`.
    override fun observeUsage(): Flow<Map<String, TagUsage>> =
        tags.observeLabelUses().map(::usageOf).flowOn(Dispatchers.Default)

    override suspend fun getTags(): List<Tag> = tags.getAll().map { it.toModel() }

    override suspend fun getTag(id: String): Tag? = tags.get(id)?.toModel()

    override suspend fun isIdTaken(id: String): Boolean = tags.find(id) != null

    override suspend fun saveTag(tag: Tag) = tags.upsert(listOf(tag.toEntity(stamper.stamp())))

    override suspend fun deleteTag(id: String) {
        val stamp = stamper.stamp()
        tags.markDeleted(id, stamp.now, stamp.by)
    }

    override suspend fun addImportedTags(tags: List<Tag>) {
        val neverEdited = Stamp(now = 0, by = null)
        this.tags.insertIfAbsent(tags.map { it.toEntity(neverEdited) })
    }
}

/** Per resolved tag id: the first spelling, the number of operations and the newest date. */
internal fun usageOf(rows: List<LabelUse>): Map<String, TagUsage> {
    val result = LinkedHashMap<String, TagUsage>()
    for (row in rows) {
        val labels = runCatching { decodeLabels(row.labels) }.getOrDefault(emptyList())
        for (label in labels.distinctBy(TagIds::of)) {
            val id = TagIds.of(label).ifEmpty { continue }
            val date = Instant.fromEpochMilliseconds(row.date)
            val old = result[id]
            result[id] = if (old == null) TagUsage(label, 1, date) else old.copy(count = old.count + 1, lastUsed = maxOf(old.lastUsed, date))
        }
    }
    return result
}
