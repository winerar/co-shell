package app.coshell.core.data.family

import app.coshell.core.data.db.SyncDao
import app.coshell.core.data.db.SyncStateEntity
import app.coshell.core.model.Family
import app.coshell.core.model.Member
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The family as last seen, kept in `sync_state` so background sync and member names work offline.
 * Used by [FamilyService]; an unreadable value counts as «no cache».
 */
internal class FamilyCache(private val dao: SyncDao) {

    suspend fun load(): Family? =
        dao.state(KEY)?.let { runCatching { json.decodeFromString<CachedFamily>(it).toModel() }.getOrNull() }

    suspend fun save(family: Family?) =
        dao.setState(SyncStateEntity(KEY, family?.let { json.encodeToString(CachedFamily.of(it)) } ?: ""))

    @Serializable
    private data class CachedFamily(val id: String, val ownerUid: String, val members: List<CachedMember>) {
        fun toModel() = Family(id, ownerUid, members.map { Member(it.uid, it.name, it.email) })
        companion object {
            fun of(f: Family) = CachedFamily(f.id, f.ownerUid, f.members.map { CachedMember(it.uid, it.name, it.email) })
        }
    }

    @Serializable
    private data class CachedMember(val uid: String, val name: String, val email: String)

    private companion object {
        const val KEY = "family"
        val json = Json { ignoreUnknownKeys = true }
    }
}
