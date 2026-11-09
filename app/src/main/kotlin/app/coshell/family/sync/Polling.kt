package app.coshell.family.sync

import kotlinx.coroutines.delay

/**
 * Runs [block] now and then every [intervalMs] until cancelled. A failed run is reported to
 * [onError] and does not stop the loop, so a network hiccup only skips one tick.
 */
internal suspend fun pollForever(
    intervalMs: Long,
    onError: (Throwable) -> Unit = {},
    block: suspend () -> Unit,
): Nothing {
    while (true) {
        runCatching { block() }.onFailure(onError)
        delay(intervalMs)
    }
}
