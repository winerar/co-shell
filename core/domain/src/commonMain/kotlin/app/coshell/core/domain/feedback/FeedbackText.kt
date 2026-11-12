package app.coshell.core.domain.feedback

/**
 * The stored `text` of a feedback message: what the user wrote, then [DIAGNOSTICS_MARKER] and the technical
 * lines. One field, so the Firestore rules and older builds need no change (#216); the robot that turns
 * messages into issues splits it at the marker (`scripts/feedback_to_issues.py`).
 */
fun FeedbackReport.storedText(): String {
    if (diagnostics.isEmpty()) return text
    val room = STORED_TEXT_LIMIT - text.length - DIAGNOSTICS_MARKER.length
    if (room <= 0) return text
    // Newest journal lines are at the end and matter most, but the header lines go first: cut from the middle.
    val joined = diagnostics.joinToString("\n")
    val tech = if (joined.length <= room) joined else joined.take(room / 2 - 1) + "…" + joined.takeLast(room - room / 2)
    return text + DIAGNOSTICS_MARKER + tech
}

/** Separates the user's text from the technical lines; must match `scripts/feedback_to_issues.py`. */
const val DIAGNOSTICS_MARKER = "\n\n--- tech ---\n"

/** `text` limit in `firebase/firestore.rules`. */
const val STORED_TEXT_LIMIT = 4000
