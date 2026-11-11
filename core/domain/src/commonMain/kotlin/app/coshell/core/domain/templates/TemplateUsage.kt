package app.coshell.core.domain.templates

import kotlinx.coroutines.flow.Flow

/**
 * How many operations this phone has saved from each template — for «Часто» and the order of the strip
 * in the «+» sheet. A device preference: each family member has their own frequent templates, so it is
 * neither synced nor part of the backup.
 */
interface TemplateUsage {
    /** Template id → number of operations saved from it. */
    val counts: Flow<Map<String, Int>>
    suspend fun record(templateId: String)
}

/** Most used first; items never used keep their own order (sort order, then name) after them. */
fun <T> List<T>.byUsage(counts: Map<String, Int>, id: (T) -> String): List<T> =
    sortedByDescending { counts[id(it)] ?: 0 }

/** «Часто»: up to [limit] items that were used at least once, most used first. */
fun <T> List<T>.frequent(counts: Map<String, Int>, limit: Int, id: (T) -> String): List<T> =
    byUsage(counts, id).filter { (counts[id(it)] ?: 0) > 0 }.take(limit)
