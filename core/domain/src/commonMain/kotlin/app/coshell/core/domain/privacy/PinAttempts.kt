package app.coshell.core.domain.privacy

/**
 * Wrong PIN attempts (#145, board `Priv145Wait`): the first [FREE] are free; after that every wrong one makes the
 * person wait — 30 s, then twice as long each time, at most 5 minutes. A right PIN or the phone's own lock clears it.
 * Kept on the device, so closing the app does not reset the wait. Times — epoch milliseconds.
 */
data class PinAttempts(val failed: Int = 0, val blockedUntil: Long = 0) {

    /** How long to wait before the next attempt, ms; 0 — may try now. */
    fun waitLeft(now: Long): Long = (blockedUntil - now).coerceAtLeast(0)

    /** One more wrong PIN at [now]. */
    fun fail(now: Long): PinAttempts {
        val count = failed + 1
        if (count < FREE) return PinAttempts(count, 0)
        val doublings = (count - FREE).coerceAtMost(MAX_DOUBLINGS)
        val wait = (FIRST_WAIT shl doublings).coerceAtMost(MAX_WAIT)
        return PinAttempts(count, now + wait)
    }

    companion object {
        const val FREE = 5
        const val FIRST_WAIT = 30_000L
        const val MAX_WAIT = 300_000L
        private const val MAX_DOUBLINGS = 4
    }
}
