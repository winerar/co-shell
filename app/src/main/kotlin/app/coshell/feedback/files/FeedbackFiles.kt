package app.coshell.feedback.files

import app.coshell.receipt.inbox.SharedFile

/** A file chosen for a feedback message, already copied into the app cache: [path] there, [name] as shown, [size] in bytes. */
data class FeedbackFile(val path: String, val name: String, val mime: String, val size: Long) {
    val kind: Kind
        get() = when {
            mime == "application/pdf" -> Kind.PDF
            mime.startsWith("image/") -> Kind.IMAGE
            else -> Kind.TEXT
        }

    enum class Kind { PDF, IMAGE, TEXT }

    companion object {
        fun of(shared: SharedFile) = FeedbackFile(shared.file.path, shared.name, shared.mime, shared.file.length())
    }
}

/**
 * Where feedback files come from and how they are read at sending (#561): the system picker gives a `content://` uri,
 * copied at once (its permission does not outlive the screen). Android — [AndroidFeedbackFiles]; tests — a fake.
 */
interface FeedbackFiles {
    /** `null` — unreadable or too big to copy. */
    suspend fun fromUri(uri: String): FeedbackFile?

    /** `null` — the copy is gone (cache cleared). */
    suspend fun bytes(file: FeedbackFile): ByteArray?
}
