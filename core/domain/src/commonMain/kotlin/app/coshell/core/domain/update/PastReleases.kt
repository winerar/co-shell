package app.coshell.core.domain.update

import kotlinx.datetime.LocalDate

/** One published release in `releases.json` (#284): its version, day of publishing and «что нового». */
data class ReleaseNote(
    val versionCode: Int,
    val versionName: String,
    val date: LocalDate?,
    val notes: String,
) {
    /** Points of [notes]: lines starting with «•» or «—»; a line without a marker continues the previous point. */
    val items: List<String> by lazy { noteItems(notes) }
}

/** History of releases published next to `version.json`, newest first. */
interface ReleaseHistorySource {
    /** Empty when updates are not configured in this build. Throws on network or format errors. */
    suspend fun history(): List<ReleaseNote>
}

/**
 * «Прошлые версии» (#284, board Fb284A): the installed release and the ones before it, newest first,
 * at most [limit]. Newer releases are not «past» — they are offered by the update screen itself.
 */
fun pastReleases(history: List<ReleaseNote>, installedVersionCode: Int, limit: Int = PAST_RELEASES_LIMIT): List<ReleaseNote> =
    history.asSequence()
        .filter { it.versionCode <= installedVersionCode }
        .distinctBy { it.versionCode }
        .sortedByDescending { it.versionCode }
        .take(limit)
        .toList()

const val PAST_RELEASES_LIMIT = 20

internal fun noteItems(notes: String): List<String> {
    val items = mutableListOf<StringBuilder>()
    for (raw in notes.lines()) {
        val line = raw.trim()
        if (line.isEmpty()) continue
        val marker = MARKERS.firstOrNull { line.startsWith(it) }
        if (marker != null || items.isEmpty()) {
            items += StringBuilder(if (marker != null) line.removePrefix(marker).trim() else line)
        } else {
            items.last().append(' ').append(line)
        }
    }
    return items.map { it.toString() }.filter { it.isNotEmpty() }
}

private val MARKERS = listOf("•", "—", "-", "*")
