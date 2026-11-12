package app.coshell.crash

import kotlin.time.Instant

/**
 * Text of a crash as kept on the phone (#218): when, which build, which thread, then the exception and the
 * first [MAX_FRAMES] frames of each cause. Small enough for the technical data of a feedback message.
 */
internal fun crashText(error: Throwable, at: Instant, versionCode: Int, thread: String): String = buildString {
    appendLine("crash at ${Instant.fromEpochSeconds(at.epochSeconds)}, build $versionCode, thread $thread")
    var current: Throwable? = error
    var depth = 0
    while (current != null && depth < MAX_CAUSES) {
        if (depth > 0) append("caused by: ")
        appendLine(current.toString().take(MAX_MESSAGE))
        current.stackTrace.take(MAX_FRAMES).forEach { appendLine("  at $it") }
        current = current.cause?.takeIf { it !== current }
        depth++
    }
}.trimEnd()

/** First line of [crashText] without the header: «IllegalStateException: …», for the message text. */
internal fun crashTitle(text: String): String {
    val line = text.lineSequence().drop(1).firstOrNull()?.takeIf { it.isNotBlank() } ?: return "unknown"
    val type = line.substringBefore(": ").substringAfterLast('.')
    val message = line.substringAfter(": ", "")
    return (if (message.isEmpty()) type else "$type: $message").take(MAX_TITLE)
}

private const val MAX_FRAMES = 15
private const val MAX_CAUSES = 3
private const val MAX_MESSAGE = 300
private const val MAX_TITLE = 200
