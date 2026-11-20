package app.coshell.core.domain.feedback

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * A file sent with a feedback message: a receipt the app could not read («Формат чека»), a screenshot of a bug.
 * Firestore on the free plan has no file storage, so a file travels as base64 [chunks] — documents of
 * `feedback_files` (≤ 1 MiB each); the robot glues them back (`scripts/feedback_to_issues.py`).
 */
class FeedbackAttachment(val name: String, val mime: String, val bytes: ByteArray) {

    /** Base64 parts of at most [CHUNK_CHARS] characters, in order. */
    @OptIn(ExperimentalEncodingApi::class)
    fun chunks(): List<String> = Base64.Default.encode(bytes).chunked(CHUNK_CHARS)

    companion object {
        /** Limits must match `firebase/firestore.rules` (`feedback_files`, `attachments`). */
        const val MAX_COUNT = 3
        const val MAX_TOTAL_BYTES = 3 * 1024 * 1024
        const val CHUNK_CHARS = 700_000
        const val MAX_NAME = 120

        /** `null` — fine; otherwise why the files cannot go. */
        fun problem(files: List<FeedbackAttachment>): FeedbackResult? = when {
            files.size > MAX_COUNT -> FeedbackResult.TooManyFiles(MAX_COUNT)
            files.sumOf { it.bytes.size.toLong() } > MAX_TOTAL_BYTES -> FeedbackResult.FilesTooLarge(MAX_TOTAL_BYTES)
            else -> null
        }

        /** The name as stored: no path, at most [MAX_NAME] characters, never empty. */
        fun cleanName(name: String): String =
            name.substringAfterLast('/').substringAfterLast('\\').trim().take(MAX_NAME).ifEmpty { "file" }
    }
}
