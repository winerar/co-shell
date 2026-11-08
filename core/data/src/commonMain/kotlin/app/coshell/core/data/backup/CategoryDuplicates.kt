package app.coshell.core.data.backup

import app.coshell.core.model.Category

/**
 * Finds local categories that duplicate a category from the backup: same type, same name
 * (trimmed, case-insensitive) and the same folder, but a different id.
 *
 * Typical case: a fresh install seeds the default categories with new ids, then a backup from the
 * old phone is restored — "Продукты" from the backup must replace the seeded "Продукты", not sit
 * next to it. Returns local id → backup id; local ids that are also in the backup are never
 * duplicates (the backup overwrites them by id).
 */
internal fun findDuplicateCategories(local: List<Category>, incoming: List<Category>): Map<String, String> {
    val incomingIds = incoming.map { it.id }.toSet()
    val incomingByKey = incoming.groupBy { it.matchKey(it.parentId) }.mapValues { it.value.first().id }
    val candidates = local.filter { it.id !in incomingIds }
    val result = mutableMapOf<String, String>()
    // Folders first, so a subcategory can follow its folder: one level of nesting only.
    val (topLevel, children) = candidates.partition { it.parentId == null }
    for (category in topLevel + children) {
        val parent = category.parentId?.let { result[it] ?: it }
        incomingByKey[category.matchKey(parent)]?.let { result[category.id] = it }
    }
    return result
}

private fun Category.matchKey(parent: String?) = Triple(type, name.trim().lowercase(), parent)
