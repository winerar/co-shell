package app.coshell.core.domain.tags

import app.coshell.core.model.Tag
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

/** Operation tags (#129). Implementation — Room in core:data; deletes are soft, like everywhere. */
interface TagRepository {
    /** Live tags by name (case-insensitive). */
    fun observeTags(): Flow<List<Tag>>

    /** How live operations use each tag reference, keyed by resolved id (`TagIds.of`). */
    fun observeUsage(): Flow<Map<String, TagUsage>>

    suspend fun getTags(): List<Tag>
    suspend fun getTag(id: String): Tag?

    /** `true` when a row with [id] exists, deleted ones included: such an id is not given to a new tag. */
    suspend fun isIdTaken(id: String): Boolean

    suspend fun saveTag(tag: Tag)
    suspend fun deleteTag(id: String)

    /**
     * Adds tags for imported labels ([EnsureLabelTagsUseCase]) only where no row exists, stamped as never
     * edited (time 0): the other phone may add the same tags, and any real edit of them wins the sync.
     */
    suspend fun addImportedTags(tags: List<Tag>)
}

/**
 * Use of one tag: [count] live operations, the newest dated [lastUsed]; [label] — the reference as the first
 * operation wrote it (a Wallet label keeps its case: «Mercado Libre»).
 */
data class TagUsage(val label: String, val count: Int, val lastUsed: Instant)
