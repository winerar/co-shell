package app.coshell.update.data

import app.coshell.core.domain.update.ReleaseNote
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** One entry of `releases.json` (CI, `scripts/make-update-files.py`): newest first, last ~20 releases. */
@Serializable
private data class ReleaseHistoryEntry(
    val versionCode: Int,
    val versionName: String,
    val date: String = "",
    val notes: String = "",
)

private val json = Json { ignoreUnknownKeys = true }

/** Parses `releases.json`; throws [IllegalArgumentException] on bad data. A bad date is just left out. */
internal fun parseReleaseHistory(text: String): List<ReleaseNote> {
    val entries = try {
        json.decodeFromString<List<ReleaseHistoryEntry>>(text)
    } catch (e: Exception) {
        throw IllegalArgumentException("Bad releases.json: ${e.message}", e)
    }
    return entries.map {
        ReleaseNote(
            versionCode = it.versionCode,
            versionName = it.versionName,
            date = runCatching { LocalDate.parse(it.date) }.getOrNull(),
            notes = it.notes.trim(),
        )
    }
}
