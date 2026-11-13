package app.coshell.core.domain.shake

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class ShakeIntentTest {
    private val saved = Instant.parse("2026-11-13T21:40:00Z")

    @Test
    fun `shake right after saving undoes it`() {
        assertEquals(ShakeIntent.UNDO_LAST_SAVE, ShakeIntents.of(saved + 1.seconds, saved, undoEnabled = true))
        assertEquals(ShakeIntent.UNDO_LAST_SAVE, ShakeIntents.of(saved + 4.seconds, saved, undoEnabled = true))
    }

    @Test
    fun `later shake opens a new entry`() {
        assertEquals(ShakeIntent.NEW_ENTRY, ShakeIntents.of(saved + 4.seconds + 1.milliseconds, saved, undoEnabled = true))
    }

    @Test
    fun `nothing saved, already undone or switched off opens a new entry`() {
        assertEquals(ShakeIntent.NEW_ENTRY, ShakeIntents.of(saved, null, undoEnabled = true))
        assertEquals(ShakeIntent.NEW_ENTRY, ShakeIntents.of(saved + 1.seconds, saved, undoEnabled = false))
    }

    @Test
    fun `clock moved back is not an undo`() {
        assertEquals(ShakeIntent.NEW_ENTRY, ShakeIntents.of(saved - 1.seconds, saved, undoEnabled = true))
    }
}
