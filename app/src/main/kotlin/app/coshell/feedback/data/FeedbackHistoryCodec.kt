package app.coshell.feedback.data

import app.coshell.core.domain.feedback.FeedbackStage
import app.coshell.core.domain.feedback.FeedbackStatus
import kotlin.time.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Stored form of [SentFeedback]: a JSON array; short names, unknown fields ignored. */
@Serializable
private data class StoredFeedback(
    val at: Long,
    val text: String,
    val id: String = "",
    val stage: String? = null,
    val issue: Int? = null,
    val issueAt: String? = null,
    val workAt: String? = null,
    val doneAt: String? = null,
    val build: Int? = null,
)

private val json = Json { ignoreUnknownKeys = true }

internal fun encodeFeedbackHistory(entries: List<SentFeedback>): String = json.encodeToString(
    entries.map { e ->
        val s = e.status
        StoredFeedback(
            e.sentAtMillis, e.text, e.documentId, s?.stage?.name, s?.issueNumber,
            s?.issueAt?.toString(), s?.inProgressAt?.toString(), s?.doneAt?.toString(), s?.doneBuild,
        )
    },
)

/** Reads the JSON array; the pre-#157 format (`millis<TAB>text` per line) is still understood. */
internal fun decodeFeedbackHistory(raw: String): List<SentFeedback> {
    if (raw.isBlank()) return emptyList()
    if (!raw.trimStart().startsWith("[")) return decodeLegacy(raw)
    val stored = runCatching { json.decodeFromString<List<StoredFeedback>>(raw) }.getOrDefault(emptyList())
    return stored.map { s ->
        val stage = s.stage?.let { name -> FeedbackStage.entries.firstOrNull { it.name == name } }
        val status = stage?.let { FeedbackStatus(it, s.issue, s.issueAt.instant(), s.workAt.instant(), s.doneAt.instant(), s.build) }
        SentFeedback(s.at, s.text, s.id, status)
    }
}

private fun String?.instant() = this?.let { runCatching { Instant.parse(it) }.getOrNull() }

/** `millis<TAB>text` per line; line breaks inside a text were kept as `\n`. */
private fun decodeLegacy(raw: String): List<SentFeedback> = raw.lines().mapNotNull { line ->
    val millis = line.substringBefore('\t', "").toLongOrNull() ?: return@mapNotNull null
    val text = line.substringAfter('\t').replace(Regex("""\\(\\|n)""")) { if (it.groupValues[1] == "n") "\n" else "\\" }
    SentFeedback(millis, text)
}
