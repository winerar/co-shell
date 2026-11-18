package app.coshell.core.domain.update

/** What a point of «Что нового» is: something added or changed, or a fix (poll 2026-11-18, labels «Новое» / «Исправлено»). */
enum class NoteKind { New, Fixed }

/** One point of release notes with its [kind]; [text] is without the marker and the «Исправлено:» prefix. */
data class NoteEntry(val kind: NoteKind, val text: String)

/**
 * Points of [notes] (see [noteItems]) with their kinds. A point that starts with «Исправлено:» (or «Исправлен:», «Fixed:»,
 * «Corregido:») is [NoteKind.Fixed], the prefix is cut; every other point is [NoteKind.New] — old notes need no changes.
 */
fun noteEntries(notes: String): List<NoteEntry> = noteItems(notes).map { item ->
    val prefix = FIXED_PREFIXES.firstOrNull { item.startsWith(it, ignoreCase = true) }
    if (prefix == null) NoteEntry(NoteKind.New, item) else NoteEntry(NoteKind.Fixed, item.substring(prefix.length).trim().ifEmpty { item })
}

private val FIXED_PREFIXES = listOf("Исправлено:", "Исправлен:", "Fixed:", "Corregido:")
