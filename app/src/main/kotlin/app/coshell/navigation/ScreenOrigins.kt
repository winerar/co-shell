package app.coshell.navigation

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import app.coshell.core.designsystem.motion.PressOriginStore
import app.coshell.core.designsystem.motion.transformOriginIn

/**
 * Where each screen grew from (issue #63): taken from the tap that opened it, so going back shrinks it
 * into the same button or row. Keyed by back-stack entry id; transitions ask several times per frame,
 * so the tap is taken once and remembered (also "no tap" — then the screen uses the plain shift).
 */
internal class ScreenOrigins(private val presses: PressOriginStore) {
    /** Window bounds of the area screens are drawn in (set from layout). */
    var hostBounds: Rect = Rect.Zero

    private val byEntry = object : LinkedHashMap<String, TransformOrigin?>() {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, TransformOrigin?>?) = size > MAX_ENTRIES
    }

    /** Origin for a screen being opened: the recent tap, if there was one. */
    fun forEnter(entryId: String): TransformOrigin? {
        if (entryId !in byEntry) byEntry[entryId] = presses.take()?.let { transformOriginIn(hostBounds, it.position) }
        return byEntry[entryId]
    }

    /** Origin a screen being closed shrinks back into: the one it opened from. */
    fun forPop(entryId: String): TransformOrigin? = byEntry[entryId]

    private companion object {
        const val MAX_ENTRIES = 32
    }
}
