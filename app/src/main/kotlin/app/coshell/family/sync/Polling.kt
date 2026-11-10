package app.coshell.family.sync

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.withTimeoutOrNull

/**
 * How often to poll: every [baseMs] while something happens, slower and slower while nothing
 * does. After [fastIdleRuns] idle runs in a row the pause doubles up to [maxMs]. Each idle poll
 * still costs a Firestore read, so a phone left open on the table should not spend 360 an hour.
 */
internal class PollBackoff(
    private val baseMs: Long,
    private val maxMs: Long,
    private val fastIdleRuns: Int,
) {
    private var idleRuns = 0

    /** Pause before the next run. */
    val nextDelayMs: Long
        get() {
            val doublings = (idleRuns - fastIdleRuns).coerceIn(0, MAX_DOUBLINGS)
            return (baseMs shl doublings).coerceAtMost(maxMs)
        }

    /** A run that changed something (or the user acted) — back to the fast pace. */
    fun active() { idleRuns = 0 }

    /** A run that found nothing new, or failed. */
    fun idle() { idleRuns++ }

    private companion object {
        const val MAX_DOUBLINGS = 20
    }
}

/**
 * Runs [block] now and then again after [backoff]'s pause until cancelled. [block] returns
 * whether it found anything (keeps the fast pace); a failed run counts as idle, is reported to
 * [onError] and does not stop the loop. An element in [wake] (e.g. the user edited something)
 * resets the pace: the next run comes one fast pause after it (not at once — the edit itself is
 * sent by the worker, and a burst of edits should not cost a pull each).
 */
internal suspend fun pollForever(
    backoff: PollBackoff,
    wake: ReceiveChannel<Unit> = Channel(),
    onError: (Throwable) -> Unit = {},
    block: suspend () -> Boolean,
): Nothing {
    while (true) {
        runCatching { block() }
            .onSuccess { busy -> if (busy) backoff.active() else backoff.idle() }
            .onFailure { backoff.idle(); onError(it) }
        var pause = backoff.nextDelayMs
        while (withTimeoutOrNull(pause) { wake.receive() } != null) {
            backoff.active()
            pause = backoff.nextDelayMs
        }
    }
}
