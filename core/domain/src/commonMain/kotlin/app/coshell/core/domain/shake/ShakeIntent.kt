package app.coshell.core.domain.shake

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/** What a recognised shake means right now (#325). */
enum class ShakeIntent { NEW_ENTRY, UNDO_LAST_SAVE }

object ShakeIntents {
    /** As long as the «Сохранено» snackbar stays (`UndoCenter`): a shake then means «oops», not «one more». */
    val UNDO_WINDOW: Duration = 4.seconds

    /**
     * [lastSavedAt] — when an operation was saved from the entry sheet and not undone yet (`null` once undone or
     * when nothing was saved), so a second shake never deletes an older operation.
     */
    fun of(now: Instant, lastSavedAt: Instant?, undoEnabled: Boolean): ShakeIntent {
        if (!undoEnabled || lastSavedAt == null) return ShakeIntent.NEW_ENTRY
        val since = now - lastSavedAt
        return if (!since.isNegative() && since <= UNDO_WINDOW) ShakeIntent.UNDO_LAST_SAVE else ShakeIntent.NEW_ENTRY
    }
}
