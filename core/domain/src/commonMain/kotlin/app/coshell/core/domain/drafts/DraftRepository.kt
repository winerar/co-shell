package app.coshell.core.domain.drafts

import app.coshell.core.model.Draft
import kotlinx.coroutines.flow.Flow

/** Drafts of unfinished expenses (#586). Device-only: no sync, no backup; «Стереть всё» empties it. Implementation — `core:data` (Room). */
interface DraftRepository {
    /** Oldest first: the order the lines were written in. */
    fun observeDrafts(): Flow<List<Draft>>
    suspend fun getDraft(id: String): Draft?
    suspend fun saveDrafts(drafts: List<Draft>)
    suspend fun deleteDrafts(ids: List<String>)
}
